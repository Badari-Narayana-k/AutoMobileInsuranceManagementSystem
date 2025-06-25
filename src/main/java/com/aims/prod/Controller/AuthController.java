package com.aims.prod.Controller;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PolicyRepository;
import com.aims.prod.Repository.UserRepository;
import com.aims.prod.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {


	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ClaimRepository claimRepository;
	
	@Autowired
	private PolicyRepository policyRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Value("${stripe.public.key}")
	private String stripePublicKey;
	
	@Value("${stripe.api.key}")
	private String secure;



	
	@GetMapping("/")
	public String home() {
		return "lander";
	}
	@GetMapping("/login")
	public String loginPage(HttpSession session,HttpServletResponse response) {
	    setCacheControlHeaders(response);
	    User user = (User) session.getAttribute("user");
	    if (user != null) {
	        switch (user.getRole().toUpperCase()) {
	            case "ADMIN": return "redirect:/admin/home";
	            case "AGENT": return "redirect:/agent/home";
	            default: return "redirect:/user/home";
	        }
	    }
	    return "login";
	}

	@PostMapping("/login")
	public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
	    User user = userService.authenticate(email, password);
	    if (user == null) {
	        model.addAttribute("error", "Invalid email or password");
	        return "login";
	    }
	    session.setAttribute("user", user);
	    switch (user.getRole().toLowerCase()) {
	        case "admin": return "redirect:/admin/home";
	        case "agent": return "redirect:/agent/home";
	        default: return "redirect:/user/home";
	    }
	}
	
	
	@GetMapping("/signup")
	public String signupPage(HttpSession session) {
		if(session.getAttribute("user")!=null) {
			User user=(User) session.getAttribute("user");
			switch(user.getRole().toUpperCase()) {
			case "ADMIN":return "redirect:/admin/home";
			case "AGENT":return "redirect:/agent/home";
			default :return "redirect:/user/home";
			}
		}
		return "signup";
	}
	
	@PostMapping("/signup")
	public String signup(@ModelAttribute User user,Model model) {
		if(userService.registerUser(user))
			return "redirect:/login";
		else {
			model.addAttribute("error","Email already Exist");
			return "signup";
		}
	}
	
	@GetMapping("/admin/add-agent")
	public String showAgentForm(HttpSession session,Model model) {
		User user=(User)session.getAttribute("user");
	    if (user == null||!user.getRole().equals("admin")) return "redirect:/login";
		model.addAttribute("agent",new User());
		return "admin-add-agent";
	}
	
	@PostMapping("/admin/add-agent")
	public String addAgent(@ModelAttribute User agent,RedirectAttributes redirectAttributes,Model model) {
		agent.setRole("agent");
		userService.createAgent(agent);
		redirectAttributes.addFlashAttribute("message","Agent Created Successfully!");
		model.addAttribute("message","Agent Created Successfully!");
		return "redirect:/admin/add-agent";
	}
	
	
	@GetMapping("/logout")
	public String logout(HttpSession session,Model model) {
		session.invalidate();
		return "redirect:/login";
	}
	
	

	@GetMapping("/user/home")
	public String userHome(HttpSession session, Model model, HttpServletResponse response) {
	    setCacheControlHeaders(response);
	    User user = (User) session.getAttribute("user");
	    if (user == null||!user.getRole().equals("user")) return "redirect:/login";

	    model.addAttribute("name", user.getName());
	    List<Policy> policies = policyRepository.findAll();
		// Filter policies to only show those not yet purchased by the user
		List<Claim> userClaims = claimRepository.findByUser(user);
		Set<Long> purchasedPolicyIds = userClaims.stream()
				.filter(claim -> "Premium Purchased".equals(claim.getStatus()))
				.map(claim -> claim.getPolicy().getId())
				.collect(Collectors.toSet());
		
		policies = policies.stream()
				.filter(policy -> !purchasedPolicyIds.contains(policy.getId()))
				.collect(Collectors.toList());

	    model.addAttribute("policies", policies);
	    return "user-home";
	}

	@GetMapping("/agent/home")
	public String agentHome(HttpSession session, Model model, HttpServletResponse response) {
	    User user = (User) session.getAttribute("user");
	    if (user == null||!user.getRole().equals("agent")) return "redirect:/login";

	    setCacheControlHeaders(response);
	    model.addAttribute("name", user.getName());
	    List<Claim> allclaims = claimRepository.findAll();
	    List<Policy> itr = policyRepository.findByAgent(user);
	    List<Long> ids = new ArrayList<>();
	    for (Policy i : itr) {
	        ids.add(i.getId());
	    }
	    allclaims = allclaims.stream().filter(c -> ids.contains(c.getPolicy().getId())).toList();
	    model.addAttribute("allclaims", allclaims);
	    List<Policy> policies = policyRepository.findByAgent(user);
	    policies = policies.stream().filter(c -> c.getAgent().getId() == user.getId()).toList();
	    List<Claim> pClaims = claimRepository.findByStatus("Pending");
	    pClaims = pClaims.stream().filter(c -> c.getPolicy().getAgent().getId() == user.getId()).toList();
	    model.addAttribute("claims", pClaims);
	    model.addAttribute("policies", policies);
	    return "agent-home";
	}

	@GetMapping("/admin/home")
	public String adminHome(HttpSession session, Model model, HttpServletResponse response) {
	    User user = (User) session.getAttribute("user");
	    if (user == null||!user.getRole().equals("admin")) return "redirect:/login";
	    setCacheControlHeaders(response);
	    model.addAttribute("name", user.getName());
	    List<User> users = userRepository.findAll();
	    model.addAttribute("users", users);
	    List<Claim> claims = claimRepository.findAll();
	    model.addAttribute("claims", claims);
	    List<Policy> policies = policyRepository.findAll();
	    model.addAttribute("policies", policies);
	    return "admin-home";
	}

	private void setCacheControlHeaders(HttpServletResponse response) {
	    response.setHeader("Cache-Control", "no-cache,no-store,must-revalidate");
	    response.setHeader("Pragma", "no-cache");
	    response.setDateHeader("Expires", 0);
	}

	
	@GetMapping("/profile")
	public String userProfile(HttpServletRequest request,Model model) {
		HttpSession session = request.getSession(false);
		if(session==null||session.getAttribute("user")==null) {
			return "redirect:/login";
		}
		User user=(User) session.getAttribute("user");
		model.addAttribute("user",user);
		List<Claim> claims=claimRepository.findByUserId(user.getId());
		model.addAttribute("claims",claims);
		List<Policy> policies=policyRepository.findByAgent(user);
		model.addAttribute("policies",policies);
		return "profile";
		
	}
	
    
	
}