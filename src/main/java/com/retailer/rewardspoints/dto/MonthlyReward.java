package com.retailer.rewardspoints.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReward {
	private String month;
	private int rewardPoints;
	private double totalExpenditure;
	private List<TransactionDetails> transactions;
	private int transactionCount;
}


