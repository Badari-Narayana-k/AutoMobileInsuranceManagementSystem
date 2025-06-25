package com.aims.prod.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aims.prod.Entity.SupportTicket;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.SupportTicketRepository;

import jakarta.servlet.http.HttpSession;
@Controller
public class SupportController {
	
	@Autowired
	SupportTicketRepository supportTicketRepository;

	
	@GetMapping("/user/support/raise")
    public String showTicketForm(HttpSession session,Model model) {
    	User user=(User)session.getAttribute("user");
	    if (user == null||!user.getRole().equals("user")) return "redirect:/login";

    	model.addAttribute("ticket",new SupportTicket());
    	return "raise-ticket";
    }
    
    @PostMapping("/user/support/raise")
    public String raiseTicket(@ModelAttribute SupportTicket ticket,HttpSession session) {
    	User user=(User) session.getAttribute("user");
    	ticket.setUser(user);
    	supportTicketRepository.save(ticket);
    	return "redirect:/user/support/history";
    }
    @GetMapping("/user/support/history")
    public String ticketHistory(Model model,HttpSession session) {
    	User user=(User) session.getAttribute("user");
	    if (user == null||!user.getRole().equals("user")) return "redirect:/login";
    	List<SupportTicket> tickets=supportTicketRepository.findByUserId(user.getId());
    	model.addAttribute("tickets",tickets);
    	return "ticket-history";
    }
    
    @GetMapping("/agent/support/tickets")
    public String viewAllTickets(HttpSession session,Model model) {
    	User user=(User) session.getAttribute("user");
	    if (user == null||!user.getRole().equals("agent")) return "redirect:/login";
    	List<SupportTicket> tickets=supportTicketRepository.findAll();
    	model.addAttribute("tickets",tickets);
    	return "admin-tickets";
    }
    
    @PostMapping("/admin/support/respond/{id}")
    public String respondToticket(@PathVariable Long id, @RequestParam String response) {
    	Optional<SupportTicket> ticketOpt=supportTicketRepository.findById(id);
    	if(ticketOpt.isPresent()) {
    		SupportTicket ticket=ticketOpt.get();
    		ticket.setAdminResponse(response);
    		ticket.setStatus("Responded");
    		supportTicketRepository.save(ticket);
    	}
    	return "redirect:/agent/support/tickets";
    }
    
}
