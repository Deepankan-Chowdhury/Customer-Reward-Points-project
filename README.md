Customer-Reward-Points-project - endpoint to validate

http://localhost:8080/api/rewardPoints/3
http://localhost:8080/api/rewardPoints/1?months=3
http://localhost:8080/api/rewardPoints/3?startDate=2026-01-02&endDate=2026-04-25
http://localhost:8080/api/rewardPoints/3?startDate=2026-02-17
http://localhost:8080/api/rewardPoints/3?endDate=2026-03-20
http://localhost:8080/api/rewardPoints/675 ----> com.retailer.rewardspoints.exception.CustomerNotFoundException: No such customer exists with ID:675
http://localhost:8080/api/rewardPoints/3?startDate=2026-04-25&endDate=2026-01-01 ----> com.retailer.rewardspoints.exception.IlligalDateRangeException: End date cannot be before start date
http://localhost:8080/api/rewardPoints/3?startDate=2026-04-28&endDate=2025-07-05   ---> com.retailer.rewardspoints.exception.FutureDateException: Start date and End date cannot be future date
http://localhost:8080/api/rewardPoints/3?startDate=2024-02-28&endDate=2024-02-28  ----> Leap year working as expected

# Retailer Reward Points API

A Spring Boot REST API that calculates and tracks customer reward points based on their transaction history. Customers earn points on purchases above certain thresholds, and the API provides a breakdown of points earned per month within a configurable date range.

---

## Table of Contents

- [Reward Points Logic](#reward-points-logic)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Sample Data](#sample-data)
- [Error Handling](#error-handling)
- [Configuration](#configuration)
- [H2 Console](#h2-console)
- [Testing](#testing)

---

## Reward Points Logic

Points are awarded on a tiered basis per transaction:

| Spend Range       | Points Earned              |
|-------------------|----------------------------|
| $0 – $50          | 0 points                   |
| $50.01 – $100     | 1 point per dollar over $50 |
| Above $100        | 1 point per dollar between $50–$100 (i.e. 50 points) **+** 2 points per dollar over $100 |

**Example:** A $120 purchase earns:
- 50 points for the $50–$100 tier
- (120 − 100) × 2 = 40 points for the above-$100 tier
- **Total: 90 points**

---

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.6**
  - Spring Web MVC
  - Spring Data JPA
  - Spring Validation
- **H2** – in-memory database
- **Lombok** – boilerplate reduction
- **JaCoCo** – code coverage reporting
- **Maven** – build tool

---

## Project Structure

```
src/main/java/com/retailer/rewardspoints/
│
├── RewardspointsApplication.java          # Application entry point
│
├── constants/
│   ├── Constants.java                     # Shared constants (date formats, default duration)
│   └── RewardPointsSlabs.java             # Enum defining reward tier thresholds
│
├── controller/
│   └── RewardPointController.java         # REST controller – exposes the reward points endpoint
│
├── data/
│   └── DataInitialization.java            # CommandLineRunner – seeds sample customers & transactions
│
├── dto/
│   ├── MonthlyReward.java                 # Per-month reward summary DTO
│   └── TransactionDetails.java            # Individual transaction details DTO
│
├── entity/
│   ├── Customer.java                      # Customer JPA entity
│   └── Transaction.java                   # Transaction JPA entity
│
├── exception/
│   ├── CustomerNotFoundException.java
│   ├── FutureDateException.java
│   ├── InvalidDateRangeException.java
│   └── GlobalExceptionHandler.java        # Centralised @RestControllerAdvice
│
├── repository/
│   ├── CustomerRepo.java
│   └── TransactionRepo.java               # Custom query: findByCustomerIdAndTransactionDateBetween
│
├── response/
│   ├── RewardPointsResponse.java          # Full API response wrapper
│   └── ErrorResponse.java                 # Standardised error payload
│
└── service/
    └── RewardPointsService.java           # Core business logic
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+

### Run the Application

```bash
# Clone the repository
git clone <repository-url>
cd rewardspoints

# Build and run
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.

On startup, `DataInitialization` automatically seeds the in-memory H2 database with **4 customers** and **20 transactions** spread across the last 3 months.

### Run Tests with Coverage

```bash
mvn test
```

JaCoCo coverage reports are generated at `target/site/jacoco/index.html`.

---

## API Reference

### Get Reward Points for a Customer

```
GET /api/rewardPoints/{customerId}
```

#### Path Parameters

| Parameter    | Type   | Required | Description          |
|--------------|--------|----------|----------------------|
| `customerId` | `Long` | Yes      | ID of the customer   |

#### Query Parameters

| Parameter          | Type        | Required | Default | Description                                                                 |
|--------------------|-------------|----------|---------|-----------------------------------------------------------------------------|
| `durationInMonths` | `Integer`   | No       | `3`     | Number of months to look back from today (ignored if `startDate` is given)  |
| `startDate`        | `LocalDate` | No       | —       | Start of the transaction window (`yyyy-MM-dd`)                              |
| `endDate`          | `LocalDate` | No       | today   | End of the transaction window (`yyyy-MM-dd`)                                |

**Date resolution priority:**
1. If both `startDate` and `endDate` are provided, they define the window exactly.
2. If only `endDate` is provided, `startDate` = `endDate` minus `durationInMonths`.
3. If neither is provided, the window is the last `durationInMonths` months up to today.

#### Example Requests

```bash
# Last 3 months (default)
GET /api/rewardPoints/1

# Last 6 months
GET /api/rewardPoints/1?durationInMonths=6

# Specific date range
GET /api/rewardPoints/1?startDate=2025-01-01&endDate=2025-03-31
```

#### Example Response (200 OK)

```json
{
  "customerId": 1,
  "customerName": "Deepankan Kiron Chowdhury",
  "customerEmail": "dkironchowdhury@gmail.com",
  "monthlyRewards": {
    "2025-02": {
      "month": "2025-02",
      "rewardPoints": 90,
      "totalExpenditure": 120.0,
      "transactionCount": 1,
      "transactions": [
        {
          "transactionId": 1,
          "date": "2025-02-05",
          "rewardPoints": 90,
          "description": "IPhone purchase",
          "transactionAmount": 120.0
        }
      ]
    }
  },
  "totalTransactionCount": 5,
  "totalTransactionAmount": 590.5,
  "totalRewardPoints": 385,
  "startDate": "2025-01-28",
  "endDate": "2025-04-28"
}
```

---

## Sample Data

The following customers are pre-loaded on startup:

| ID | Name                        | Email                        |
|----|-----------------------------|------------------------------|
| 1  | Deepankan Kiron Chowdhury   | dkironchowdhury@gmail.com    |
| 2  | Biswarup Roy                | biswarupRoy@gmail.com        |
| 3  | Ayush Shukla                | ayushMtetro@gmail.com        |
| 4  | Ankita Chowdhury            | ankitaChowdhury@gmail.com    |

Each customer has 5 transactions spread across the past 1–3 months covering a variety of spend amounts to exercise all reward tiers (including below-threshold, single-tier, and dual-tier transactions).

---

## Error Handling

All errors are returned as a structured `ErrorResponse`:

```json
{
  "status": 400,
  "error": "Not found",
  "message": "No such customer exists with ID: 99",
  "timestamp": "2025-04-28T10:30:00"
}
```

| Scenario                          | Exception                      | HTTP Status |
|-----------------------------------|--------------------------------|-------------|
| Customer ID does not exist        | `CustomerNotFoundException`    | 400         |
| `startDate` is after `endDate`    | `InvalidDateRangeException`    | 400         |
| `startDate` or `endDate` is in the future | `FutureDateException`  | 400         |
| Invalid parameter type            | `MethodArgumentTypeMismatchException` | 400  |
| Any unexpected error              | `Exception`                    | 500         |

---

## Configuration

`src/main/resources/application.yaml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:rewardpointsdb
    driver-class-name: org.h2.Driver
    username: deep
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
```

The database is entirely in-memory and resets on every application restart.

---

## H2 Console

The H2 web console is available at **http://localhost:8080/h2-console** while the application is running.

| Field        | Value                      |
|--------------|----------------------------|
| JDBC URL     | `jdbc:h2:mem:rewardpointsdb` |
| Username     | `deep`                     |
| Password     | *(leave blank)*            |

---

## Testing

The project has two test classes covering the service layer and the controller layer independently using **JUnit 5** and **Mockito**. All tests are unit tests — no Spring context is loaded and no real database is hit.

```bash
# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=RewardPointsServiceTest
mvn test -Dtest=RewardPointControllerTest
```

JaCoCo coverage reports are written to `target/site/jacoco/index.html` after `mvn test`.

---

### RewardPointsServiceTest

Tests the core business logic in `RewardPointsService` in isolation. `CustomerRepo` and `TransactionRepo` are both mocked with Mockito.

#### Exception / Validation Tests

| Test | Description |
|------|-------------|
| `calculateRewardPoints_unknownCustomer_throwsCustomerNotFoundException` | Throws `CustomerNotFoundException` when the given customer ID does not exist; verifies the transaction repo is never called. |
| `calculateRewardPoints_startAfterEnd_throwsIlligalDateRangeException` | Throws `InvalidDateRangeException` when `startDate` is strictly after `endDate`; verifies the transaction repo is never called. |
| `calculateRewardPoints_startAOrEnd_throwsFututreDateException` | Throws `FutureDateException` when either `startDate` or `endDate` is a future date; verifies the transaction repo is never called. |
| `calculateRewardPoints_startOneDayAfterEnd_throwsIlligalDateRangeException` | Throws `InvalidDateRangeException` when `startDate` is exactly one day after `endDate` (off-by-one boundary check). |
| `calculateRewardPoints_startEqualsEnd_doesNotThrow` | A same-day window (`startDate == endDate`) is treated as valid and does not throw. |
| `calculateRewardPoints_LeapYear_doesNotThrow` | Passing `2024-02-29` (a valid leap-year date) does not throw any exception. |

#### Date Resolution / Window Tests

| Test | Description |
|------|-------------|
| `calculateRewardPoints_bothDatesNull_defaultsToTodayMinusDuration` | When both dates are `null`, the query window is `today − durationInMonths` → `today`. |
| `calculateRewardPoints_bothDatesNullDuration6_usesSixMonthWindow` | When `durationInMonths = 6` and both dates are `null`, a 6-month window ending today is used. |
| `calculateRewardPoints_durationNull_usesConstantDefault3` | When `durationInMonths` is `null`, the service falls back to `Constants.DEFAULT_MONTH_DURATION` (3). |
| `calculateRewardPoints_onlyEndDateProvided_startDefaultsFromEnd` | When only `endDate` is provided, `startDate` is calculated as `endDate − durationInMonths`. |
| `calculateRewardPoints_onlyStartDateProvided_endDefaultsToToday` | When only `startDate` is provided, `endDate` defaults to today. |
| `calculateRewardPoints_bothDatesExplicit_usesThemDirectly` | When both dates are supplied, they are forwarded to the repository without modification. |

#### Response Correctness Tests

| Test | Description |
|------|-------------|
| `calculateRewardPoints_responseHasCorrectCustomerInfo` | The response contains the correct `customerId`, `customerName`, and `customerEmail`. |
| `calculateRewardPoints_endDateSetInResponse` | Both `startDate` and `endDate` are correctly stored in the response object. |
| `calculateRewardPoints_noTransactions_allTotalsAreZero` | When the repository returns no transactions, all totals (points, count, amount) are zero and `monthlyRewards` is empty. |
| `calculateRewardPoints_transactionDetailsMappedCorrectly` | All fields of `TransactionDetails` (ID, amount, date, description, points) are mapped correctly from the `Transaction` entity. |

#### Grouping & Aggregation Tests

| Test | Description |
|------|-------------|
| `calculateRewardPoints_twoTxnSameMonth_groupedTogether` | Two transactions in the same calendar month are merged into a single `MonthlyReward` entry with combined points, expenditure, and count. |
| `calculateRewardPoints_txnsInTwoDifferentMonths_createdAsSeparateEntries` | Transactions in two different months produce two separate `MonthlyReward` map entries. |
| `calculateRewardPoints_multipleMonths_totalsAggregatedCorrectly` | `totalRewardPoints`, `totalTransactionCount`, and `totalTransactionAmount` are correctly summed across all monthly entries. |

#### Reward Points Calculation (Slab Boundary) Tests

| Test | Input Amount | Expected Points | Slab Hit |
|------|-------------|-----------------|----------|
| `calculateRewardPoints_30Dollars_earnsZeroPoints` | $30.00 | 0 | Below $50 threshold |
| `calculateRewardPoints_exactly50Dollars_earnsZeroPoints` | $50.00 | 0 | Boundary — no points at exactly $50 |
| `calculateRewardPoints_76Dollars_earns26Points` | $76.00 | 26 | First slab only: (76 − 50) × 1 |
| `calculateRewardPoints_exactly100Dollars_earns50Points` | $100.00 | 50 | First slab only: (100 − 50) × 1 |
| `calculateRewardPoints_120Dollars_earns90Points` | $120.00 | 90 | Both slabs: 50 + (120 − 100) × 2 |
| `calculateRewardPoints_206Dollars_earns262Points` | $206.00 | 262 | Both slabs: 50 + (206 − 100) × 2 |

---

### RewardPointControllerTest

Tests `RewardPointController` using `MockMvc` in standalone mode. `RewardPointsService` is mocked — no service logic runs. The focus is on HTTP routing, parameter binding, serialization, and error propagation.

#### Happy Path Tests

| Test | Description |
|------|-------------|
| `getRewardPoints_defaultParams_returns200` | Calling the endpoint with only a `customerId` returns HTTP 200 and the correct JSON fields (`customerId`, `customerName`, `totalRewardPoints`, etc.). Verifies the service is called with `durationInMonths = 3` and both dates `null`. |
| `getRewardPoints_customDuration_callsServiceWithCorrectDuration` | Passing `durationInMonths=6` forwards the value `6` to the service. |
| `getRewardPoints_withBothDates_callsServiceWithParsedDates` | Both `startDate` and `endDate` query params are parsed from `yyyy-MM-dd` strings and passed as `LocalDate` objects to the service. |
| `getRewardPoints_onlyStartDate_endDateIsNull` | When only `startDate` is given, the controller forwards it with `endDate = null`. |
| `getRewardPoints_onlyEndDate_startDateIsNull` | When only `endDate` is given, the controller forwards it with `startDate = null`. |
| `getRewardPoints_allParamsSupplied_allForwardedToService` | All four params (`customerId`, `durationInMonths`, `startDate`, `endDate`) are forwarded correctly when all are provided. |

#### Response Structure Tests

| Test | Description |
|------|-------------|
| `getRewardPoints_monthlyRewardsMapPresentInResponse` | The `monthlyRewards` map is present in the JSON response and the nested fields (`rewardPoints`, `totalExpenditure`, `transactionCount`) are correctly serialised. |
| `getRewardPoints_emptyMonthlyRewards_returns200` | A response with an empty `monthlyRewards` map and zero totals is handled correctly and still returns HTTP 200. |

#### Exception / Error Tests

| Test | Description |
|------|-------------|
| `getRewardPoints_customerNotFound_propagatesException` | A `CustomerNotFoundException` thrown by the service propagates out of the service call as expected. |
| `getRewardPoints_illegalDateRange_propagatesException` | An `InvalidDateRangeException` thrown by the service propagates out of the service call as expected. |
| `getRewardPoints_nonNumericCustomerId_returns400` | Passing a non-numeric value (e.g. `abc`) as `customerId` results in HTTP 400 Bad Request due to type mismatch. |
| `getRewardPoints_invalidDateFormat_returns400` | Passing a date in an unsupported format (e.g. `01-01-2024` instead of `2024-01-01`) results in HTTP 400 Bad Request. |
