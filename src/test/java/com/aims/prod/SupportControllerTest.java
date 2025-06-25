package com.aims.prod;

import com.aims.prod.Controller.SupportController;
import com.aims.prod.Entity.SupportTicket;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.SupportTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportController.class)
public class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SupportTicketRepository supportTicketRepository; // Mock the repository

    private MockHttpSession session;
    private User user;
    private User agent;
    private SupportTicket userTicket1;
    private SupportTicket userTicket2;
    private SupportTicket agentTicket; // Example of a ticket that might be for another user but seen by agent

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

        userTicket1 = new SupportTicket();
        userTicket1.setSubject("Issue with login");
        userTicket1.setMessage("Cannot log in to my account.");
        userTicket1.setStatus("Open");
        userTicket1.setUser(user);

        userTicket2 = new SupportTicket();
        userTicket2.setSubject("Policy update failed");
        userTicket2.setMessage("My policy details are not updating.");
        userTicket2.setStatus("Open");
        userTicket2.setUser(user);

        agentTicket = new SupportTicket();
        agentTicket.setSubject("Payment Error");
        agentTicket.setMessage("Client cannot make payment.");
        agentTicket.setStatus("Open");
        User anotherUser = new User();
        anotherUser.setId(3L);
        agentTicket.setUser(anotherUser); // This ticket belongs to another user

        session = new MockHttpSession();
    }

   

    @Test
    void showTicketForm_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/support/raise"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(supportTicketRepository);
    }

    @Test
    void showTicketForm_loggedInUserNotUser_redirectsToLogin() throws Exception {
        session.setAttribute("user", agent); // An agent, not a user
        mockMvc.perform(get("/user/support/raise").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(supportTicketRepository);
    }

   
   
   

    @Test
    void ticketHistory_loggedInUser_returnsTicketHistoryViewWithTickets() throws Exception {
        session.setAttribute("user", user);
        List<SupportTicket> userTickets = Arrays.asList(userTicket1, userTicket2);
        when(supportTicketRepository.findByUserId(user.getId())).thenReturn(userTickets);

        mockMvc.perform(get("/user/support/history").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("ticket-history"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attribute("tickets", userTickets));

        verify(supportTicketRepository, times(1)).findByUserId(user.getId());
    }

    @Test
    void ticketHistory_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/support/history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(supportTicketRepository);
    }

    @Test
    void ticketHistory_loggedInUserNotUser_redirectsToLogin() throws Exception {
        session.setAttribute("user", agent); // An agent, not a user
        mockMvc.perform(get("/user/support/history").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(supportTicketRepository);
    }

    // --- viewAllTickets Tests (/agent/support/tickets GET) ---

    @Test
    void viewAllTickets_loggedInAgent_returnsAdminTicketsViewWithAllTickets() throws Exception {
        session.setAttribute("user", agent);
        List<SupportTicket> allTickets = Arrays.asList(userTicket1, userTicket2, agentTicket);
        when(supportTicketRepository.findAll()).thenReturn(allTickets);

        mockMvc.perform(get("/agent/support/tickets").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-tickets"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attribute("tickets", allTickets));

        verify(supportTicketRepository, times(1)).findAll();
    }

    @Test
    void viewAllTickets_noSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/agent/support/tickets"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(supportTicketRepository);
    }

    @Test
    void viewAllTickets_loggedInUserNotAgent_redirectsToLogin() throws Exception {
        session.setAttribute("user", user); // A user, not an agent
        mockMvc.perform(get("/agent/support/tickets").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(supportTicketRepository);
    }

    // --- respondToTicket Tests (/admin/support/respond/{id} POST) ---

    @Test
    void respondToTicket_ticketFound_updatesTicketAndRedirects() throws Exception {
        session.setAttribute("user", agent); // Assume agent can respond (controller doesn't check role here, but good practice to have session)
        when(supportTicketRepository.findById(1L)).thenReturn(Optional.of(userTicket1));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(userTicket1);

        mockMvc.perform(post("/admin/support/respond/{id}", 1L)
                .session(session) // Although controller doesn't explicitly check session for this post, good to pass
                .param("response", "This is an admin response."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/support/tickets"));

        verify(supportTicketRepository, times(1)).findById(1L);
        verify(supportTicketRepository, times(1)).save(argThat(ticket ->
            ticket.getAdminResponse().equals("This is an admin response.") &&
            ticket.getStatus().equals("Responded")
        ));
    }

    @Test
    void respondToTicket_ticketNotFound_redirectsWithoutError() throws Exception {
        session.setAttribute("user", agent); // Assume agent can respond
        when(supportTicketRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/support/respond/{id}", 99L)
                .session(session)
                .param("response", "No such ticket response."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/agent/support/tickets"));

        verify(supportTicketRepository, times(1)).findById(99L);
        verify(supportTicketRepository, never()).save(any(SupportTicket.class)); // Save should not be called
    }
    
   
}