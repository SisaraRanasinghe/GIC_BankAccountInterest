package sg.com.gic.bankaccountinterest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.bankaccountinterest.model.Account;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.service.serviceimpl.TransactionServiceImpl;
import sg.com.gic.bankaccountinterest.validator.DateValidator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DateValidator dateValidator;
    private TransactionServiceImpl transactionService;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(accountRepository, dateValidator);
        System.setOut(new PrintStream(outContent));
    }

    @Test
    void testConstructor() {
        assertNotNull(transactionService);
    }

    @Test
    void testProcessTransactionWithInvalidDateNullDate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(null, "AC001", "D", "100.00")
        );

        assertEquals("Invalid date format. Date should be in YYYYMMdd format (8 digits only).", exception.getMessage());
        verify(dateValidator, never()).validateDate(anyString());
    }

    @Test
    void testProcessTransactionWithInvalidDateWrongFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction("2025-01-01", "AC001", "D", "100.00")
        );

        assertEquals("Invalid date format. Date should be in YYYYMMdd format (8 digits only).", exception.getMessage());
        verify(dateValidator, never()).validateDate(anyString());
    }

    @Test
    void testProcessTransactionWithInvalidDateInvalidDate() {
        String date = "20251232"; // Invalid date

        doThrow(new IllegalArgumentException("Invalid date"))
                .when(dateValidator).validateDate(date);

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, "AC001", "D", "100.00")
        );

        verify(dateValidator).validateDate(date);
    }

    @Test
    void testProcessTransactionWithInvalidAccountId() {
        String date = "20250101";
        String accountId = "Account0001"; // > 10 characters

        doNothing().when(dateValidator).validateDate(anyString());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, accountId, "D", "100.00")
        );

        assertEquals("Invalid account name. It should be up to 10 characters.", exception.getMessage());
    }

    @Test
    void testProcessTransactionWithInvalidTransactionType() {
        String date = "20250101";
        String accountId = "AC001";
        String type = "X"; // Invalid type

        doNothing().when(dateValidator).validateDate(anyString());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, accountId, type, "100.00")
        );

        assertEquals("Transaction type must be D (deposit) or W (withdrawal)", exception.getMessage());
    }

    @Test
    void testProcessTransactionWithInvalidAmountFormat() {
        String date = "20250101";
        String accountId = "AC001";
        String type = "D";
        String amount = "abc"; // Not a number

        doNothing().when(dateValidator).validateDate(anyString());

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, accountId, type, amount)
        );

        assertEquals("Invalid Amount. Please enter a valid numeric value.", exception.getMessage());
        verify(dateValidator).validateDate(date);

        // Try a different invalid format to ensure coverage
        Exception exception2 = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, accountId, type, "100,00")
        );
        assertEquals("Invalid Amount. Please enter a valid numeric value.", exception2.getMessage());
    }

    @Test
    void testProcessTransactionWithNegativeAmount() {
        String date = "20250101";
        String accountId = "AC001";
        String type = "D";
        String amount = "-100.00"; // Negative amount

        doNothing().when(dateValidator).validateDate(anyString());

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, accountId, type, amount)
        );

        assertEquals("Amount must be greater than zero", exception.getMessage());
        verify(dateValidator).validateDate(date);
    }

    @Test
    void testProcessTransactionWithInsufficientBalance() {
        String date = "20250101";
        String accountId = "AC001";
        String type = "W"; // Withdrawal
        String amount = "100.00";

        doNothing().when(dateValidator).validateDate(anyString());

        //Mock exact values and use exact comparison to match code path
        when(accountRepository.calculateBalanceAtDate(eq(accountId), eq(date)))
                .thenReturn(new BigDecimal("99.99")); // Balance less than withdrawal amount

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(date, accountId, type, amount)
        );

        assertEquals("Insufficient balance for withdrawal", exception.getMessage());
        verify(dateValidator).validateDate(date);
        verify(accountRepository).calculateBalanceAtDate(accountId, date);
    }

    @Test
    void testProcessTransactionWithValidWithdrawal() {
        String date = "20250101";
        String accountId = "AC001";
        String type = "W";
        String amount = "50.00";

        Account account = new Account(accountId);
        lenient().when(accountRepository.findByAccountId(accountId)).thenReturn(Optional.of(account));

        doNothing().when(dateValidator).validateDate(anyString());
        when(accountRepository.calculateBalanceAtDate(anyString(), anyString())).thenReturn(new BigDecimal("100.00"));
        when(accountRepository.findByAccountId(anyString())).thenReturn(Optional.of(account));

        transactionService.processTransaction(date, accountId, type, amount);

        verify(dateValidator).validateDate(date);
        verify(accountRepository).calculateBalanceAtDate(accountId, date);
        verify(accountRepository).addTransaction(eq(accountId), any(Transaction.class));
    }
}
