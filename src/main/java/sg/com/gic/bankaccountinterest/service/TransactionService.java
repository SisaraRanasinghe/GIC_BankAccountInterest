package sg.com.gic.bankaccountinterest.service;

public interface TransactionService {
    void processTransaction(String date, String accountId, String type, String amountStr);

    String generateTransactionId(String date, String accountId);
}
