package com.aims.prod.Controller;

import com.aims.prod.Entity.*;
import com.aims.prod.Repository.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Added for RedirectAttributes

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PaymentController {

    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @Value("${stripe.api.key}") // Assuming you have a secret key property
    private String stripeSecretKey;

    public PaymentController(PolicyRepository policyRepository, ClaimRepository claimRepository, PaymentRepository paymentRepository) {
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/payment")
    public String home(@RequestParam Long policyId, Model model, HttpSession session) {
        session.setAttribute("policyId", policyId);
        model.addAttribute("stripePublicKey", stripePublicKey);
        Policy p = policyRepository.findById(policyId).orElse(null);
        model.addAttribute("price", p.getPrice());
        return "payment-checkout";
    }

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public Map<String, String> createCheckoutSession(HttpSession session) throws StripeException {
        Long policyId = (Long) session.getAttribute("policyId");
        Policy policy = policyRepository.findById(policyId).orElseThrow();
        User user = (User) session.getAttribute("user");

        // Ensure Stripe API key is set before creating objects
        Stripe.apiKey = stripeSecretKey;

        Map<String, Object> customerParams = new HashMap<>();
        customerParams.put("name", user.getName());
        customerParams.put("email", user.getEmail());

        Map<String, Object> address = new HashMap<>();
        address.put("line1", "Main Street");
        address.put("city", "Hyderabad");
        address.put("state", "Telangana");
        address.put("country", "IN");
        address.put("postal_code", "500001");

        customerParams.put("address", address);
        Customer customer = Customer.create(customerParams);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomer(customer.getId())
                .setSuccessUrl("http://localhost:8080/payment-success?session_id={CHECKOUT_SESSION_ID}") // Add session_id to success URL
                .setCancelUrl("http://localhost:8080/payment-cancel")
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) //ALIPAY works but the currency should be in cny - chineese
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount((long) (policy.getPrice() * 100)) // Stripe expects amount in smallest currency unit (paise)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(policy.getPolicyName())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session stripeSession = Session.create(params);
        // It's better to save the checkout session ID directly, and later retrieve the payment intent from it.
        // For the purpose of refund, we need the PaymentIntent ID, not just the checkout session ID.
        // We'll retrieve the PaymentIntent ID in the success handler.
        session.setAttribute("checkoutSessionId", stripeSession.getId());

        Map<String, String> response = new HashMap<>();
        response.put("id", stripeSession.getId());
        return response;
    }

    @GetMapping("/payment-success")
    public String handleSuccess(@RequestParam("session_id") String sessionId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            Long policyId = (Long) session.getAttribute("policyId");
            Policy policy = policyRepository.findById(policyId).orElseThrow();

            // Retrieve the Checkout Session to get the PaymentIntent ID
            Stripe.apiKey = stripeSecretKey; // Ensure API key is set
            Session checkoutSession = Session.retrieve(sessionId);
            String paymentIntentId = checkoutSession.getPaymentIntent();

            Claim claim = new Claim();
            claim.setUser(user);
            claim.setPolicy(policy);
            claim.setDate(LocalDateTime.now());
            claim.setStatus("Premium Purchased");
            claim.setPolicyName(policy.getPolicyName());
            // Store the PaymentIntent ID for potential refunds
            claim.setPaymentIntentId(paymentIntentId); // This is the crucial change

            claimRepository.save(claim);
            paymentRepository.save(new Payment(user.getEmail(), "succeeded", (long) policy.getPrice(), paymentIntentId, LocalDateTime.now()));
        } catch (StripeException e) {
            e.printStackTrace();
            return "redirect:/payment-cancel";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/payment-cancel";
        }

        return "redirect:/user/claims";
    }

    @GetMapping("/payment-cancel")
    public String handleCancel() {
        return "redirect:/user/home";
    }

    // This method would typically be in an AgentController or similar
    @PostMapping("agent/claim/{id}/approve")
    public String approveClaim(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim != null) {
            // Check if the claim is already approved and refunded to prevent double refunds
            if (claim.getStatus().equals("Approved & Refunded")) {
                redirectAttributes.addFlashAttribute("error", "Claim already approved and refunded.");
                return "redirect:/agent/claim-requests";
            }
            
            claim.setStatus("Approved");
            claim.setAgentResponseDate(LocalDateTime.now());
            claimRepository.save(claim);

            Stripe.apiKey = stripeSecretKey; // Ensure your Stripe secret key is set

            try {
                // Ensure the paymentIntentId is actually the PaymentIntent ID, not the Checkout Session ID
                String paymentIntentId = claim.getPaymentIntentId();

                if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Payment Intent ID not found for this claim. Cannot issue refund.");
                    return "redirect:/agent/claim-requests";
                }

                RefundCreateParams params = RefundCreateParams.builder()
                        .setPaymentIntent(paymentIntentId)
                        .build();

                Refund refund = Refund.create(params);
                claim.setRefundId(refund.getId());
                claim.setStatus("Approved & Refunded");
                claimRepository.save(claim);
                redirectAttributes.addFlashAttribute("message", "Claim approved and refund initiated successfully.");

            } catch (StripeException e) {
                e.printStackTrace();
                // Optionally update claim status to reflect refund failure
                claim.setStatus("Approved (Refund Failed)");
                claimRepository.save(claim);
                redirectAttributes.addFlashAttribute("error", "Refund failed: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "An unexpected error occurred during refund: " + e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Claim not found.");
        }
        return "redirect:/agent/claim-requests";
    }
    
    @GetMapping("/user/payment-history")
	public String viewPaymentHistory(HttpSession session, Model model) {
	User user = (User) session.getAttribute("user");
    if (user == null||!user.getRole().equals("user")) return "redirect:/login";


	List<Payment> payments = paymentRepository.findByEmail(user.getEmail());
	model.addAttribute("payments", payments);
	return "user-payment-history";
	}
}