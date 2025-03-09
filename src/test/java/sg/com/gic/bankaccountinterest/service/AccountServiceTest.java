package sg.com.gic.bankaccountinterest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.bankaccountinterest.model.Account;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.model.TransactionType;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.service.serviceimpl.AccountServiceImpl;
import sg.com.gic.bankaccountinterest.validator.DateValidator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InterestCalculator interestCalculator;

    @Mock
    private DateValidator dateValidator;

    private AccountServiceImpl accountService;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository, interestCalculator, dateValidator);
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @Test
    void testConstructorShouldInitializeDependencies() {
        assertNotNull(accountService);
    }

    @Test
    void testAccountExistsWhenAccountExistsShouldReturnTrue() {
        String accountId = "AC001";
        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(new Account(accountId)));
        boolean result = accountService.accountExists(accountId);

        assertTrue(result);
        verify(accountRepository).findByAccountId(accountId);
    }

    @Test
    void testPrintStatementWithInvalidYearMonthShouldThrowException() {
        String accountId = "AC001";
        String invalidYearMonth = "2025"; // Not in the format YYYYMM

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.printStatement(accountId, invalidYearMonth));

        assertEquals("Invalid date format. Date should be in YYYYMM format.", exception.getMessage());
        verifyNoInteractions(accountRepository, interestCalculator);
    }

    @Test
    void testPrintStatementWithInvalidDateShouldThrowException() {
        String accountId = "AC001";
        String yearMonth = "202513"; // Invalid month 13

        doThrow(new IllegalArgumentException("Invalid date")).when(dateValidator).validateDate(anyString());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.printStatement(accountId, yearMonth));

        assertEquals("Invalid date", exception.getMessage());
        verify(dateValidator).validateDate("20251301");
    }

    @Test
    void testPrintStatementWithNonExistentAccountShouldThrowException() {
        String accountId = "AC001";
        String yearMonth = "202501";

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.printStatement(accountId, yearMonth));

        assertEquals("Account does not exist. Unable to generate print statement.", exception.getMessage());
        verify(accountRepository).findByAccountId(accountId);
    }

    @Test
    void testPrintStatementWithNoTransactionsShouldThrowException() {
        String accountId = "AC001";
        String yearMonth = "202501";

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(new Account(accountId)));
        when(accountRepository.getTransactionsInPeriod(eq(accountId), eq("20250101"), eq("20250131")))
                .thenReturn(new ArrayList<>());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                accountService.printStatement(accountId, yearMonth));

        assertEquals("No transactions found for the given date. Unable to generate print statement.", exception.getMessage());
        verify(accountRepository).getTransactionsInPeriod(accountId, "20250101", "20250131");
    }

    @Test
    void testPrintStatementDateFormatPatternTest() {
        String accountId = "AC001";
        String yearMonth = "202501";

        Account account = new Account(accountId);
        lenient().when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("20250101", "20250101-01", TransactionType.DEPOSIT, new BigDecimal("100.00")));

        lenient().when(accountRepository.getTransactionsInPeriod(anyString(), anyString(), anyString()))
                .thenReturn(transactions);
        lenient().when(accountRepository.calculateBalanceBeforeDate(anyString(), anyString()))
                .thenReturn(new BigDecimal("200.00"));
        lenient().when(interestCalculator.calculateMonthlyInterest(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1.25"));

        accountService.printStatement(accountId, yearMonth);

        // Verify the correct date format is being used by checking the captured parameters
        ArgumentCaptor<String> startDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> endDateCaptor = ArgumentCaptor.forClass(String.class);

        verify(accountRepository).getTransactionsInPeriod(
                eq(accountId),
                startDateCaptor.capture(),
                endDateCaptor.capture()
        );

        assertEquals("20250101", startDateCaptor.getValue());
        assertEquals("20250131", endDateCaptor.getValue());
    }

    @Test
    void testPrintStatementWithLeapYearFebruaryShouldHandleCorrectEndDate() {
        String accountId = "AC001";
        String yearMonth = "202402"; // February 2024 (leap year)

        Account account = new Account(accountId);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("20240215", "20240215-01", TransactionType.DEPOSIT, new BigDecimal("100.00")));

        when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.getTransactionsInPeriod(anyString(), anyString(), anyString()))
                .thenReturn(transactions);
        when(accountRepository.calculateBalanceBeforeDate(anyString(), anyString()))
                .thenReturn(new BigDecimal("200.00"));
        when(interestCalculator.calculateMonthlyInterest(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1.25"));

        accountService.printStatement(accountId, yearMonth);

        // Verify the correct start and end dates for February in a leap year
        ArgumentCaptor<String> startDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> endDateCaptor = ArgumentCaptor.forClass(String.class);

        verify(accountRepository).getTransactionsInPeriod(
                eq(accountId),
                startDateCaptor.capture(),
                endDateCaptor.capture()
        );

        assertEquals("20240201", startDateCaptor.getValue());
        assertEquals("20240229", endDateCaptor.getValue()); // February 2024 has 29 days (leap year)
    }

    @Test
    void testWithdrawalBranchExplicitly() {
        String accountId = "AC001";
        String yearMonth = "202501";

        Account account = new Account(accountId);

        // Create a list with just one withdrawal transaction
        List<Transaction> transactions = new ArrayList<>();
        Transaction withdrawal = new Transaction("20250101", "20250101-01", TransactionType.WITHDRAWAL, new BigDecimal("50.00"));
        transactions.add(withdrawal);

        lenient().when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));
        lenient().when(accountRepository.getTransactionsInPeriod(anyString(), anyString(), anyString()))
                .thenReturn(transactions);
        lenient().when(accountRepository.calculateBalanceBeforeDate(anyString(), anyString()))
                .thenReturn(new BigDecimal("100.00"));
        lenient().when(interestCalculator.calculateMonthlyInterest(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("0.50"));

        // Reset output capture for a clean test
        outputStreamCaptor.reset();

        // Execute the method
        accountService.printStatement(accountId, yearMonth);

        // Convert the output to string and verify the balance calculation
        String output = outputStreamCaptor.toString();

        // After a withdrawal of 50 from 100, balance should be 50
        assertTrue(output.contains("50.00 |    50.00"),
                "Output should show balance of 50.00 after withdrawal: " + output);
    }
}
