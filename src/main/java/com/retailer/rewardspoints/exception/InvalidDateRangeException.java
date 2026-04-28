package com.retailer.rewardspoints.exception;

public class InvalidDateRangeException extends RuntimeException {
	public InvalidDateRangeException() {	
	super("End date cannot be before start date");
}
}

