package com.aims.prod.Controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PolicyRepository;

import jakarta.servlet.http.HttpSession;
@Controller
public class PolicyController {

	@Autowired
	PolicyRepository policyRepository;
	
	@Autowired
	ClaimRepository claimRepository;
	
	@GetMapping("/create-policy")
	public String showCreatePolicyForm(HttpSession session,Model model) {
		User agent=(User) session.getAttribute("user");
		if(agent==null||!"agent".equals(agent.getRole())) {
			return "redirect:/login";
		}
		model.addAttribute("policy",new Policy());
		return "create-policy";
	}
	
	@PostMapping("/create-policy")
	public String handlePolicySubmit(@ModelAttribute Policy policy,HttpSession session) {
		User agent=(User) session.getAttribute("user");
		if(agent==null||!"agent".equals(agent.getRole())) {
			return "redirect:/login";
		}
		policy.setAgent(agent);
		policy.setCreationDate(LocalDate.now());
		policy.setValidTill(policy.getCreationDate().plusYears(1));
		policyRepository.save(policy);
		return "redirect:/agent/home";
	}
	
	@GetMapping("/policies")
	public String showAllPolicies(HttpSession session,Model model) {
		User user=(User) session.getAttribute("user");
		if(user==null||!"user".equals(user.getRole())) {
			return "redirect:/login";
		}
		List<Policy> policies=policyRepository.findAll();
		List<Claim> userClaims=claimRepository.findByUserId(user.getId());
		Set<Long> claimedPolicies=userClaims.stream().map(c->c.getPolicy().getId()).collect(Collectors.toSet());
		List<Policy> remaining=new ArrayList<>();
		for(Policy p: policies) {
			if(!claimedPolicies.contains(p.getId())) {
				remaining.add(p);
			}
		}
		model.addAttribute("policies",remaining);
		model.addAttribute("claimed",claimedPolicies);
		return "user-policies";
	}
}
