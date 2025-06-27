package com.aims.prod.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;
import com.aims.prod.Repository.ClaimRepository;
import com.aims.prod.Repository.PolicyRepository;
import com.aims.prod.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ClaimController {

	@Autowired
	ClaimRepository claimRepository;

	@Autowired
	PolicyRepository policyRepository;

	@Autowired
	UserRepository userRepository;

    @Value("${file.upload-dir}") 
    private String uploadDir;

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;


	 @GetMapping("/user/claims")
    public String viewClass(Model model,HttpServletRequest request,@RequestParam(required=false) Long claimId,@RequestParam(required=false) String policyName,@RequestParam(required=false) String status,@RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDateTime date) {
    	HttpSession session = request.getSession(false);
		if(session==null||session.getAttribute("user")==null) {
			return "redirect:/login";
		}
		User user=(User) session.getAttribute("user");
	    if (user == null||!user.getRole().equals("user")) return "redirect:/login";

		List<Claim> claims;
		// Simplified search logic
		if (claimId != null || (policyName != null && !policyName.isEmpty()) || (status != null && !status.isEmpty()) || date != null) {
			claims = claimRepository.searchClaims(claimId, policyName, status, date).stream()
					.filter(c -> c.getUser().getId()==(user.getId()))
					.collect(Collectors.toList());
		} else {
			claims = claimRepository.findByUserId(user.getId());
		}
		model.addAttribute("claims",claims);
	 	return "user-claims";
    }

    @GetMapping("/user/claims/request/{claimId}")
    public String showClaimSubmissionForm(@PathVariable Long claimId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("user")) {
            return "redirect:/login";
        }

        Optional<Claim> optionalClaim = claimRepository.findById(claimId);
        if (optionalClaim.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Claim not found.");
            return "redirect:/user/claims";
        }

        Claim claim = optionalClaim.get();
        if (claim.getUser().getId()!=(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to submit this claim.");
            return "redirect:/user/claims";
        }

        if (!"Premium Purchased".equals(claim.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "This claim cannot be submitted. Current status: " + claim.getStatus());
            return "redirect:/user/claims";
        }

        Policy policy = policyRepository.findById(claim.getPolicy().getId()).orElse(null);
        if (policy == null) {
            redirectAttributes.addFlashAttribute("error", "Associated policy not found.");
            return "redirect:/user/claims";
        }

        model.addAttribute("claim", claim);
        model.addAttribute("user", user);
        model.addAttribute("policy", policy);

        return "claim-submission-form";
    }

    @PostMapping("/user/claims/submit")
    public String submitClaimDetails(
            @RequestParam("claimId") Long claimId,
            @RequestParam("userPhoneNumber") String userPhoneNumber,
            @RequestParam("userAddress") String userAddress,
            @RequestParam(value = "vehicleName", required = false) String vehicleName,
            @RequestParam(value = "vehicleNumber", required = false) String vehicleNumber,
            @RequestParam("description") String description,
            @RequestParam(value = "proofImages", required = false) MultipartFile[] proofImages,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals("user")) {
            return "redirect:/login";
        }

        Optional<Claim> optionalClaim = claimRepository.findById(claimId);
        if (optionalClaim.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Claim not found.");
            return "redirect:/user/claims";
        }

        Claim claim = optionalClaim.get();
        if (claim.getUser().getId()!=(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to submit this claim.");
            return "redirect:/user/claims";
        }

        claim.setUserPhoneNumber(userPhoneNumber);
        claim.setUserAddress(userAddress);
        claim.setVehicleName(vehicleName);
        claim.setVehicleNumber(vehicleNumber);
        claim.setDescription(description);
        claim.setDate(LocalDateTime.now());

        List<String> imagePaths = new ArrayList<>();
        if (proofImages != null && proofImages.length > 0) {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                for (MultipartFile file : proofImages) {
                    if (!file.isEmpty()) {
                        
                        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                            redirectAttributes.addFlashAttribute("error", "One or more images exceed the maximum allowed size of 5MB.");
                            return "redirect:/user/claims/request/" + claimId; 
                        }

                        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                        Path filePath = uploadPath.resolve(fileName);
                        Files.copy(file.getInputStream(), filePath);
                        imagePaths.add("/uploads/" + fileName); 
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("error", "Failed to upload images. Please try again.");
                return "redirect:/user/claims/request/" + claimId; 
            }
        }
        claim.setProofImagePaths(imagePaths);
        claim.setStatus("Pending"); 
        claimRepository.save(claim);

        redirectAttributes.addFlashAttribute("message", "Claim submitted successfully! It is now pending agent review.");
        return "redirect:/user/claims";
    }


    @PostMapping("/user/claims/request/{claimId}")
    public String requestClaim(@PathVariable Long claimId, HttpSession session) {
    	Optional<Claim> optionalClaim=claimRepository.findById(claimId);
    	if(optionalClaim.isPresent()) {
    		Claim claim=optionalClaim.get();
    		claim.setStatus("Pending");
    		claimRepository.save(claim);
    	}
    	return "redirect:/user/claims";
    }



    @GetMapping("/agent/claim-requests")
    public String getAllClaimRequests(Model model,HttpSession session) {
    	User user=(User)session.getAttribute("user");
	    if (user == null||!user.getRole().equals("agent")) return "redirect:/login";
    	List<Claim> pendingClaims=claimRepository.findByStatus("Pending");
    	pendingClaims=pendingClaims.stream().filter(c->c.getPolicy().getAgent().getId()==user.getId()).toList();
    	pendingClaims.stream().filter(c->c.getId()==user.getId());
    	model.addAttribute("claims",pendingClaims);
    	return "agent-claim-requests";
    }


    @PostMapping("agent/claim/{id}/reject")
    public String rejectClaim(@PathVariable Long id,RedirectAttributes redirectAttributes) {
    	Claim claim=claimRepository.findById(id).orElse(null);
    	if(claim!=null) {
    		claim.setStatus("Rejected");
    		claim.setAgentResponseDate(LocalDateTime.now());
    		claimRepository.save(claim);

    	}
    	return "redirect:/agent/claim-requests";
    }

    @GetMapping("/agent/claim/{id}/view")
    public String viewClaimDetails(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        User agent = (User) session.getAttribute("user");
        if (agent == null || !agent.getRole().equals("agent")) {
            return "redirect:/login";
        }

        Optional<Claim> optionalClaim = claimRepository.findById(id);
        if (optionalClaim.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Claim not found.");
            return "redirect:/agent/claim-requests";
        }

        Claim claim = optionalClaim.get();
      
        if (claim.getPolicy().getAgent().getId()!=(agent.getId())) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to view this claim.");
            return "redirect:/agent/claim-requests";
        }

        model.addAttribute("claim", claim);
        model.addAttribute("proofImages", claim.getProofImagePaths()); 

        return "agent-view-claim";
    }
}
