package com.aims.prod.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.prod.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByEmail(String email);
	List<User> findAll();
	//void deleteUser(Long id);
	Optional findById(Long id);

}
