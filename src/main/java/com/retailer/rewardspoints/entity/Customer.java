package com.retailer.rewardspoints.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="customer")
@Data
@NoArgsConstructor
public class Customer {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@NotBlank(message="Customer name is required")
	private String name;
	@NotBlank(message="Customer emailId is required")
	private String email;
	
	public Customer(@NotBlank(message = "Customer name is required") String name, @NotBlank(message = "Customer emailId is required") String email) {
		super();
		this.name = name;
		this.email = email;
	}	
}