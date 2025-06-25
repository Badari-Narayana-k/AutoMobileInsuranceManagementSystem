package com.aims.prod.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aims.prod.Entity.Claim;
import com.aims.prod.Entity.User;

public interface ClaimRepository extends JpaRepository<Claim,Long> {
	
	List<Claim> findAll();
	List<Claim> findByUser(User user);
	List<Claim> findByUserId(Long userId);
	List<Claim> findByStatus(String string);
	@Query("SELECT c FROM Claim c WHERE "+"(:claimId is NULL OR c.id= :claimId) AND "+"(:policyName IS NULL OR LOWER(c.policyName) LIKE LOWER(CONCAT('%', :policyName, '%'))) AND "+ "(:status IS NULL OR LOWER(c.status)=LOWER(:status)) AND " + "(:date IS NULL OR DATE(c.date)= :date)")
	List<Claim> searchClaims(@Param("claimId") Long claimId,@Param("policyName") String policyName,@Param("status") String status,@Param("date") LocalDateTime date);
}
