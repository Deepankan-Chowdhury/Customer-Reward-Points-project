package com.retailer.rewardspoints.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="transaction")
@Data
@NoArgsConstructor
public class Transaction {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name="customerId", nullable=false)
	private Customer customer;
	@NotNull(message="Amount is required")
	private Double amount;
	@NotNull(message="Transaction Date is required")
	private LocalDate transactionDate;
	private String tranctionDescription;
	
	public Transaction(Customer customer, @NotNull(message = "Amount is required") Double amount,
			@NotNull(message = "Transaction Date is required") LocalDate transactionDate, String tranctionDescription) {
		super();
		this.customer = customer;
		this.amount = amount;
		this.transactionDate = transactionDate;
		this.tranctionDescription = tranctionDescription;
	}	
}