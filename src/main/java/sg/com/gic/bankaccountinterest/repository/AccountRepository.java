package sg.com.gic.bankaccountinterest.repository;

import sg.com.gic.bankaccountinterest.model.Account;
import sg.com.gic.bankaccountinterest.model.Transaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByAccountId(String accountId);

    void saveAccount(Account account);

    void addTransaction(String accountId, Transaction transaction);

    BigDecimal calculateBalanceAtDate(String accountId, String date);

    BigDecimal calculateBalanceBeforeDate(String accountId, String date);

    List<Transaction> getTransactionsInPeriod(String accountId, String startDate, String endDate);
}
