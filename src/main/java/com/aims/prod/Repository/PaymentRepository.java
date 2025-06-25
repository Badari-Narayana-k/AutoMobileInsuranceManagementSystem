package com.aims.prod.Repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.prod.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	

	List<Payment> findByEmail(String email);

}
