package com.aims.prod;

import com.aims.prod.Controller.PolicyController;
import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
public class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyRepository policyRepository; // Mock the PolicyRepository

    @MockBean
    private ClaimRepository claimRepository;   // Mock the ClaimRepository

    private MockHttpSession session;
    private User user;
    private User agent;
    private Policy policy1;
    private Policy policy2;
    private Policy policy3;
    private Claim claimForPolicy1;

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

        policy1 = new Policy();
        policy1.setId(101L);
        policy1.setPolicyName("HealthCare Policy");
        policy1.setAgent(agent);
        policy1.setCreationDate(LocalDate.of(2023, 1, 1));
        policy1.setValidTill(LocalDate.of(2024, 1, 1));

        policy2 = new Policy();
        policy2.setId(102L);
        policy2.setPolicyName("Life Insurance");
        policy2.setAgent(agent);
        policy2.setCreationDate(LocalDate.of(2023, 3, 15));
        policy2.setValidTill(LocalDate.of(2024, 3, 15));

        policy3 = new Policy();
        policy3.setId(103L);
        policy3.setPolicyName("Car Insurance");
        policy3.setAgent(agent);
        policy3.setCreationDate(LocalDate.of(2023, 5, 1));
        policy3.setValidTill(LocalDate.of(2024, 5, 1));

        claimForPolicy1 = new Claim();
        claimForPolicy1.setId(1L);
        claimForPolicy1.setUser(user);
        claimForPolicy1.setPolicy(policy1); // This claim is for policy1

        session = new MockHttpSession();
    }

    // --- showCreatePolicyForm Tests ---

   
    @Test
    void showCreatePolicyForm_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/create-policy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(policyRepository);
        verifyNoInteractions(claimRepository);
    }

    @Test
    void showCreatePolicyForm_loggedInUserNotAgent_redirectsToLogin() throws Exception {
        session.setAttribute("user", user); // A user, not an agent
        mockMvc.perform(get("/create-policy").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(policyRepository);
        verifyNoInteractions(claimRepository);
    }

    // --- handlePolicySubmit Tests ---

    @Test
    void handlePolicySubmit_loggedInAgent_createsPolicyAndRedirects() throws Exception {
        session.setAttribute("user", agent);
        // Mock save to return the same policy object, or just void if it's a void method
        when(policyRepository.save(any(Policy.class))).thenReturn(policy1); 

        mockMvc.perform(post("/create-policy")
                .session(session)
                .param("policyName", "New Test Policy")
                .flashAttr("policy", new Policy())) // Provide a basic policy object for @ModelAttribute
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/home"));

        // Verify that save was called with a Policy object
        verify(policyRepository, times(1)).save(argThat(policy -> {
            // Assertions to check the values set by the controller before saving
            return policy.getPolicyName().equals("New Test Policy") &&
                   policy.getAgent().equals(agent) &&
                   policy.getCreationDate().isEqual(LocalDate.now()) &&
                   policy.getValidTill().isEqual(LocalDate.now().plusYears(1));
        }));
        verifyNoInteractions(claimRepository);
    }

    @Test
    void handlePolicySubmit_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/create-policy")
                .param("policyName", "New Test Policy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(policyRepository);
        verifyNoInteractions(claimRepository);
    }

    @Test
    void handlePolicySubmit_loggedInUserNotAgent_redirectsToLogin() throws Exception {
        session.setAttribute("user", user); // A user, not an agent
        mockMvc.perform(post("/create-policy")
                .session(session)
                .param("policyName", "New Test Policy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verifyNoInteractions(policyRepository);
        verifyNoInteractions(claimRepository);
    }

    // --- showAllPolicies Tests ---

    @Test
    void showAllPolicies_loggedInUser_returnsUserPoliciesViewWithFilteredPolicies() throws Exception {
        session.setAttribute("user", user);
        List<Policy> allPolicies = Arrays.asList(policy1, policy2, policy3);
        List<Claim> userClaims = Collections.singletonList(claimForPolicy1); // User has claimed policy1

        when(policyRepository.findAll()).thenReturn(allPolicies);
        when(claimRepository.findByUserId(user.getId())).thenReturn(userClaims);

        mockMvc.perform(get("/policies").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("user-policies"))
                .andExpect(model().attributeExists("policies"))
                .andExpect(model().attributeExists("claimed"))
                .andExpect(model().attribute("policies", Arrays.asList(policy2, policy3))) // Expect policy2 and policy3
                .andExpect(model().attribute("claimed", new HashSet<>(Collections.singletonList(policy1.getId())))); // Expect claimed Policy1 ID

        verify(policyRepository, times(1)).findAll();
        verify(claimRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    void showAllPolicies_loggedInUserNoClaims_returnsAllPolicies() throws Exception {
        session.setAttribute("user", user);
        List<Policy> allPolicies = Arrays.asList(policy1, policy2, policy3);

        when(policyRepository.findAll()).thenReturn(allPolicies);
        when(claimRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList()); // No claims

        mockMvc.perform(get("/policies").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("user-policies"))
                .andExpect(model().attribute("policies", allPolicies)) // Expect all policies
                .andExpect(model().attribute("claimed", Collections.emptySet())); // Expect empty claimed set

        verify(policyRepository, times(1)).findAll();
        verify(claimRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    void showAllPolicies_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/policies"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(policyRepository);
        verifyNoInteractions(claimRepository);
    }

    @Test
    void showAllPolicies_loggedInUserNotUser_redirectsToLogin() throws Exception {
        session.setAttribute("user", agent); // An agent, not a user
        mockMvc.perform(get("/policies").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(policyRepository);
        verifyNoInteractions(claimRepository);
    }
}