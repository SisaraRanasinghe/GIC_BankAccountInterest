package sg.com.gic.bankaccountinterest.service.serviceimpl;

import sg.com.gic.bankaccountinterest.model.InterestRule;
import sg.com.gic.bankaccountinterest.repository.InterestRuleRepository;
import sg.com.gic.bankaccountinterest.service.AccountService;
import sg.com.gic.bankaccountinterest.service.BankService;
import sg.com.gic.bankaccountinterest.service.TransactionService;
import sg.com.gic.bankaccountinterest.validator.DateValidator;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class BankServiceImpl implements BankService {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final InterestRuleRepository interestRuleRepository;
    private final DateValidator dateValidator;

    public BankServiceImpl(AccountService accountService,
                           TransactionService transactionService,
                           InterestRuleRepository interestRuleRepository,
                           DateValidator dateValidator) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.interestRuleRepository = interestRuleRepository;
        this.dateValidator = dateValidator;
    }

    @Override
    public void processTransaction(String input) {
        String[] parts = input.split(" ");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid transaction format. Use <Date> <Account> <Type> <Amount>");
        }

        String date = parts[0];
        String accountId = parts[1];
        String type = parts[2];
        String amount = parts[3];

        transactionService.processTransaction(date, accountId, type, amount);
    }

    @Override
    public void addInterestRule(String input) {
        String[] parts = input.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid interest rule format. Use <Date> <RuleId> <Rate>");
        }

        String date = parts[0];
        String ruleId = parts[1];
        String rateStr = parts[2];

        if (date == null || !date.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("Invalid date format. Date should be in YYYYMMdd format (8 digits only).");
        }

        dateValidator.validateDate(date);

        if (!ruleId.matches("^[\\w\\W]{1,10}$")) {
            throw new IllegalArgumentException("Invalid Rule ID. It should be up to 10 characters.");
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Rate. Please enter a valid numeric value.");
        }

        if (rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(new BigDecimal("100")) >= 0) {
            throw new IllegalArgumentException("Interest rate must be between 0 and 100.");
        }

        // Delete any existing rules for the same date
        interestRuleRepository.deleteRulesByDate(date);

        // Add the new rule
        InterestRule newRule = new InterestRule(date, ruleId, rate);
        interestRuleRepository.saveInterestRule(newRule);

        // Display all rules
        List<InterestRule> allRules = interestRuleRepository.getAllRules();
        allRules.sort(Comparator.comparing(InterestRule::getDate));

        System.out.println("Interest rules:");
        System.out.println("| Date     | RuleId | Rate (%) |");
        for (InterestRule rule : allRules) {
            System.out.println(rule);
        }
    }

    @Override
    public void printStatement(String input) {
        String[] parts = input.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid statement format. Use <Account> <Year><Month>");
        }

        String accountId = parts[0];
        String yearMonth = parts[1];

        accountService.printStatement(accountId, yearMonth);
    }
}
