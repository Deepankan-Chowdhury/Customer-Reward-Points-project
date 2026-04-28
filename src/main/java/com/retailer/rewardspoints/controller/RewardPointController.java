package com.retailer.rewardspoints.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.retailer.rewardspoints.response.RewardPointsResponse;
import com.retailer.rewardspoints.service.RewardPointsService;

@RestController
@RequestMapping("/api")
public class RewardPointController {

	private static final Logger logger = LoggerFactory.getLogger(RewardPointController.class);

	private final RewardPointsService service;

	public RewardPointController(RewardPointsService service) {
		this.service = service;
	}

	@GetMapping("/rewardPoints/{customerId}")
	public ResponseEntity<RewardPointsResponse> getRewardPoints(@PathVariable Long customerId,
			@RequestParam(required = false, defaultValue = "3") Integer durationInMonths,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

		logger.info("Customer reward points calculation for /GET customerId: {}, total durationInMonths {}, "
						+ "transaction startDate: {}, transaction endtDate: {}",
						customerId, durationInMonths, startDate, endDate);

		RewardPointsResponse response = service.calculateRewardPoints(customerId, durationInMonths, startDate, endDate);
		return ResponseEntity.ok(response);
	}
}
