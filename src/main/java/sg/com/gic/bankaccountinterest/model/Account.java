package sg.com.gic.bankaccountinterest.model;

import java.util.ArrayList;
import java.util.List;

public class Account {
    private final String accountId;
    private final List<Transaction> transactions;

    public Account(String accountId) {
        this.accountId = accountId;
        this.transactions = new ArrayList<>();
    }

    public String getAccountId() {
        return accountId;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
}

