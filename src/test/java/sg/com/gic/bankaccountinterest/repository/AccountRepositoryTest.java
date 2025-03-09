package sg.com.gic.bankaccountinterest.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.com.gic.bankaccountinterest.model.Account;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.model.TransactionType;
import sg.com.gic.bankaccountinterest.repository.repositoryimpl.AccountRepositoryImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountRepositoryTest {
    private AccountRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new AccountRepositoryImpl();
    }

    @Test
    void testFindByAccountIdExistingAccountReturnsAccount() {
        Account account = new Account("AC001");
        repository.saveAccount(account);
        Optional<Account> result = repository.findByAccountId("AC001");

        assertTrue(result.isPresent());
        assertEquals("AC001", result.get().getAccountId());
    }

    @Test
    void testAddTransactionExistingAccountTransactionIsAdded() {
        Account account = new Account("AC001");
        repository.saveAccount(account);
        Transaction transaction = new Transaction("20250101", "20250101-01", TransactionType.DEPOSIT, new BigDecimal("100.00"));
        repository.addTransaction("AC001", transaction);

        Optional<Account> updatedAccount = repository.findByAccountId("AC001");
        assertTrue(updatedAccount.isPresent());
        assertEquals(1, updatedAccount.get().getTransactions().size());
        assertEquals(TransactionType.DEPOSIT, updatedAccount.get().getTransactions().get(0).getType());
        assertEquals(new BigDecimal("100.00"), updatedAccount.get().getTransactions().get(0).getAmount());
    }

    @Test
    void testCalculateBalanceAtDateMultipleTransactionTypesReturnsCorrectBalance() {
        Account account = new Account("AC001");
        repository.saveAccount(account);

        // Add transactions
        repository.addTransaction("AC001", new Transaction("20250101", "20250101-01", TransactionType.DEPOSIT, new BigDecimal("100.00")));
        repository.addTransaction("AC001", new Transaction("20250102", "20250102-01", TransactionType.WITHDRAWAL, new BigDecimal("50.00")));
        repository.addTransaction("AC001", new Transaction("20250103", "20250103-01", TransactionType.INTEREST, new BigDecimal("5.00")));
        repository.addTransaction("AC001", new Transaction("20250104", "20250104-01", TransactionType.DEPOSIT, new BigDecimal("200.00")));

        // Check balance on Jan 3
        BigDecimal balance = repository.calculateBalanceAtDate("AC001", "20250103");
        assertEquals(new BigDecimal("55.00"), balance);
    }

    @Test
    void testCalculateBalanceAtDateNonExistentAccountReturnsZero() {
        BigDecimal balance = repository.calculateBalanceAtDate("NON_EXISTENT", "20250101");
        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void testCalculateBalanceBeforeDateExistingAccountWithTransactionsReturnsCorrectBalance() {
        Account account = new Account("AC001");
        repository.saveAccount(account);

        repository.addTransaction("AC001", new Transaction("20250101", "20250101-01", TransactionType.DEPOSIT, new BigDecimal("100.00")));
        repository.addTransaction("AC001", new Transaction("20250102", "20250102-01", TransactionType.WITHDRAWAL, new BigDecimal("50.00")));
        repository.addTransaction("AC001", new Transaction("20250103", "20250103-01", TransactionType.INTEREST, new BigDecimal("5.00")));

        // Check balance before Jan 3
        BigDecimal balance = repository.calculateBalanceBeforeDate("AC001", "20250103");
        assertEquals(new BigDecimal("50.00"), balance);
    }

    @Test
    void testCalculateBalanceBeforeDateNonExistentAccountReturnsZero() {
        BigDecimal balance = repository.calculateBalanceBeforeDate("NON_EXISTENT", "20250101");
        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void testGetTransactionsInPeriodExistingAccountWithTransactionsInPeriodReturnsTransactions() {
        Account account = new Account("AC001");
        repository.saveAccount(account);

        repository.addTransaction("AC001", new Transaction("20250101", null, TransactionType.DEPOSIT, new BigDecimal("100.00")));
        repository.addTransaction("AC001", new Transaction("20250102", "20250102-01", TransactionType.WITHDRAWAL, new BigDecimal("50.00")));
        repository.addTransaction("AC001", new Transaction("20250103", "20250103-01", TransactionType.INTEREST, new BigDecimal("5.00")));
        repository.addTransaction("AC001", new Transaction("20250104", "20250104-01", TransactionType.DEPOSIT, new BigDecimal("200.00")));

        // Get transactions from Jan 2 to Jan 3
        List<Transaction> transactions = repository.getTransactionsInPeriod("AC001", "20250102", "20250103");
        assertEquals(2, transactions.size());
    }
}
