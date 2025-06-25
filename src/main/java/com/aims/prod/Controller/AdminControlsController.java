package com.aims.prod.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aims.prod.Entity.User;
import com.aims.prod.Repository.UserRepository;
import com.aims.prod.Service.UserService;

import jakarta.servlet.http.HttpSession;
@Controller
public class AdminControlsController {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;

    
    @GetMapping("/admin/users")
    public String userManagement(HttpSession session,Model model) {
    	User user=(User)session.getAttribute("user");
	    if (user == null||!user.getRole().equals("admin")) return "redirect:/login";
    	List<User> userlist=userRepository.findAll();
    	userlist=userlist.stream().filter(u->u.getRole().equals("user")).toList();
    	model.addAttribute("users",userlist);
    	return "user-management";
    }
    
    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/update-role")
    public String updateUserRole(@RequestParam Long id, @RequestParam String role) {
        userService.updateRole(id, role.toLowerCase()); // Implement this in service
        return "redirect:/admin/users";
    }

}
