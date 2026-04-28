package com.retailer.rewardspoints.response;

import java.util.Map;

import com.retailer.rewardspoints.dto.MonthlyReward;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardPointsResponse {	
	private long customerId;
	private String customerName;
	private String customerEmail;
	private Map<String, MonthlyReward> monthlyRewards;
	private int totalTransactionCount;
	private double totalTransactionAmount;
	private int totalRewardPoints;
	private String startDate;
	private String endDate;
}
