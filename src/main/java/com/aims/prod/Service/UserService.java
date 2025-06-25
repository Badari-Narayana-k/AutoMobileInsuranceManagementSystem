package com.aims.prod.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.aims.prod.Entity.User;
import com.aims.prod.Repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepo;


	public boolean registerUser(User user) {
		if(userRepo.findByEmail(user.getEmail())!=null)
			return false;
		String hashedPassword=BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
		user.setPassword(hashedPassword);
		user.setRole("user");
		userRepo.save(user);
		return true;
		
	}
	
	public User authenticate(String email,String password) {
		User user=userRepo.findByEmail(email);
		if(user!=null&&BCrypt.checkpw(password, user.getPassword()))
		return user;
		return null;
	}
	
	public void createAgent(User agent) {
		agent.setName(agent.getName());
		agent.setEmail(agent.getEmail());
		String hashedPassword=BCrypt.hashpw(agent.getPassword(), BCrypt.gensalt());
		agent.setPassword(hashedPassword);
		agent.setRole("agent");
		userRepo.save(agent);
	}
	
	public void updateRole(Long id,String role) {
		User user= userRepo.getById(id);
		user.setRole(role);
		userRepo.save(user);
	}
	
	

}
