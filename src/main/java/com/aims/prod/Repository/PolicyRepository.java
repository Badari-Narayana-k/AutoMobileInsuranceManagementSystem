package com.aims.prod.Repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.prod.Entity.Policy;
import com.aims.prod.Entity.User;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
	
	List<Policy> findByAgent(User agent);
	List<Policy> findAll();

}
