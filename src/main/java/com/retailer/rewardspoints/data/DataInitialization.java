package com.retailer.rewardspoints.data;

import java.time.LocalDate;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.retailer.rewardspoints.entity.Customer;
import com.retailer.rewardspoints.entity.Transaction;
import com.retailer.rewardspoints.repository.CustomerRepo;
import com.retailer.rewardspoints.repository.TransactionRepo;

@Component
public class DataInitialization implements CommandLineRunner {
	
	private static Logger logger= LoggerFactory.getLogger(DataInitialization.class);
	
	private final CustomerRepo customerRepository;
	private final TransactionRepo transactionRepository;
	
	private DataInitialization(CustomerRepo customerRepository, TransactionRepo transactionRepository) {
		this.customerRepository = customerRepository;
		this.transactionRepository = transactionRepository;
	}

	@Override
	public void run(String... args) {
		logger.info("Loading sample data....");
		
		Customer deepankan = customerRepository.save(new Customer("Deepankan Kiron Chowdhury", "dkironchowdhury@gmail.com"));
		Customer biswarup = customerRepository.save(new Customer("Biswarup Roy", "biswarupRoy@gmail.com"));
		Customer ayush = customerRepository.save(new Customer("Ayush Shukla", "ayushMtetro@gmail.com"));
		Customer ankita = customerRepository.save(new Customer("Ankita Chwowdhury", "ankitaChowdhury@gmail.com"));
		
		LocalDate now= LocalDate.now();
		LocalDate month1 = now.minusMonths(2).withDayOfMonth(5);
		LocalDate month2 = now.minusMonths(1).withDayOfMonth(10);
		LocalDate month3 = now.minusMonths(2);
		
		transactionRepository.saveAll(Arrays.asList(
				new Transaction (deepankan, 120.00, month1, "IPhone purchase"),
				new Transaction (deepankan, 75.50, month2, "Laptop purchase"),
				new Transaction (deepankan, 200.00, month3, "Cloths shopping"),
				new Transaction (deepankan, 45.00, month3, "Books purchase"),
				new Transaction (deepankan, 150.00, month1, "Jwellary purchase"),
				
				new Transaction (biswarup, 100.00, month1, "Grocery shopping"),
				new Transaction (biswarup, 102.00, month2, "Mobile phone purchase"),
				new Transaction (biswarup, 140.00, month3, "Shoes purchase"),
				new Transaction (biswarup, 20.00, month1, "Cloths purchase"),
				new Transaction (biswarup, 10.00, month2, "Celaning material purchase"),
				
				new Transaction (ayush, 15.00, month3, "Fruits shopping"),
				new Transaction (ayush, 123.00, month2, "Birthday gift purchase"),
				new Transaction (ayush, 158.00, month2, "T-Shirt shopping"),
				new Transaction (ayush, 49.83, month1, "Appliance purchase"),
				new Transaction (ayush, 38.00, month1, "Furniture shopping"),
				
				new Transaction (ankita, 50.00, month3, "No points as 50$ shopping"),
				new Transaction (ankita, 76.00, month2, "50$ - 100$ shopping"),
				new Transaction (ankita, 100.00, month1, "52$ - 101$ shopping"),
				new Transaction (ankita, 30.00, month3, "No points as below 50$ shopping"),
				new Transaction (ankita, 206.00, month3, "Sports shoes purchase")));
		
		logger.info("Sample data loaded: {} customers, {} transactions", customerRepository.count(), transactionRepository.count());
	}
}