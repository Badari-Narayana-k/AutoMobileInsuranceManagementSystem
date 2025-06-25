package com.aims.prod;

import com.aims.prod.AutoInsuranceManagementSystem2Application;
import com.aims.prod.Controller.AuthController;
import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PaymentRepository;
import com.aims.prod.Repository.PolicyRepository;
import com.aims.prod.Repository.SupportTicketRepository;
import com.aims.prod.Repository.UserRepository;
import com.aims.prod.Service.MailService;
import com.aims.prod.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AutoInsuranceManagementSystem2Application autoInsuranceManagementSystem2Application;

    @MockBean
    private MailService mailService;

    @MockBean
    private UserService userService;

    @MockBean
    private ClaimRepository claimRepository;

    @MockBean
    private PolicyRepository policyRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @MockBean
    private SupportTicketRepository supportTicketRepository;
    
    private MockHttpSession session;
    private User regularUser;
    private User agentUser;
    private User adminUser;
    private Policy policy1;
    private Policy policy2;
    private Claim claim1;
    private Claim claim2;

    @BeforeEach
    void setUp() {
        regularUser = new User();
        regularUser.setId(1L);
        regularUser.setName("Test User");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword("password"); // Not sensitive in tests
        regularUser.setRole("user");

        agentUser = new User();
        agentUser.setId(2L);
        agentUser.setName("Test Agent");
        agentUser.setEmail("agent@example.com");
        agentUser.setPassword("password");
        agentUser.setRole("agent");

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setName("Test Admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRole("admin");

        policy1 = new Policy();
        policy1.setId(101L);
        policy1.setPolicyName("Health Policy");
        policy1.setAgent(agentUser); // Link policy to agent

        policy2 = new Policy();
            policy2.setId(102L);
            policy2.setPolicyName("Life Policy");
            policy2.setAgent(agentUser); // Link policy to agent

        claim1 = new Claim();
        claim1.setId(201L);
        claim1.setUser(regularUser);
        claim1.setPolicy(policy1);
        claim1.setStatus("Pending");

        claim2 = new Claim();
        claim2.setId(202L);
        claim2.setUser(regularUser);
        claim2.setPolicy(policy2);
        claim2.setStatus("Approved");

        session = new MockHttpSession();
    }

    // --- / GET home ---
    @Test
    void home_returnsLanderView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("lander"));
    }

    // --- /login GET loginPage ---
    @Test
    void loginPage_noUserInSession_returnsLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(header().string("Cache-Control", "no-cache,no-store,must-revalidate")); // Verify headers
    }

    @Test
    void loginPage_userAlreadyLoggedIn_redirectsToUserHome() throws Exception {
        session.setAttribute("user", regularUser);
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/home"));
    }

    @Test
    void loginPage_agentAlreadyLoggedIn_redirectsToAgentHome() throws Exception {
        session.setAttribute("user", agentUser);
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/home"));
    }

    @Test
    void loginPage_adminAlreadyLoggedIn_redirectsToAdminHome() throws Exception {
        session.setAttribute("user", adminUser);
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/home"));
    }

    // --- /login POST login ---
    @Test
    void login_validUserCredentials_redirectsToUserHome() throws Exception {
        when(userService.authenticate("user@example.com", "password")).thenReturn(regularUser);

        mockMvc.perform(post("/login")
                .param("email", "user@example.com")
                .param("password", "password")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/home"));

        verify(userService, times(1)).authenticate("user@example.com", "password");
        // Verify user is set in session
        assert session.getAttribute("user") == regularUser;
    }

    @Test
    void login_validAgentCredentials_redirectsToAgentHome() throws Exception {
        when(userService.authenticate("agent@example.com", "password")).thenReturn(agentUser);

        mockMvc.perform(post("/login")
                .param("email", "agent@example.com")
                .param("password", "password")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/home"));

        verify(userService, times(1)).authenticate("agent@example.com", "password");
        assert session.getAttribute("user") == agentUser;
    }

    @Test
    void login_validAdminCredentials_redirectsToAdminHome() throws Exception {
        when(userService.authenticate("admin@example.com", "password")).thenReturn(adminUser);

        mockMvc.perform(post("/login")
                .param("email", "admin@example.com")
                .param("password", "password")
                .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/home"));

        verify(userService, times(1)).authenticate("admin@example.com", "password");
        assert session.getAttribute("user") == adminUser;
    }

    @Test
    void login_invalidCredentials_returnsLoginPageWithError() throws Exception {
        when(userService.authenticate(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/login")
                .param("email", "wrong@example.com")
                .param("password", "wrongpass")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Invalid email or password"));

        verify(userService, times(1)).authenticate("wrong@example.com", "wrongpass");
        assert session.getAttribute("user") == null;
    }

    // --- /signup GET signupPage ---
    @Test
    void signupPage_noUserInSession_returnsSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void signupPage_userAlreadyLoggedIn_redirectsToUserHome() throws Exception {
        session.setAttribute("user", regularUser);
        mockMvc.perform(get("/signup").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/home"));
    }

    // --- /signup POST signup ---
    @Test
    void signup_successfulRegistration_redirectsToLogin() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(true);

        mockMvc.perform(post("/signup")
                .param("email", "newuser@example.com")
                .param("password", "newpass")
                .flashAttr("user", new User())) // Provide a basic user object for @ModelAttribute
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    @Test
    void signup_emailAlreadyExists_returnsSignupPageWithError() throws Exception {
        when(userService.registerUser(any(User.class))).thenReturn(false);

        mockMvc.perform(post("/signup")
                .param("email", "existing@example.com")
                .param("password", "pass")
                .flashAttr("user", new User()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Email already Exist"));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    // --- /admin/add-agent GET showAgentForm ---
    
    @Test
    void showAgentForm_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/add-agent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(userService);
    }

    @Test
    void showAgentForm_loggedInUserNotAdmin_redirectsToLogin() throws Exception {
        session.setAttribute("user", regularUser);
        mockMvc.perform(get("/admin/add-agent").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(userService);
    }

   
    

    
    // --- /logout GET logout ---
    @Test
    void logout_invalidatesSessionAndRedirectsToLogin() throws Exception {
        session.setAttribute("user", regularUser); // Put something in session to invalidate

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        
        // Verify session is invalidated
        assert session.isInvalid();
    }

    // --- /user/home GET userHome ---
    @Test
    void userHome_loggedInUser_returnsUserHomeView() throws Exception {
        session.setAttribute("user", regularUser);
        List<Policy> allPolicies = Arrays.asList(policy1, policy2);
        when(policyRepository.findAll()).thenReturn(allPolicies);

        mockMvc.perform(get("/user/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("user-home"))
                .andExpect(model().attribute("name", regularUser.getName()))
                .andExpect(model().attribute("policies", allPolicies))
                .andExpect(header().string("Cache-Control", "no-cache,no-store,must-revalidate"));
        
        verify(policyRepository, times(1)).findAll();
    }

    @Test
    void userHome_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(policyRepository);
    }

    @Test
    void userHome_loggedInUserNotUser_redirectsToLogin() throws Exception {
        session.setAttribute("user", agentUser); // Agent trying to access user home
        mockMvc.perform(get("/user/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(policyRepository);
    }

    // --- /agent/home GET agentHome ---
    @Test
    void agentHome_loggedInAgent_returnsAgentHomeView() throws Exception {
        session.setAttribute("user", agentUser);
        List<Policy> agentPolicies = Arrays.asList(policy1, policy2); // Policies linked to agentUser
        List<Claim> allClaims = Arrays.asList(claim1, claim2); // Claims linked to policy1 and policy2
        List<Claim> pendingClaims = Collections.singletonList(claim1); // Claim1 is pending

        when(claimRepository.findAll()).thenReturn(allClaims);
        when(policyRepository.findByAgent(agentUser)).thenReturn(agentPolicies);
        when(claimRepository.findByStatus("Pending")).thenReturn(pendingClaims);

        mockMvc.perform(get("/agent/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("agent-home"))
                .andExpect(model().attribute("name", agentUser.getName()))
                .andExpect(model().attribute("allclaims", Arrays.asList(claim1, claim2))) // all claims relevant to agent's policies
                .andExpect(model().attribute("claims", Collections.singletonList(claim1))) // pending claims relevant to agent's policies
                .andExpect(model().attribute("policies", Arrays.asList(policy1, policy2))) // policies managed by agent
                .andExpect(header().string("Cache-Control", "no-cache,no-store,must-revalidate"));

        verify(claimRepository, times(1)).findAll();
        verify(policyRepository, times(2)).findByAgent(agentUser); // Called twice in controller
        verify(claimRepository, times(1)).findByStatus("Pending");
    }

    @Test
    void agentHome_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/agent/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void agentHome_loggedInUserNotAgent_redirectsToLogin() throws Exception {
        session.setAttribute("user", regularUser); // Regular user trying to access agent home
        mockMvc.perform(get("/agent/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
        verifyNoInteractions(policyRepository);
    }

    // --- /admin/home GET adminHome ---
    @Test
    void adminHome_loggedInAdmin_returnsAdminHomeView() throws Exception {
        session.setAttribute("user", adminUser);
        List<User> allUsers = Arrays.asList(regularUser, agentUser, adminUser);
        List<Claim> allClaims = Arrays.asList(claim1, claim2);
        List<Policy> allPolicies = Arrays.asList(policy1, policy2);

        when(userRepository.findAll()).thenReturn(allUsers);
        when(claimRepository.findAll()).thenReturn(allClaims);
        when(policyRepository.findAll()).thenReturn(allPolicies);

        mockMvc.perform(get("/admin/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-home"))
                .andExpect(model().attribute("name", adminUser.getName()))
                .andExpect(model().attribute("users", allUsers))
                .andExpect(model().attribute("claims", allClaims))
                .andExpect(model().attribute("policies", allPolicies))
                .andExpect(header().string("Cache-Control", "no-cache,no-store,must-revalidate"));

        verify(userRepository, times(1)).findAll();
        verify(claimRepository, times(1)).findAll();
        verify(policyRepository, times(1)).findAll();
    }

    @Test
    void adminHome_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(claimRepository);
        verifyNoInteractions(policyRepository);
    }

    @Test
    void adminHome_loggedInUserNotAdmin_redirectsToLogin() throws Exception {
        session.setAttribute("user", regularUser); // Regular user trying to access admin home
        mockMvc.perform(get("/admin/home").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(claimRepository);
        verifyNoInteractions(policyRepository);
    }

    // --- /profile GET userProfile ---
    @Test
    void userProfile_loggedInUser_returnsProfileView() throws Exception {
        session.setAttribute("user", regularUser);
        List<Claim> userClaims = Arrays.asList(claim1, claim2); // Claims linked to regularUser
        when(claimRepository.findByUserId(regularUser.getId())).thenReturn(userClaims);
        // User is not an agent, so policyRepository.findByAgent will return empty list
        when(policyRepository.findByAgent(any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", regularUser))
                .andExpect(model().attribute("claims", userClaims))
                .andExpect(model().attribute("policies", Collections.emptyList())); // No policies for regular user
        
        verify(claimRepository, times(1)).findByUserId(regularUser.getId());
        verify(policyRepository, times(1)).findByAgent(regularUser);
    }

    @Test
    void userProfile_loggedInAgent_returnsProfileViewWithAgentPolicies() throws Exception {
        session.setAttribute("user", agentUser);
        List<Claim> agentClaims = Collections.emptyList(); // Assuming an agent doesn't have claims as a user
        List<Policy> agentPolicies = Arrays.asList(policy1, policy2); // Policies managed by agent

        when(claimRepository.findByUserId(agentUser.getId())).thenReturn(agentClaims);
        when(policyRepository.findByAgent(agentUser)).thenReturn(agentPolicies);

        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", agentUser))
                .andExpect(model().attribute("claims", agentClaims))
                .andExpect(model().attribute("policies", agentPolicies)); // Agent's policies
        
        verify(claimRepository, times(1)).findByUserId(agentUser.getId());
        verify(policyRepository, times(1)).findByAgent(agentUser);
    }


    @Test
    void userProfile_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(claimRepository);
        verifyNoInteractions(policyRepository);
    }
}