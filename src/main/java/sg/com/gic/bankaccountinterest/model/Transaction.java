package sg.com.gic.bankaccountinterest.model;

import java.math.BigDecimal;

public class Transaction {
    private final String date;
    private final String id;
    private final TransactionType type;
    private final BigDecimal amount;

    public Transaction(String date, String id, TransactionType type, BigDecimal amount) {
        this.date = date;
        this.id = id;
        this.type = type;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("| %-8s | %-10s | %-4s | %8.2f |", date, id, type, amount);
    }
}
