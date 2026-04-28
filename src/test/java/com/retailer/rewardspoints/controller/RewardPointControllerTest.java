package com.retailer.rewardspoints.controller;
import com.retailer.rewardspoints.exception.CustomerNotFoundException;
import com.retailer.rewardspoints.exception.InvalidDateRangeException;
import com.retailer.rewardspoints.dto.MonthlyReward;
import com.retailer.rewardspoints.response.RewardPointsResponse;
import com.retailer.rewardspoints.service.RewardPointsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RewardPointControllerTest {

    @Mock
    private RewardPointsService service;

    @InjectMocks
    private RewardPointController controller;

    private MockMvc mockMvc;
    private RewardPointsResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        MonthlyReward jan = new MonthlyReward(
                "2024-01", 90, 120.0, List.of(), 1);

        Map<String, MonthlyReward> monthlyMap = new HashMap<>();
        monthlyMap.put("2024-01", jan);

        sampleResponse = new RewardPointsResponse(
                1L, "Alice Smith", "alice@example.com",
                monthlyMap, 1, 120.0, 90,
                "2024-01-01", "2024-03-31");
    }

    @Test
    @DisplayName("Default params: returns 200 with correct response body")
    void getRewardPoints_defaultParams_returns200() throws Exception {
        when(service.calculateRewardPoints(eq(1L), eq(3), isNull(), isNull()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("Alice Smith"))
                .andExpect(jsonPath("$.customerEmail").value("alice@example.com"))
                .andExpect(jsonPath("$.totalRewardPoints").value(90))
                .andExpect(jsonPath("$.totalTransactionCount").value(1))
                .andExpect(jsonPath("$.totalTransactionAmount").value(120.0));

        verify(service, times(1)).calculateRewardPoints(1L, 3, null, null);
    }

    @Test
    @DisplayName("Custom durationInMonths: passes value to service correctly")
    void getRewardPoints_customDuration_callsServiceWithCorrectDuration() throws Exception {
        when(service.calculateRewardPoints(eq(1L), eq(6), isNull(), isNull()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .param("durationInMonths", "6")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).calculateRewardPoints(1L, 6, null, null);
    }

    @Test
    @DisplayName("Both startDate and endDate provided: passes parsed dates to service")
    void getRewardPoints_withBothDates_callsServiceWithParsedDates() throws Exception {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end   = LocalDate.of(2024, 3, 31);

        when(service.calculateRewardPoints(eq(1L), eq(3), eq(start), eq(end)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .param("startDate", "2024-01-01")
                        .param("endDate",  "2024-03-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRewardPoints").value(90));

        verify(service).calculateRewardPoints(1L, 3, start, end);
    }

    @Test
    @DisplayName("Only startDate provided: endDate is null when passed to service")
    void getRewardPoints_onlyStartDate_endDateIsNull() throws Exception {
        LocalDate start = LocalDate.of(2024, 1, 1);

        when(service.calculateRewardPoints(eq(1L), eq(3), eq(start), isNull()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .param("startDate", "2024-01-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).calculateRewardPoints(1L, 3, start, null);
    }

    @Test
    @DisplayName("Only endDate provided: startDate is null when passed to service")
    void getRewardPoints_onlyEndDate_startDateIsNull() throws Exception {
        LocalDate end = LocalDate.of(2024, 3, 31);

        when(service.calculateRewardPoints(eq(1L), eq(3), isNull(), eq(end)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .param("endDate", "2024-03-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).calculateRewardPoints(1L, 3, null, end);
    }

    @Test
    @DisplayName("All params supplied: all values forwarded to service correctly")
    void getRewardPoints_allParamsSupplied_allForwardedToService() throws Exception {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end   = LocalDate.of(2024, 6, 30);

        when(service.calculateRewardPoints(eq(2L), eq(6), eq(start), eq(end)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/2")
                        .param("durationInMonths", "6")
                        .param("startDate", "2024-01-01")
                        .param("endDate",  "2024-06-30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).calculateRewardPoints(2L, 6, start, end);
    }

    @Test
    @DisplayName("Monthly rewards map is present and correctly structured in response")
    void getRewardPoints_monthlyRewardsMapPresentInResponse() throws Exception {
        when(service.calculateRewardPoints(any(), any(), any(), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyRewards['2024-01'].rewardPoints").value(90))
                .andExpect(jsonPath("$.monthlyRewards['2024-01'].totalExpenditure").value(120.0))
                .andExpect(jsonPath("$.monthlyRewards['2024-01'].transactionCount").value(1));
    }

    @Test
    @DisplayName("Response with empty monthlyRewards is handled correctly")
    void getRewardPoints_emptyMonthlyRewards_returns200() throws Exception {
        RewardPointsResponse emptyResponse = new RewardPointsResponse(
                1L, "Alice Smith", "alice@example.com",
                Collections.emptyMap(), 0, 0.0, 0,
                "2024-01-01", "2024-03-31");

        when(service.calculateRewardPoints(any(), any(), any(), any()))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/rewardPoints/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRewardPoints").value(0))
                .andExpect(jsonPath("$.totalTransactionCount").value(0));
    }

    @Test
    @DisplayName("CustomerNotFoundException from service propagates as 5xx")
    void getRewardPoints_customerNotFound_propagatesException() throws Exception {
        when(service.calculateRewardPoints(eq(99L), any(), any(), any()))
                .thenThrow(new CustomerNotFoundException(99L));

        assertThrows(CustomerNotFoundException.class, () -> {
            service.calculateRewardPoints(99L, null, null, null);
        });
    }

    @Test
    @DisplayName("InvalidDateRangeException from service propagates as 5xx")
    void getRewardPoints_illegalDateRange_propagatesException() throws Exception {
        when(service.calculateRewardPoints(eq(1L), any(), eq(LocalDate.of(2026, 10, 03)), eq(LocalDate.now())))
                .thenThrow(new InvalidDateRangeException());

        assertThrows(InvalidDateRangeException.class, () -> {
            service.calculateRewardPoints(1L, null, LocalDate.of(2026, 10, 03), LocalDate.now());
        });
    }

    @Test
    @DisplayName("Non-numeric customerId returns 400 Bad Request")
    void getRewardPoints_nonNumericCustomerId_returns400() throws Exception {
        mockMvc.perform(get("/api/rewardPoints/abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Invalid date format returns 400 Bad Request")
    void getRewardPoints_invalidDateFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/rewardPoints/1")
                        .param("startDate", "01-01-2024")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}