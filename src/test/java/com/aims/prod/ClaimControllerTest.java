package com.aims.prod;

import com.aims.prod.Controller.ClaimController;
import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Policy; // Assuming Policy entity exists and has an agent
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository; // Import the repository
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Use MockBean for the repository
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Needed for stream operations

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimController.class)
public class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimRepository claimRepository; // Now mocking ClaimRepository directly

    private MockHttpSession session;
    private User user;
    private User agent;
    private Claim claim1;
    private Claim claim2;
    private Policy policy; // Assuming Policy entity exists

    @BeforeEach
    void setUp() {
        // Setup common test data
        user = new User();
        user.setId(1L);
        user.setName("testuser");
        user.setRole("user");

        agent = new User();
        agent.setId(2L);
        agent.setName("testagent");
        agent.setRole("agent");

        policy = new Policy();
        policy.setId(10L);
        policy.setAgent(agent); // Associate policy with the agent

        claim1 = new Claim();
        claim1.setId(1L);
        claim1.setPolicyName("Health Policy");
        claim1.setStatus("Approved");
        claim1.setDate(LocalDateTime.of(2023, 1, 15, 10, 0));
        claim1.setUser(user); // Set user for claim1
        claim1.setPolicy(policy); // Associate claim with policy

        claim2 = new Claim();
        claim2.setId(2L);
        claim2.setPolicyName("Car Policy");
        claim2.setStatus("Pending");
        claim2.setDate(LocalDateTime.of(2023, 2, 20, 11, 30));
        claim2.setUser(user); // Set user for claim2
        claim2.setPolicy(policy); // Associate claim with policy

        session = new MockHttpSession();
    }

    // --- User Claims Tests ---

    @Test
    void viewClass_loggedInUserNoSearchParams_returnsUserClaimsView() throws Exception {
        session.setAttribute("user", user);
        List<Claim> userClaims = Arrays.asList(claim1, claim2);
        // Mocking the behavior for the initial load (no search params)
        when(claimRepository.findByUserId(user.getId())).thenReturn(userClaims);

        mockMvc.perform(get("/user/claims").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("user-claims"))
                .andExpect(model().attributeExists("claims"))
                .andExpect(model().attribute("claims", userClaims));

        verify(claimRepository, times(1)).findByUserId(user.getId());
        verify(claimRepository, never()).searchClaims(any(), any(), any(), any()); // Ensure searchClaims is not called
    }

    
    

   
    

    @Test
    void viewClass_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/claims"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void viewClass_sessionWithoutUser_redirectsToLogin() throws Exception {
        session.setAttribute("someOtherAttribute", "value"); // No user attribute
        mockMvc.perform(get("/user/claims").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void viewClass_loggedOutUser_redirectsToLogin() throws Exception {
        User loggedOutUser = new User();
        loggedOutUser.setId(3L);
        loggedOutUser.setRole("guest"); // Not a valid role for this page
        session.setAttribute("user", loggedOutUser);

        mockMvc.perform(get("/user/claims").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void requestClaim_successfulRequest_redirects() throws Exception {
        session.setAttribute("user", user); // Ensure user is logged in for security check (though not in original controller)
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim2)); // claim2 is 'Pending' after request
        when(claimRepository.save(any(Claim.class))).thenReturn(claim2); // Mock save operation

        mockMvc.perform(post("/user/claims/request/{claimId}", 1L).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/claims"));

        // Verify that findById was called, status was changed, and save was called
        verify(claimRepository, times(1)).findById(1L);
        verify(claimRepository, times(1)).save(argThat(claim -> claim.getStatus().equals("Pending")));
    }

    @Test
    void requestClaim_claimNotFound_redirectsWithoutError() throws Exception {
        session.setAttribute("user", user); // Ensure user is logged in
        when(claimRepository.findById(anyLong())).thenReturn(Optional.empty()); // Claim not found

        mockMvc.perform(post("/user/claims/request/{claimId}", 99L).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/claims"));

        verify(claimRepository, times(1)).findById(99L);
        verify(claimRepository, never()).save(any(Claim.class)); // Save should not be called
    }

    

    @Test
    void getAllClaimRequests_loggedInAgent_returnsAgentClaimRequestsView() throws Exception {
        session.setAttribute("user", agent);
        List<Claim> allPendingClaims = Arrays.asList(claim2); // Only claim2 is pending
        when(claimRepository.findByStatus("Pending")).thenReturn(allPendingClaims);

        mockMvc.perform(get("/agent/claim-requests").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("agent-claim-requests"))
                .andExpect(model().attributeExists("claims"))
                // Controller filters by agent's policy, so verify that filtering works
                .andExpect(model().attribute("claims", Collections.singletonList(claim2))); // Assuming claim2 is linked to agent's policy

        verify(claimRepository, times(1)).findByStatus("Pending");
    }

    @Test
    void getAllClaimRequests_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/agent/claim-requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void getAllClaimRequests_notAnAgent_redirectsToLogin() throws Exception {
        session.setAttribute("user", user); // User trying to access agent page
        mockMvc.perform(get("/agent/claim-requests").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void rejectClaim_successfulRejection_redirects() throws Exception {
        session.setAttribute("user", agent); // Ensure agent is logged in
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim1)); // Mock finding the claim
        when(claimRepository.save(any(Claim.class))).thenReturn(claim1); // Mock saving the claim

        mockMvc.perform(post("/agent/claim/{id}/reject", 1L).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/claim-requests"));

        // Verify that findById was called, status was changed, and save was called
        verify(claimRepository, times(1)).findById(1L);
        verify(claimRepository, times(1)).save(argThat(claim -> claim.getStatus().equals("Rejected") && claim.getAgentResponseDate() != null));
    }

    @Test
    void rejectClaim_claimNotFound_redirectsWithoutError() throws Exception {
        session.setAttribute("user", agent); // Ensure agent is logged in
        when(claimRepository.findById(anyLong())).thenReturn(Optional.empty()); // Claim not found

        mockMvc.perform(post("/agent/claim/{id}/reject", 99L).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/claim-requests"));

        verify(claimRepository, times(1)).findById(99L);
        verify(claimRepository, never()).save(any(Claim.class)); // Save should not be called
    }

    
}