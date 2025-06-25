package com.aims.prod.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aims.prod.Entity.SupportTicket;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
	List<SupportTicket> findByUserId(Long userId);
}
