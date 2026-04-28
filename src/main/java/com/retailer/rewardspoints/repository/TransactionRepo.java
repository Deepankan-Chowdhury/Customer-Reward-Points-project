package com.retailer.rewardspoints.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.retailer.rewardspoints.entity.Transaction;

@Repository
public interface TransactionRepo extends JpaRepository<Transaction, Long> {
	List<Transaction> findByCustomerIdAndTransactionDateBetween(long customerId, LocalDate startDate, LocalDate endDate);
}
