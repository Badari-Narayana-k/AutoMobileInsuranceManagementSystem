package com.aims.prod.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Payment {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String email;
	private String status;
	private String transactionId;
	private Long amount;
	private LocalDateTime timestamp;
	public Payment() {
		super();
	}
	public Payment( String email, String status,Long amount, String transactionId, LocalDateTime timestamp) {
		super();
		this.email = email;
		this.status = status;
		this.amount=amount;
		this.transactionId = transactionId;
		this.timestamp = timestamp;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	@Override
	public String toString() {
		return "Payment [id=" + id + ", email=" + email + ", status=" + status + ", transactionId=" + transactionId
				+ ", timestamp=" + timestamp + "]";
	}
	
	
	

}
