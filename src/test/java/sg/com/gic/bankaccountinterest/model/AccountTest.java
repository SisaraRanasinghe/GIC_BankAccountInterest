package sg.com.gic.bankaccountinterest.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {
    private static final String TEST_ACCOUNT_ID = "AC001";
    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(TEST_ACCOUNT_ID);
    }

    @Test
    void testConstructorShouldInitializeWithCorrectId() {
        assertEquals(TEST_ACCOUNT_ID, account.getAccountId(),
                "Account ID should match the value provided in constructor");
    }

    @Test
    void testConstructorShouldInitializeWithEmptyTransactionsList() {
        assertTrue(account.getTransactions().isEmpty(),
                "New account should have empty transactions list");
    }

    @Test
    void testAddTransactionShouldAcceptNullTransaction() {
        // Check if this throws an exception
        assertDoesNotThrow(() -> account.addTransaction(null),
                "Adding null transaction should not throw exception");

        // Verify it was added
        assertEquals(1, account.getTransactions().size(),
                "Null transaction should be added to the list");
    }
}