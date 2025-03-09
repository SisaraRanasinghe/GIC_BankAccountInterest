package sg.com.gic.bankaccountinterest.service;

public interface AccountService {
    void printStatement(String account, String yearMonth);

    boolean accountExists(String accountId);
}
