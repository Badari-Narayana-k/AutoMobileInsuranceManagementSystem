package com.aims.prod.Service;

import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Payment;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PaymentRepository;
import com.aims.prod.Repository.PolicyRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    private final PaymentRepository paymentRepository;
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

    public StripeService(PaymentRepository paymentRepository,
                         ClaimRepository claimRepository,
                         PolicyRepository policyRepository) {
        this.paymentRepository = paymentRepository;
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
    }

    public Map<String, Object> createPaymentIntent(Long policyId) throws StripeException {
        Policy policy = policyRepository.getById(policyId);

        PaymentIntentCreateParams.Shipping.Address address = PaymentIntentCreateParams.Shipping.Address.builder()
                .setLine1("123 main street")
                .setCity("Hyderabad")
                .setCountry("IN")
                .setPostalCode("500001")
                .build();

        PaymentIntentCreateParams.Shipping shipping = PaymentIntentCreateParams.Shipping.builder()
                .setName("Test User")
                .setAddress(address)
                .build();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) policy.getPrice())
                .setCurrency("inr")
                .setDescription("Payment for policy: " + policy.getPolicyName())
                .setShipping(shipping)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("clientSecret", intent.getClientSecret());
        responseData.put("intentId", intent.getId());

        return responseData;
    }

    public ResponseEntity<String> savePayment(String intentId, String email, HttpSession session) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(intentId);
            String status = intent.getStatus();
            Long amount = intent.getAmount();
            LocalDateTime timeStamp = LocalDateTime.now();

            User user = (User) session.getAttribute("user");
            Long policyId = (Long) session.getAttribute("policyId");

            if ("succeeded".equals(status)) {
                Policy policy = policyRepository.findById(policyId).orElse(null);
                Claim claim = new Claim();
                claim.setUser(user);
                claim.setPolicy(policy);
                claim.setDate(timeStamp);
                claim.setStatus("Premium Purchased");
                claim.setPolicyName(policy.getPolicyName());
                claim.setPaymentIntentId(intentId);

                claimRepository.save(claim);
                paymentRepository.save(new Payment(email, status, amount, intentId, timeStamp));

                return ResponseEntity.ok("Saved payment with status " + status);
            } else {
                return ResponseEntity.status(400).body("Payment not successful");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to save payment");
        }
    }
}
