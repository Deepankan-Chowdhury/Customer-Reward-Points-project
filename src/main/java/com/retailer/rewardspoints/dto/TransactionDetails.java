package com.retailer.rewardspoints.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetails {
	private Long transactionId;
	private String date;
	private int rewardPoints;
	private String description;
	private double transactionAmount;	
}
