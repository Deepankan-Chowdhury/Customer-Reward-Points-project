package com.retailer.rewardspoints.exception;

public class FutureDateException extends RuntimeException{
	public FutureDateException() {
		super("Start date or End date cannot be future date");		
	}             
}
