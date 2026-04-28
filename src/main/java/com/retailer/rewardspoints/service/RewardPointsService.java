package com.retailer.rewardspoints.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.retailer.rewardspoints.constants.Constants;
import com.retailer.rewardspoints.controller.RewardPointController;
import com.retailer.rewardspoints.entity.Customer;
import com.retailer.rewardspoints.entity.Transaction;
import com.retailer.rewardspoints.exception.CustomerNotFoundException;
import com.retailer.rewardspoints.exception.FutureDateException;
import com.retailer.rewardspoints.exception.InvalidDateRangeException;
import com.retailer.rewardspoints.repository.CustomerRepo;
import com.retailer.rewardspoints.repository.TransactionRepo;
import com.retailer.rewardspoints.dto.MonthlyReward;
import com.retailer.rewardspoints.response.RewardPointsResponse;
import com.retailer.rewardspoints.dto.TransactionDetails;

@Service
public class RewardPointsService {

	private static final Logger logger = LoggerFactory.getLogger(RewardPointController.class);

	private final CustomerRepo customerRepository;
	private final TransactionRepo transactionRepository;

	public RewardPointsService(CustomerRepo customerRepository, TransactionRepo transactionRepository) {
		this.customerRepository = customerRepository;
		this.transactionRepository = transactionRepository;
	}

	public RewardPointsResponse calculateRewardPoints(Long customerId, Integer durationInMonths, LocalDate startDate,
			LocalDate endDate) {

		int totalRewardPoints = 0;
		int totalTransactionCount = 0;
		Double totalTransactionAmount = 0.0;

		logger.info(
				"Customer reward points calculation form service customerId: {}, total durationInMonths {}, "
						+ "transaction startDate: {}, transaction endtDate: {}",
				customerId, durationInMonths, startDate, endDate);

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException(customerId));
		logger.info("Customer forund with customerId" + customerId);

		LocalDate end = (endDate != null) ? endDate : LocalDate.now();
		LocalDate start = (startDate != null) ? startDate
				: end.minusMonths(durationInMonths != null ? durationInMonths : Constants.DEFAULT_MONTH_DURATION);

		if(start.isAfter(LocalDate.now()) || end.isAfter(LocalDate.now())) {
			throw new FutureDateException();
		}
		if (start.isAfter(end)) {
			throw new InvalidDateRangeException();
		}

		List<Transaction> transactions = getTransactions(customerId, start, end);

		Map<String, MonthlyReward> getAllRewards = getAllMonthsRewards(transactions);

		for (MonthlyReward totalRewards : getAllRewards.values()) {
			totalRewardPoints = totalRewardPoints + totalRewards.getRewardPoints();
			totalTransactionAmount = totalTransactionAmount + totalRewards.getTotalExpenditure();
			totalTransactionCount = totalTransactionCount + totalRewards.getTransactionCount();
		}
		RewardPointsResponse response = new RewardPointsResponse();
		response.setCustomerId(customerId);
		response.setCustomerName(customer.getName());
		response.setCustomerEmail(customer.getEmail());
		response.setStartDate(start.toString());
		response.setEndDate(end.toString());
		response.setMonthlyRewards(getAllRewards);
		response.setTotalTransactionCount(totalTransactionCount);
		response.setTotalTransactionAmount(totalTransactionAmount);
		response.setTotalRewardPoints(totalRewardPoints);

		return response;
	}

	private Map<String, MonthlyReward> getAllMonthsRewards(List<Transaction> transactions) {
		Map<String, MonthlyReward> allMonthsRewards = new HashMap<>();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.MONTH_FORMAT);

		for (Transaction txn : transactions) {
			String monthKey = formatter.format(txn.getTransactionDate());
			MonthlyReward monthlyRewards = allMonthsRewards.get(monthKey);
			if (monthlyRewards == null) {
				monthlyRewards = new MonthlyReward();
				monthlyRewards.setMonth(monthKey);
				monthlyRewards.setTransactions(new ArrayList<>());
				allMonthsRewards.put(monthKey, monthlyRewards);
			}
			int points=CalulateRewardPoints(txn.getAmount());
			TransactionDetails particularTxnDetails = new TransactionDetails();
			particularTxnDetails.setTransactionId(txn.getId());
			particularTxnDetails.setTransactionAmount(txn.getAmount());
			particularTxnDetails.setDate(txn.getTransactionDate().toString());
			particularTxnDetails.setDescription(txn.getTranctionDescription());
			particularTxnDetails.setRewardPoints(points);
			monthlyRewards.getTransactions().add(particularTxnDetails);
			monthlyRewards.setRewardPoints(points + monthlyRewards.getRewardPoints());
			monthlyRewards.setTransactionCount(monthlyRewards.getTransactionCount() + 1);
			monthlyRewards.setTotalExpenditure(monthlyRewards.getTotalExpenditure() + txn.getAmount());
		}
		return allMonthsRewards;
	}

	private int CalulateRewardPoints(double amount) {
		int points = 0;
		int dollars = (int) amount;
		if (dollars > 100) {
			points = points + ((dollars - 100) * 2) + 50;
		} else if (amount > 50) {
			points = (dollars - 50) * 1;
		}
		return points;
	}

	private List<Transaction> getTransactions(Long customerId, LocalDate start, LocalDate end) {
		CompletableFuture<List<Transaction>> future = CompletableFuture.supplyAsync(
				() -> transactionRepository.findByCustomerIdAndTransactionDateBetween(customerId, start, end));
		return future.join();
	}
}