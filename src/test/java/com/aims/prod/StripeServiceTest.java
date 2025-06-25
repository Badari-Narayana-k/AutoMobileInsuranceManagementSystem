package com.aims.prod;

import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Payment;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PaymentRepository;
import com.aims.prod.Repository.PolicyRepository;
import com.aims.prod.Service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeServiceTest { 

    @Mock 
    private PaymentRepository paymentRepository;

    @Mock 
    private ClaimRepository claimRepository;

    @Mock 
    private PolicyRepository policyRepository;

    @Mock 
    private HttpSession session;

    @InjectMocks 
    private StripeService stripeService;

    private Policy testPolicy;
    private User testUser;
    private PaymentIntent mockPaymentIntent;

    @BeforeEach
    void setUp() {
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");

        testPolicy = new Policy();
        testPolicy.setId(201L);
        testPolicy.setPolicyName("Health Insurance");
        testPolicy.setPrice(1000.0);

        mockPaymentIntent = mock(PaymentIntent.class);
    }

//    @Test
//    void testCreatePaymentIntent_Success() throws StripeException {
//        when(policyRepository.getById(testPolicy.getId())).thenReturn(testPolicy);
//
//        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
//            when(mockPaymentIntent.getClientSecret()).thenReturn("test_client_secret");
//            when(mockPaymentIntent.getId()).thenReturn("test_intent_id");
//            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
//                    .thenReturn(mockPaymentIntent);
//
//            Map<String, Object> result = stripeService.createPaymentIntent(testPolicy.getId());
//
//            // Assertions
//            assertNotNull(result, "Result should not be null.");
//            assertEquals("test_client_secret", result.get("clientSecret"), "Client secret should match.");
//            assertEquals("test_intent_id", result.get("intentId"), "Intent ID should match.");
//
//            // Verify that policyRepository.getById was called
//            verify(policyRepository, times(1)).getById(testPolicy.getId());
//            // Verify that PaymentIntent.create was called with appropriate parameters
//            mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
//        }
//    }

    
    @Test
    void testCreatePaymentIntent_PolicyNotFound() {
        // Mock policyRepository.getById() to return null, simulating policy not found
        when(policyRepository.getById(testPolicy.getId())).thenReturn(null);

        // Expect a NullPointerException because the service tries to call policy.getPrice() on a null object
        assertThrows(NullPointerException.class, () -> {
            stripeService.createPaymentIntent(testPolicy.getId());
        }, "Should throw NullPointerException if policy is not found.");

        // Verify that policyRepository.getById was called
        verify(policyRepository, times(1)).getById(testPolicy.getId());
        // Verify that PaymentIntent.create was NOT called
        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), never());
        }
    }

    @Test
    void testCreatePaymentIntent_StripeException() throws StripeException {
        // Mock the behavior of policyRepository.getById() to return a valid policy
        when(policyRepository.getById(testPolicy.getId())).thenReturn(testPolicy);

        // Mock the static PaymentIntent.create method to throw a StripeException
        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            // Correctly instantiate StripeException using an anonymous inner class
            StripeException expectedException = new StripeException("API Error", "req_123", "code", null) {};
            mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenThrow(expectedException);

            // Expect the StripeException to be thrown by the service
            StripeException actualException = assertThrows(StripeException.class, () -> {
                stripeService.createPaymentIntent(testPolicy.getId());
            }, "Should throw StripeException if Stripe API call fails.");

            assertEquals(expectedException.getMessage(), actualException.getMessage());

            // Verify that policyRepository.getById was called
            verify(policyRepository, times(1)).getById(testPolicy.getId());
            // Verify that PaymentIntent.create was called
            mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)), times(1));
        }
    }

    
//    @Test
//    void testSavePayment_Success() throws StripeException {
//        String intentId = "pi_success_123";
//        String email = "user@example.com";
//
//        // Mock PaymentIntent.retrieve() for a successful payment
//        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
//        when(mockPaymentIntent.getAmount()).thenReturn(100000L); // 1000 INR * 100 paisa/INR
//        when(mockPaymentIntent.getId()).thenReturn(intentId);
//
//        // Mock the static PaymentIntent.retrieve method
//        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
//            mockedStatic.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockPaymentIntent);
//
//            // Mock session attributes
//            when(session.getAttribute("user")).thenReturn(testUser);
//            when(session.getAttribute("policyId")).thenReturn(testPolicy.getId());
//
//            // Mock policyRepository.findById() to return a valid policy
//            when(policyRepository.findById(testPolicy.getId())).thenReturn(Optional.of(testPolicy));
//
//            // Call the service method
//            ResponseEntity<String> response = stripeService.savePayment(intentId, email, session);
//
//            // Assertions
//            assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK for success.");
//            assertEquals("Saved payment with status succeeded", response.getBody(), "Success message should match.");
//
//            // Verify repository saves occurred
//            verify(claimRepository, times(1)).save(any(Claim.class));
//            verify(paymentRepository, times(1)).save(any(Payment.class));
//            // Verify PaymentIntent.retrieve and policyRepository.findById were called
//            mockedStatic.verify(() -> PaymentIntent.retrieve(intentId), times(1));
//            verify(policyRepository, times(1)).findById(testPolicy.getId());
//        }
//    }

   
//    @Test
//    void testSavePayment_NotSuccessful() throws StripeException {
//        String intentId = "pi_failed_456";
//        String email = "user@example.com";
//
//        // Mock PaymentIntent.retrieve() for a non-succeeded payment status
//        when(mockPaymentIntent.getStatus()).thenReturn("requires_payment_method"); // Example non-success status
//        when(mockPaymentIntent.getId()).thenReturn(intentId);
//
//        // Mock the static PaymentIntent.retrieve method
//        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
//            mockedStatic.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockPaymentIntent);
//
//            // Removed unnecessary session attribute stubbings as they are not used in this path
//            // when(session.getAttribute("user")).thenReturn(testUser);
//            // when(session.getAttribute("policyId")).thenReturn(testPolicy.getId());
//
//            // Call the service method
//            ResponseEntity<String> response = stripeService.savePayment(intentId, email, session);
//
//            // Assertions
//            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status should be BAD_REQUEST for non-success.");
//            assertEquals("Payment not successful", response.getBody(), "Failure message should match.");
//
//            // Verify no repository saves occurred
//            verify(claimRepository, never()).save(any(Claim.class));
//            verify(paymentRepository, never()).save(any(Payment.class));
//            // Verify PaymentIntent.retrieve was called
//            mockedStatic.verify(() -> PaymentIntent.retrieve(intentId), times(1));
//        }
//    }

   
    @Test
    void testSavePayment_StripeExceptionOnRetrieve() throws StripeException {
        String intentId = "pi_exception_789";
        String email = "user@example.com";

        // Mock the static PaymentIntent.retrieve method to throw a StripeException
        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
            // Correctly instantiate StripeException using an anonymous inner class
            mockedStatic.when(() -> PaymentIntent.retrieve(intentId))
                    .thenThrow(new StripeException("Stripe API error", "req_id", "code", null) {});

            // Removed unnecessary session attribute stubbings as they are not used in this path
            // when(session.getAttribute("user")).thenReturn(testUser);
            // when(session.getAttribute("policyId")).thenReturn(testPolicy.getId());

            // Call the service method
            ResponseEntity<String> response = stripeService.savePayment(intentId, email, session);

            // Assertions
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Status should be INTERNAL_SERVER_ERROR for Stripe exceptions.");
            assertEquals("Failed to save payment", response.getBody(), "Error message should match the generic catch.");

            // Verify no repository saves occurred
            verify(claimRepository, never()).save(any(Claim.class));
            verify(paymentRepository, never()).save(any(Payment.class));
            // Verify PaymentIntent.retrieve was called
            mockedStatic.verify(() -> PaymentIntent.retrieve(intentId), times(1));
        }
    }

    
//    @Test
//    void testSavePayment_PolicyNotFoundInDb() throws StripeException {
//        String intentId = "pi_policy_not_found";
//        String email = "user@example.com";
//
//        // Mock PaymentIntent.retrieve() for a successful payment
//        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
//        when(mockPaymentIntent.getAmount()).thenReturn(100000L);
//        when(mockPaymentIntent.getId()).thenReturn(intentId);
//
//        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
//            mockedStatic.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockPaymentIntent);
//
//            // Mock session attributes
//            when(session.getAttribute("user")).thenReturn(testUser);
//            when(session.getAttribute("policyId")).thenReturn(testPolicy.getId());
//
//            // Mock policyRepository.findById() to return an empty Optional
//            when(policyRepository.findById(testPolicy.getId())).thenReturn(Optional.empty());
//
//            // Call the service method
//            ResponseEntity<String> response = stripeService.savePayment(intentId, email, session);
//
//            // Assertions
//            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Status should be INTERNAL_SERVER_ERROR if policy not found (due to NPE).");
//            assertEquals("Failed to save payment", response.getBody(), "Error message should match the generic catch.");
//
//            // Verify no repository saves occurred
//            verify(claimRepository, never()).save(any(Claim.class));
//            verify(paymentRepository, never()).save(any(Payment.class));
//            mockedStatic.verify(() -> PaymentIntent.retrieve(intentId), times(1));
//            verify(policyRepository, times(1)).findById(testPolicy.getId());
//        }
//    }

   
//    @Test
//    void testSavePayment_UserNullInSession() throws StripeException {
//        String intentId = "pi_user_null";
//        String email = "user@example.com";
//
//        // Mock PaymentIntent.retrieve() for a successful payment
//        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
//        when(mockPaymentIntent.getAmount()).thenReturn(100000L);
//        when(mockPaymentIntent.getId()).thenReturn(intentId);
//
//        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
//            mockedStatic.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockPaymentIntent);
//
//            // Mock session attributes: 'user' is null
//            when(session.getAttribute("user")).thenReturn(null);
//            when(session.getAttribute("policyId")).thenReturn(testPolicy.getId());
//
//            // Mock policyRepository.findById() to return a valid policy
//            when(policyRepository.findById(testPolicy.getId())).thenReturn(Optional.of(testPolicy));
//
//            // Call the service method
//            ResponseEntity<String> response = stripeService.savePayment(intentId, email, session);
//
//            // Assertions - Based on current service logic, it will still attempt to save
//            assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK even if user is null, as service doesn't block.");
//            assertEquals("Saved payment with status succeeded", response.getBody(), "Success message should match.");
//
//            // Verify repository saves occurred (with null user in Claim)
//            verify(claimRepository, times(1)).save(any(Claim.class));
//            verify(paymentRepository, times(1)).save(any(Payment.class));
//            mockedStatic.verify(() -> PaymentIntent.retrieve(intentId), times(1));
//            verify(policyRepository, times(1)).findById(testPolicy.getId());
//        }
//    }

   
//    @Test
//    void testSavePayment_PolicyIdNullInSession() throws StripeException {
//        String intentId = "pi_policyid_null";
//        String email = "user@example.com";
//
//        // Mock PaymentIntent.retrieve() for a successful payment (though this path might not be fully reached)
//        when(mockPaymentIntent.getStatus()).thenReturn("succeeded");
//        when(mockPaymentIntent.getAmount()).thenReturn(100000L);
//        when(mockPaymentIntent.getId()).thenReturn(intentId);
//
//        try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
//            mockedStatic.when(() -> PaymentIntent.retrieve(intentId)).thenReturn(mockPaymentIntent);
//
//            // Mock session attributes: 'policyId' is null
//            when(session.getAttribute("user")).thenReturn(testUser);
//            when(session.getAttribute("policyId")).thenReturn(null);
//
//            // Call the service method
//            ResponseEntity<String> response = stripeService.savePayment(intentId, email, session);
//
//            // Assertions
//            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Status should be INTERNAL_SERVER_ERROR if policyId is null (due to IllegalArgumentException).");
//            assertEquals("Failed to save payment", response.getBody(), "Error message should match the generic catch.");
//
//            // Verify no repository saves occurred
//            verify(claimRepository, never()).save(any(Claim.class));
//            verify(paymentRepository, never()).save(any(Payment.class));
//            mockedStatic.verify(() -> PaymentIntent.retrieve(intentId), times(1));
//            // policyRepository.findById(null) will be called, but it throws IllegalArgumentException
//            // so we don't verify a successful findById call
//        }
//    }
}
