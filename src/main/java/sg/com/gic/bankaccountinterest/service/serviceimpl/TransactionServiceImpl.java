package sg.com.gic.bankaccountinterest.service.serviceimpl;

import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.model.TransactionType;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.service.TransactionService;
import sg.com.gic.bankaccountinterest.validator.DateValidator;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final DateValidator dateValidator;

    public TransactionServiceImpl(AccountRepository accountRepository, DateValidator dateValidator) {
        this.accountRepository = accountRepository;
        this.dateValidator = dateValidator;
    }

    @Override
    public void processTransaction(String date, String accountId, String typeStr, String amountStr) {
        if (date == null || !date.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("Invalid date format. Date should be in YYYYMMdd format (8 digits only).");
        }
        dateValidator.validateDate(date);

        if (!accountId.matches("^[\\w\\W]{1,10}$")) {
            throw new IllegalArgumentException("Invalid account name. It should be up to 10 characters.");
        }

        TransactionType type;
        try {
            type = TransactionType.fromCode(typeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Transaction type must be D (deposit) or W (withdrawal)");
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Amount. Please enter a valid numeric value.");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        // Check balance for withdrawals
        BigDecimal balance = accountRepository.calculateBalanceAtDate(accountId, date);
        if (type == TransactionType.WITHDRAWAL && balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance for withdrawal");
        }

        // Generate transaction ID and create transaction
        String transactionId = generateTransactionId(date, accountId);
        Transaction transaction = new Transaction(date, transactionId, type, amount);

        // Add transaction to account
        accountRepository.addTransaction(accountId, transaction);

        // Display account statement
        System.out.println("Account: " + accountId);
        System.out.println("| Date     | Txn Id      | Type | Amount   |");

        // Get all transactions for this account and display
        accountRepository.findByAccountId(accountId).ifPresent(account -> {
            account.getTransactions().stream()
                    .sorted(Comparator.comparing(Transaction::getDate))
                    .forEach(txn -> System.out.println(txn));
        });
    }

    @Override
    public String generateTransactionId(String date, String accountId) {
        // Get all transactions for this account and date
        List<Transaction> transactions = accountRepository.findByAccountId(accountId)
                .map(account -> account.getTransactions())
                .orElse(List.of());

        // Count transactions with the same date
        long count = transactions.stream()
                .filter(t -> t.getDate().equals(date))
                .count() + 1;

        return date + "-" + String.format("%02d", count);
    }
}