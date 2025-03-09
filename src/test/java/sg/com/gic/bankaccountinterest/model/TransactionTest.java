package sg.com.gic.bankaccountinterest.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTest {
    @Test
    void testConstructorShouldInitializeFields() {
        String date = "20250101";
        String id = "GIC123456";
        TransactionType type = TransactionType.DEPOSIT;
        BigDecimal amount = new BigDecimal("100.50");

        Transaction transaction = new Transaction(date, id, type, amount);

        assertEquals(date, transaction.getDate());
        assertEquals(id, transaction.getId());
        assertEquals(type, transaction.getType());
        assertEquals(amount, transaction.getAmount());
    }
}