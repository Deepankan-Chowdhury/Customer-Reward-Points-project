package com.retailer.rewardspoints.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.retailer.rewardspoints.entity.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Long> {}
