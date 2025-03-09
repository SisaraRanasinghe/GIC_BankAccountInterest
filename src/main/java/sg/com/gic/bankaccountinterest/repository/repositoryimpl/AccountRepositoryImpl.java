package sg.com.gic.bankaccountinterest.repository.repositoryimpl;

import sg.com.gic.bankaccountinterest.model.Account;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.model.TransactionType;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountRepositoryImpl implements AccountRepository {
    private final Map<String, Account> accounts = new HashMap<>();

    @Override
    public Optional<Account> findByAccountId(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public void saveAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    @Override
    public void addTransaction(String accountId, Transaction transaction) {
        accounts.computeIfAbsent(accountId, Account::new)
                .addTransaction(transaction);
    }

    @Override
    public BigDecimal calculateBalanceAtDate(String accountId, String date) {
        Account account = accounts.get(accountId);
        if (account == null) {
            return BigDecimal.ZERO;
        }

        return account.getTransactions().stream()
                .filter(t -> t.getDate().compareTo(date) <= 0)
                .map(t -> {
                    if (t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.INTEREST) {
                        return t.getAmount();
                    } else if (t.getType() == TransactionType.WITHDRAWAL) {
                        return t.getAmount().negate();
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateBalanceBeforeDate(String accountId, String date) {
        Account account = accounts.get(accountId);
        if (account == null) {
            return BigDecimal.ZERO;
        }

        return account.getTransactions().stream()
                .filter(t -> t.getDate().compareTo(date) < 0)
                .map(t -> {
                    if (t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.INTEREST) {
                        return t.getAmount();
                    } else if (t.getType() == TransactionType.WITHDRAWAL) {
                        return t.getAmount().negate();
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> getTransactionsInPeriod(String accountId, String startDate, String endDate) {
        Account account = accounts.get(accountId);
        if (account == null) {
            return new ArrayList<>();
        }

        return account.getTransactions().stream()
                .filter(t -> t.getDate().compareTo(startDate) >= 0 && t.getDate().compareTo(endDate) <= 0)
                .collect(Collectors.toList());
    }
}
