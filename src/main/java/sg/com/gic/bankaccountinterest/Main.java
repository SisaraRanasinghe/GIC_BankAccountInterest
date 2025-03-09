package sg.com.gic.bankaccountinterest;

import sg.com.gic.bankaccountinterest.console.ConsoleUI;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.repository.InterestRuleRepository;
import sg.com.gic.bankaccountinterest.repository.repositoryimpl.AccountRepositoryImpl;
import sg.com.gic.bankaccountinterest.repository.repositoryimpl.InterestRuleRepositoryImpl;
import sg.com.gic.bankaccountinterest.service.AccountService;
import sg.com.gic.bankaccountinterest.service.BankService;
import sg.com.gic.bankaccountinterest.service.InterestCalculator;
import sg.com.gic.bankaccountinterest.service.TransactionService;
import sg.com.gic.bankaccountinterest.service.serviceimpl.AccountServiceImpl;
import sg.com.gic.bankaccountinterest.service.serviceimpl.BankServiceImpl;
import sg.com.gic.bankaccountinterest.service.serviceimpl.InterestCalculatorImpl;
import sg.com.gic.bankaccountinterest.service.serviceimpl.TransactionServiceImpl;
import sg.com.gic.bankaccountinterest.validator.DateValidator;
import sg.com.gic.bankaccountinterest.validator.validatorimpl.DateValidatorImpl;

public class Main {
    public static void main(String[] args) {

        // Initialize repositories
        AccountRepository accountRepository = new AccountRepositoryImpl();
        InterestRuleRepository interestRuleRepository = new InterestRuleRepositoryImpl();

        // Initialize validators
        DateValidator dateValidator = new DateValidatorImpl();

        // Initialize services
        InterestCalculator interestCalculator = new InterestCalculatorImpl(
                interestRuleRepository, accountRepository);

        TransactionService transactionService = new TransactionServiceImpl(
                accountRepository, dateValidator);

        AccountService accountService = new AccountServiceImpl(
                accountRepository, interestCalculator, dateValidator);

        // Main bank service that coordinates all operations
        BankService bankService = new BankServiceImpl(
                accountService,
                transactionService,
                interestRuleRepository,
                dateValidator);

        // Start the console UI
        ConsoleUI consoleUI = new ConsoleUI(bankService);
        consoleUI.start();
    }
}
