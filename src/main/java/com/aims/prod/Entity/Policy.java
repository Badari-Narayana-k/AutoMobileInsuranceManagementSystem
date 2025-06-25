package com.aims.prod.Entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="policies")
public class Policy {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	private String policyName;
	private String vehicleType;
	private double price;
	private double coverageRate;
	 private String policyType;
	
	public String getPolicyType() {
		return policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}

	@Column(length=1000)
	private String description;
	
	private LocalDate creationDate;
	private LocalDate validTill;
	
	@ManyToOne
	@JoinColumn(name="agent_id")
	private User agent;
	
	
	

	public Policy() {
		super();
	}

	public Policy(String policyName, String vehicleType, double price, double coverageRate, String description,
			LocalDate creationDate, LocalDate validTill) {
		super();
		this.policyName = policyName;
		this.vehicleType = vehicleType;
		this.price = price;
		this.coverageRate = coverageRate;
		this.description = description;
		this.creationDate = creationDate;
		this.validTill = validTill;
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

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getCoverageRate() {
		return coverageRate;
	}

	public void setCoverageRate(double coverageRate) {
		this.coverageRate = coverageRate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public User getAgent() {
		return agent;
	}

	public void setAgent(User agent) {
		this.agent = agent;
	}
	
	
	

}
