package com.reatailer.rewardspoints.service;

import com.retailer.rewardspoints.entity.Customer;
import com.retailer.rewardspoints.entity.Transaction;
import com.retailer.rewardspoints.exception.CustomerNotFoundException;
import com.retailer.rewardspoints.exception.FutureDateException;
import com.retailer.rewardspoints.exception.InvalidDateRangeException;
import com.retailer.rewardspoints.repository.CustomerRepo;
import com.retailer.rewardspoints.repository.TransactionRepo;
import com.retailer.rewardspoints.dto.MonthlyReward;
import com.retailer.rewardspoints.response.RewardPointsResponse;
import com.retailer.rewardspoints.service.RewardPointsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardPointsServiceTest {

    @Mock
    private CustomerRepo customerRepository;

    @Mock
    private TransactionRepo transactionRepository;

    @InjectMocks
    private RewardPointsService service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("Alice Smith", "alice@example.com");
        customer.setId(1L);
    }
    
    private Transaction buildTransaction(Long id, double amount, LocalDate date, String desc) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setAmount(amount);
        t.setTransactionDate(date);
        t.setTranctionDescription(desc);
        return t;
    }

    private void stubCustomerFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
    }

    private void stubTransactions(List<Transaction> txns) {
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(txns);
    }

    @Test
    @DisplayName("Throws CustomerNotFoundException when customer ID does not exist")
    void calculateRewardPoints_unknownCustomer_throwsCustomerNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculateRewardPoints(99L, 3, null, null))
                .isInstanceOf(CustomerNotFoundException.class);

        verifyNoInteractions(transactionRepository);
    }
    
    @Test
    @DisplayName("Throws InvalidDateRangeException when startDate is strictly after endDate")
    void calculateRewardPoints_startAfterEnd_throwsIlligalDateRangeException() {
        stubCustomerFound();

        assertThatThrownBy(() ->
                service.calculateRewardPoints(1L, 3,
                        LocalDate.of(2024, 6, 1),
                        LocalDate.of(2024, 1, 1)))
                .isInstanceOf(InvalidDateRangeException.class);

        verifyNoInteractions(transactionRepository);
    }
    
    @Test
    @DisplayName("Throws FutureDateException when startDate or endDate is in future date")
    void calculateRewardPoints_startAOrEnd_throwsFututreDateException() {
        stubCustomerFound();

        assertThatThrownBy(() ->
                service.calculateRewardPoints(1L, 3,
                        LocalDate.of(2027, 6, 1),
                        LocalDate.of(2026, 1, 12)))
                .isInstanceOf(FutureDateException.class);

        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("Throws InvalidDateRangeException when startDate is one day after endDate")
    void calculateRewardPoints_startOneDayAfterEnd_throwsIlligalDateRangeException() {
        stubCustomerFound();

        LocalDate end = LocalDate.of(2024, 3, 31);

        assertThatThrownBy(() ->
                service.calculateRewardPoints(1L, 3, end.plusDays(1), end))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    @DisplayName("Does NOT throw when startDate equals endDate (same-day range is valid)")
    void calculateRewardPoints_startEqualsEnd_doesNotThrow() {
        stubCustomerFound();
        LocalDate date = LocalDate.of(2024, 3, 15);
        stubTransactions(Collections.emptyList());

        assertThatCode(() ->
                service.calculateRewardPoints(1L, 3, date, date))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does NOT throw when leap year i.e; 2024-02-29 is passed")
    void calculateRewardPoints_LeapYear_doesNotThrow() {
        stubCustomerFound();
        LocalDate date = LocalDate.of(2024, 2, 29);
        stubTransactions(Collections.emptyList());

        assertThatCode(() -> service.calculateRewardPoints(1L, 3, date, date)).doesNotThrowAnyException();
    }
    	
    @Test
    @DisplayName("Both dates null: end defaults to today, start defaults to today minus durationInMonths")
    void calculateRewardPoints_bothDatesNull_defaultsToTodayMinusDuration() {
        stubCustomerFound();
        stubTransactions(Collections.emptyList());

        service.calculateRewardPoints(1L, 3, null, null);

        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                1L,
                LocalDate.now().minusMonths(3),
                LocalDate.now());
    }

    @Test
    @DisplayName("Both dates null with duration 6: uses 6-month window")
    void calculateRewardPoints_bothDatesNullDuration6_usesSixMonthWindow() {
        stubCustomerFound();
        stubTransactions(Collections.emptyList());

        service.calculateRewardPoints(1L, 6, null, null);

        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                1L,
                LocalDate.now().minusMonths(6),
                LocalDate.now());
    }

    @Test
    @DisplayName("durationInMonths is null: falls back to DEFAULT_MONTH_DURATION (3)")
    void calculateRewardPoints_durationNull_usesConstantDefault3() {
        stubCustomerFound();
        stubTransactions(Collections.emptyList());

        service.calculateRewardPoints(1L, null, null, null);

        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                1L,
                LocalDate.now().minusMonths(3),
                LocalDate.now());
    }

    @Test
    @DisplayName("Only endDate provided: start defaults to endDate minus durationInMonths")
    void calculateRewardPoints_onlyEndDateProvided_startDefaultsFromEnd() {
        stubCustomerFound();
        LocalDate end = LocalDate.of(2024, 3, 31);
        stubTransactions(Collections.emptyList());

        service.calculateRewardPoints(1L, 3, null, end);

        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                1L, end.minusMonths(3), end);
    }

    @Test
    @DisplayName("Only startDate provided: end defaults to today")
    void calculateRewardPoints_onlyStartDateProvided_endDefaultsToToday() {
        stubCustomerFound();
        LocalDate start = LocalDate.of(2024, 1, 1);
        stubTransactions(Collections.emptyList());

        service.calculateRewardPoints(1L, 3, start, null);

        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                1L, start, LocalDate.now());
    }

    @Test
    @DisplayName("Both dates provided explicitly: uses them as-is without modification")
    void calculateRewardPoints_bothDatesExplicit_usesThemDirectly() {
        stubCustomerFound();
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end   = LocalDate.of(2024, 3, 31);
        stubTransactions(Collections.emptyList());

        service.calculateRewardPoints(1L, 3, start, end);

        verify(transactionRepository)
                .findByCustomerIdAndTransactionDateBetween(1L, start, end);
    }

    @Test
    @DisplayName("Response contains correct customer name and email")
    void calculateRewardPoints_responseHasCorrectCustomerInfo() {
        stubCustomerFound();
        stubTransactions(Collections.emptyList());

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);

        assertThat(response.getCustomerId()).isEqualTo(1L);
        assertThat(response.getCustomerName()).isEqualTo("Alice Smith");
        assertThat(response.getCustomerEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Response totals are all zero when there are no transactions")
    void calculateRewardPoints_noTransactions_allTotalsAreZero() {
        stubCustomerFound();
        stubTransactions(Collections.emptyList());

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);

        assertThat(response.getTotalRewardPoints()).isZero();
        assertThat(response.getTotalTransactionAmount()).isZero();
        assertThat(response.getTotalTransactionCount()).isZero();
        assertThat(response.getMonthlyRewards()).isEmpty();
    }

    @Test
    @DisplayName("endDate is correctly set in response (covers the setStartDate bug)")
    void calculateRewardPoints_endDateSetInResponse() {
        stubCustomerFound();
        LocalDate end = LocalDate.of(2024, 3, 31);
        LocalDate start = LocalDate.of(2023, 12, 31);
        stubTransactions(Collections.emptyList());

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, end);
        assertThat(response.getEndDate()).isEqualTo(end.toString());
        assertThat(response.getStartDate()).isEqualTo(start.toString());
    }

    @DisplayName("Parameterized: CalulateRewardPoints returns correct points for each amount")
    void calculateRewardPoints_pointsCalculation_correctForAllSlabs(
            double amount, int expectedPoints) {

        stubCustomerFound();

        Transaction txn = buildTransaction(1L, amount,
                LocalDate.now().minusMonths(1), "test");

        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(eq(1L), any(), any()))
                .thenReturn(List.of(txn));

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);

        assertThat(response.getTotalRewardPoints()).isEqualTo(expectedPoints);
    }

    @Test
    @DisplayName("Two transactions in same month are grouped into one MonthlyReward entry")
    void calculateRewardPoints_twoTxnSameMonth_groupedTogether() {
        stubCustomerFound();

        LocalDate date = LocalDate.now().minusMonths(1).withDayOfMonth(5);
        List<Transaction> txns = List.of(
                buildTransaction(1L, 120.0, date, "Purchase A"),
                buildTransaction(2L, 75.5, date.plusDays(3), "Purchase B")
        );
        stubTransactions(txns);

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);

        String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        MonthlyReward monthly = response.getMonthlyRewards().get(monthKey);

        assertThat(monthly).isNotNull();
        assertThat(monthly.getTransactionCount()).isEqualTo(2);
        // 120.0 → 90 pts, 75.5 → 25 pts  → total 115
        assertThat(monthly.getRewardPoints()).isEqualTo(115);
        assertThat(monthly.getTotalExpenditure()).isEqualTo(120.0 + 75.5);
        assertThat(monthly.getTransactions()).hasSize(2);
    }

    @Test
    @DisplayName("Transactions in two different months create two separate MonthlyReward entries")
    void calculateRewardPoints_txnsInTwoDifferentMonths_createdAsSeparateEntries() {
        stubCustomerFound();

        LocalDate month1Date = LocalDate.now().minusMonths(2).withDayOfMonth(5);
        LocalDate month2Date = LocalDate.now().minusMonths(1).withDayOfMonth(10);

        List<Transaction> txns = List.of(
                buildTransaction(1L, 120.0, month1Date, "Month1 purchase"),
                buildTransaction(2L, 200.0, month2Date, "Month2 purchase")
        );
        stubTransactions(txns);

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);

        assertThat(response.getMonthlyRewards()).hasSize(2);

        String key1 = month1Date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String key2 = month2Date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        assertThat(response.getMonthlyRewards().get(key1).getRewardPoints()).isEqualTo(90);   // (120-100)*2+50
        assertThat(response.getMonthlyRewards().get(key2).getRewardPoints()).isEqualTo(250);  // (200-100)*2+50
    }

    @Test
    @DisplayName("TransactionDetails fields are correctly mapped from Transaction entity")
    void calculateRewardPoints_transactionDetailsMappedCorrectly() {
        stubCustomerFound();

        LocalDate date = LocalDate.now().minusMonths(1).withDayOfMonth(5);
        Transaction txn = buildTransaction(42L, 120.0, date, "IPhone purchase");
        stubTransactions(List.of(txn));
        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);
        String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        var detail = response.getMonthlyRewards().get(monthKey).getTransactions().get(0);
        assertThat(detail.getTransactionId()).isEqualTo(42L);
        assertThat(detail.getTransactionAmount()).isEqualTo(120.0);
        assertThat(detail.getDate()).isEqualTo(date.toString());
        assertThat(detail.getDescription()).isEqualTo("IPhone purchase");
        assertThat(detail.getRewardPoints()).isEqualTo(90); // (120-100)*2+50
    }

    @Test
    @DisplayName("Total reward points, count and amount are summed correctly across all months")
    void calculateRewardPoints_multipleMonths_totalsAggregatedCorrectly() {
        stubCustomerFound();

        LocalDate m1 = LocalDate.now().minusMonths(2).withDayOfMonth(5);
        LocalDate m2 = LocalDate.now().minusMonths(1).withDayOfMonth(10);

        List<Transaction> txns = List.of(
                buildTransaction(1L, 120.0, m1, "desc1"),
                buildTransaction(2L, 200.0, m2, "desc2"),
                buildTransaction(3L, 76.0,  m1, "desc3")
        );
        stubTransactions(txns);

        RewardPointsResponse response = service.calculateRewardPoints(1L, 3, null, null);

        assertThat(response.getTotalTransactionCount()).isEqualTo(3);
        assertThat(response.getTotalTransactionAmount()).isEqualTo(120.0 + 200.0 + 76.0);
        assertThat(response.getTotalRewardPoints()).isEqualTo(90 + 250 + 26);
    }

    @Test
    @DisplayName("Ankita: 50.0 (boundary) earns 0 points")
    void calculateRewardPoints_exactly50Dollars_earnsZeroPoints() {
        stubCustomerFound();
        stubTransactions(List.of(
                buildTransaction(1L, 50.0, LocalDate.now().minusMonths(1), "No points at 50")));

        assertThat(service.calculateRewardPoints(1L, 3, null, null)
                .getTotalRewardPoints()).isZero();
    }

    @Test
    @DisplayName("Ankita: 76.0 → 26 points (first slab only)")
    void calculateRewardPoints_76Dollars_earns26Points() {
        stubCustomerFound();
        stubTransactions(List.of(
                buildTransaction(1L, 76.0, LocalDate.now().minusMonths(1), "First slab")));

        assertThat(service.calculateRewardPoints(1L, 3, null, null)
                .getTotalRewardPoints()).isEqualTo(26);
    }

    @Test
    @DisplayName("Deepankan: 120.0 → 90 points (second slab)")
    void calculateRewardPoints_120Dollars_earns90Points() {
        stubCustomerFound();
        stubTransactions(List.of(
                buildTransaction(1L, 120.0, LocalDate.now().minusMonths(1), "Second slab")));

        assertThat(service.calculateRewardPoints(1L, 3, null, null)
                .getTotalRewardPoints()).isEqualTo(90);
    }

    @Test
    @DisplayName("Ankita: 30.0 → 0 points (below first slab)")
    void calculateRewardPoints_30Dollars_earnsZeroPoints() {
        stubCustomerFound();
        stubTransactions(List.of(
                buildTransaction(1L, 30.0, LocalDate.now().minusMonths(1), "Below first slab")));

        assertThat(service.calculateRewardPoints(1L, 3, null, null)
                .getTotalRewardPoints()).isZero();
    }

    @Test
    @DisplayName("Ankita: 206.0 → 262 points (high second slab)")
    void calculateRewardPoints_206Dollars_earns262Points() {
        stubCustomerFound();
        stubTransactions(List.of(
                buildTransaction(1L, 206.0, LocalDate.now().minusMonths(1), "Sports shoes")));

        assertThat(service.calculateRewardPoints(1L, 3, null, null)
                .getTotalRewardPoints()).isEqualTo(262);
    }

    @Test
    @DisplayName("Ankita: 100.0 → 50 points (exactly at slab boundary, goes to first-slab branch)")
    void calculateRewardPoints_exactly100Dollars_earns50Points() {
        stubCustomerFound();
        stubTransactions(List.of(
                buildTransaction(1L, 100.0, LocalDate.now().minusMonths(1), "Exact 100")));

        assertThat(service.calculateRewardPoints(1L, 3, null, null)
                .getTotalRewardPoints()).isEqualTo(50);
    }
}