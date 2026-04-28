package com.retailer.rewardspoints.exception;

public class CustomerNotFoundException extends RuntimeException {
	public CustomerNotFoundException(Long customerId) {
		super("No such customer exists with ID: {}" + customerId);
	}
}
