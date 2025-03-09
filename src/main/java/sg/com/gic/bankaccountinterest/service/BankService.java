package sg.com.gic.bankaccountinterest.service;

public interface BankService {
    void processTransaction(String input);

    void addInterestRule(String input);

    void printStatement(String input);

}
