package sg.com.gic.bankaccountinterest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.bankaccountinterest.model.InterestRule;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.model.TransactionType;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.repository.InterestRuleRepository;
import sg.com.gic.bankaccountinterest.service.serviceimpl.InterestCalculatorImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InterestCalculatorTest {
    @Mock
    private InterestRuleRepository interestRuleRepository;

    @Mock
    private AccountRepository accountRepository;

    private InterestCalculatorImpl calculator;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ACCOUNT_ID = "AC001";

    @BeforeEach
    void setUp() {
        calculator = new InterestCalculatorImpl(interestRuleRepository, accountRepository);
    }

    @Test
    void testConstructorShouldInitializeRepositoriesProperly() {
        InterestCalculatorImpl instance = new InterestCalculatorImpl(interestRuleRepository, accountRepository);
        assertNotNull(instance);
    }

    @Test
    void testCalculateMonthlyInterestWhenSinglePeriodShouldCalculateInterestCorrectly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        InterestRule rule = new InterestRule("20251201", "RULE01", new BigDecimal("2.50"));
        List<InterestRule> rules = Collections.singletonList(rule);
        BigDecimal balance = new BigDecimal("10000.00");

        when(interestRuleRepository.getAllRules()).thenReturn(rules);
        when(interestRuleRepository.getApplicableRuleForDate(any(LocalDate.class))).thenReturn(Optional.of(rule));
        when(accountRepository.getTransactionsInPeriod(
                eq(ACCOUNT_ID),
                eq(startDate.format(DATE_FORMAT)),
                eq(endDate.format(DATE_FORMAT))))
                .thenReturn(Collections.emptyList());
        when(accountRepository.calculateBalanceAtDate(eq(ACCOUNT_ID), anyString()))
                .thenReturn(balance);

        BigDecimal result = calculator.calculateMonthlyInterest(ACCOUNT_ID, startDate, endDate);

        // (10000 * 2.50 * 31) / (365 * 100) = 21.23
        BigDecimal expected = new BigDecimal("21.23");
        assertEquals(expected, result);
    }

    @Test
    void testCalculateMonthlyInterestWhenMultiplePeriodsDueToTransactionsShouldCalculateInterestCorrectly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        InterestRule rule = new InterestRule("20251201", "RULE01", new BigDecimal("2.50"));
        List<InterestRule> rules = Collections.singletonList(rule);

        Transaction transaction1 = new Transaction("20250110", "20250110-01", TransactionType.DEPOSIT, new BigDecimal("5000.00"));
        Transaction transaction2 = new Transaction("20250120", "20250120-01", TransactionType.WITHDRAWAL, new BigDecimal("3000.00"));
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        BigDecimal balance1 = new BigDecimal("10000.00"); // Jan 1 - Jan 9
        BigDecimal balance2 = new BigDecimal("15000.00"); // Jan 10 - Jan 19
        BigDecimal balance3 = new BigDecimal("12000.00"); // Jan 20 - Jan 31

        when(interestRuleRepository.getAllRules()).thenReturn(rules);
        when(interestRuleRepository.getApplicableRuleForDate(any(LocalDate.class))).thenReturn(Optional.of(rule));
        when(accountRepository.getTransactionsInPeriod(
                eq(ACCOUNT_ID),
                eq(startDate.format(DATE_FORMAT)),
                eq(endDate.format(DATE_FORMAT))))
                .thenReturn(transactions);

        when(accountRepository.calculateBalanceAtDate(eq(ACCOUNT_ID), eq(startDate.format(DATE_FORMAT))))
                .thenReturn(balance1);
        when(accountRepository.calculateBalanceAtDate(eq(ACCOUNT_ID), eq(LocalDate.of(2025, 1, 10).format(DATE_FORMAT))))
                .thenReturn(balance2);
        when(accountRepository.calculateBalanceAtDate(eq(ACCOUNT_ID), eq(LocalDate.of(2025, 1, 20).format(DATE_FORMAT))))
                .thenReturn(balance3);

        BigDecimal result = calculator.calculateMonthlyInterest(ACCOUNT_ID, startDate, endDate);

        // Period 1: (10000 * 2.50 * 9) / (365 * 100) = 6.16
        // Period 2: (15000 * 2.50 * 10) / (365 * 100) = 10.27
        // Period 3: (12000 * 2.50 * 12) / (365 * 100) = 9.86
        // Total: 26.29 (rounded to 26.30)
        BigDecimal expected = new BigDecimal("26.30");
        assertEquals(expected, result);
    }

    @Test
    void testCalculateMonthlyInterestWhenMultiplePeriodsDueToRuleChangesShouldCalculateInterestCorrectly() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        InterestRule rule1 = new InterestRule("20241201", "RULE01", new BigDecimal("2.50"));
        InterestRule rule2 = new InterestRule("20250115", "RULE01", new BigDecimal("3.00"));
        List<InterestRule> rules = Arrays.asList(rule1, rule2);

        BigDecimal balance = new BigDecimal("10000.00");

        when(interestRuleRepository.getAllRules()).thenReturn(rules);
        when(accountRepository.getTransactionsInPeriod(
                eq(ACCOUNT_ID),
                eq(startDate.format(DATE_FORMAT)),
                eq(endDate.format(DATE_FORMAT))))
                .thenReturn(Collections.emptyList());

        // This matches how InterestRuleRepositoryImpl.getApplicableRuleForDate works:
        // It returns the most recent rule that's not after the given date
        when(interestRuleRepository.getApplicableRuleForDate(any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(0);
                    if (date.isBefore(LocalDate.of(2025, 1, 15))) {
                        return Optional.of(rule1);
                    } else {
                        return Optional.of(rule2);
                    }
                });

        when(accountRepository.calculateBalanceAtDate(eq(ACCOUNT_ID), anyString()))
                .thenReturn(balance);

        BigDecimal result = calculator.calculateMonthlyInterest(ACCOUNT_ID, startDate, endDate);

        // Period 1: (10000 * 2.50 * 14) / (365 * 100) = 9.59
        // Period 2: (10000 * 3.00 * 17) / (365 * 100) = 13.97
        // Total: 23.56
        BigDecimal expected = new BigDecimal("23.56");
        assertEquals(expected, result);
    }

    @Test
    void testCalculateMonthlyInterestWhenNoApplicableRuleForPeriodShouldSkipThatPeriod() {
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        InterestRule rule = new InterestRule("20250115", "RULE01", new BigDecimal("2.50"));
        List<InterestRule> rules = Collections.singletonList(rule);

        BigDecimal balance = new BigDecimal("10000.00");

        when(interestRuleRepository.getAllRules()).thenReturn(rules);
        when(accountRepository.getTransactionsInPeriod(
                eq(ACCOUNT_ID),
                eq(startDate.format(DATE_FORMAT)),
                eq(endDate.format(DATE_FORMAT))))
                .thenReturn(Collections.emptyList());

        // Simulating InterestRuleRepositoryImpl behavior - no applicable rule before Jan 15
        when(interestRuleRepository.getApplicableRuleForDate(any(LocalDate.class)))
                .thenAnswer(invocation -> {
                    LocalDate date = invocation.getArgument(0);
                    if (date.isBefore(LocalDate.of(2025, 1, 15))) {
                        return Optional.empty();
                    } else {
                        return Optional.of(rule);
                    }
                });

        when(accountRepository.calculateBalanceAtDate(eq(ACCOUNT_ID), anyString()))
                .thenReturn(balance);

        BigDecimal result = calculator.calculateMonthlyInterest(ACCOUNT_ID, startDate, endDate);

        // Period 1: No applicable rule, so skip
        // Period 2: (10000 * 2.50 * 17) / (365 * 100) = 11.64
        BigDecimal expected = new BigDecimal("11.64");
        assertEquals(expected, result);
    }
}
