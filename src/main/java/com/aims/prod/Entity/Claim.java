package com.aims.prod.Entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection; // Added for List<String>
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="claims")
public class Claim {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column(name="policy_name", nullable=false)
	private String policyName;
	
	@Column(name="status",nullable=false)
	private String status="Premium Purchased"; // Changed default status
	
	@Column(name="claim_date",nullable=false)
	private LocalDateTime date;
	
	@ManyToOne
	@JoinColumn(name="user_id",nullable=false)
	private User user;
	
	@Column(name="agent_response_date")
	private LocalDateTime agentResponseDate;
	
	private String paymentIntentId;
	
	private String refundId;

	// New fields for claim submission form
	private String userPhoneNumber;
	private String userAddress;
	private String vehicleName;
	private String vehicleNumber;

	@Column(length = 2000) // Increased length for description
	private String description;

	@ElementCollection // To store a collection of strings (image paths)
	private List<String> proofImagePaths;
	
	
	public String getRefundId() {
		return refundId;
	}


	public void setRefundId(String refundId) {
		this.refundId = refundId;
	}


	public String getPaymentIntentId() {
		return paymentIntentId;
	}


	public void setPaymentIntentId(String paymentIntentId) {
		this.paymentIntentId = paymentIntentId;
	}


	public LocalDateTime getAgentResponseDate() {
		return agentResponseDate;
	}


	public void setAgentResponseDate(LocalDateTime agentResponseDate) {
		this.agentResponseDate = agentResponseDate;
	}


	public Policy getPolicy() {
		return policy;
	}
	

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	@ManyToOne
	@JoinColumn(name="policy_id",nullable = false)
	private Policy policy;
	
	public Claim() {}

	// Updated constructor to reflect new fields, if needed. Assuming default constructor is used mostly.
	public Claim(String policyName, String status, LocalDateTime date, User user) {
		super();
		this.policyName = policyName;
		this.status = status;
		this.date = date;
		this.user = user;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	// New Getters and Setters for added fields
	public String getUserPhoneNumber() {
		return userPhoneNumber;
	}

	public void setUserPhoneNumber(String userPhoneNumber) {
		this.userPhoneNumber = userPhoneNumber;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getProofImagePaths() {
		return proofImagePaths;
	}

	public void setProofImagePaths(List<String> proofImagePaths) {
		this.proofImagePaths = proofImagePaths;
	}
}