package com.retailer.rewardspoints.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.retailer.rewardspoints.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	Logger logger=LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException e){
		logger.error("Customer not found for the given customer Id.: {}" +e);
		ErrorResponse response= new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not found", e.getMessage(), LocalDateTime.now());		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}
	
	@ExceptionHandler(InvalidDateRangeException.class)
	public ResponseEntity<ErrorResponse> handleIlligalDateRange(InvalidDateRangeException e){
		logger.error("End date cannot be before start date.: {}" +e);
		ErrorResponse response= new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invaild date range", e.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		
	}
	@ExceptionHandler(FutureDateException.class)
	public ResponseEntity<ErrorResponse> handleFutureDate(FutureDateException e){
		logger.error("Start date or End date cannot be future date.: {}" +e);
		ErrorResponse response= new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Future date error", e.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		
	}
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e){
		logger.error("Invalid parameter.:" +e);
		ErrorResponse response= new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid parameter", e.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception e){
		logger.error("Unexpeted error occured.: {}" +e);
		ErrorResponse response= new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error", e.getMessage(), LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);		
	}
}