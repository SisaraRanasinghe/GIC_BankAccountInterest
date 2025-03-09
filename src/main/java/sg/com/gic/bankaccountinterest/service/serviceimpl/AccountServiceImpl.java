package sg.com.gic.bankaccountinterest.service.serviceimpl;

import sg.com.gic.bankaccountinterest.model.Account;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.model.TransactionType;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.service.AccountService;
import sg.com.gic.bankaccountinterest.service.InterestCalculator;
import sg.com.gic.bankaccountinterest.validator.DateValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final InterestCalculator interestCalculator;
    private final DateValidator dateValidator;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public AccountServiceImpl(AccountRepository accountRepository,
                              InterestCalculator interestCalculator,
                              DateValidator dateValidator) {
        this.accountRepository = accountRepository;
        this.interestCalculator = interestCalculator;
        this.dateValidator = dateValidator;
    }

    @Override
    public void printStatement(String accountId, String yearMonth) {
        // Validate yearMonth format
        if (!yearMonth.matches("^\\d{6}$")) {
            throw new IllegalArgumentException("Invalid date format. Date should be in YYYYMM format.");
        }

        String year = yearMonth.substring(0, 4);
        String month = yearMonth.substring(4, 6);
        String fullDate = year + month + "01";

        dateValidator.validateDate(fullDate);

        int yearInt = Integer.parseInt(year);
        int monthInt = Integer.parseInt(month);
        YearMonth yearMonthObj = YearMonth.of(yearInt, monthInt);

        LocalDate startDate = yearMonthObj.atDay(1);
        LocalDate endDate = yearMonthObj.atEndOfMonth();

        String startDateStr = startDate.format(DATE_FORMAT);
        String endDateStr = endDate.format(DATE_FORMAT);

        Optional<Account> accountOptional = accountRepository.findByAccountId(accountId);
        if (!accountOptional.isPresent()) {
            throw new IllegalArgumentException("Account does not exist. Unable to generate print statement.");
        }

        List<Transaction> transactions = accountRepository.getTransactionsInPeriod(accountId, startDateStr, endDateStr);
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("No transactions found for the given date. Unable to generate print statement.");
        }

        transactions.sort(Comparator.comparing(Transaction::getDate));

        BigDecimal openingBalance = accountRepository.calculateBalanceBeforeDate(accountId, startDateStr);
        BigDecimal runningBalance = openingBalance;

        System.out.println("Account: " + accountId);
        System.out.println("| Date     | Txn Id      | Type | Amount   | Balance  |");

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.DEPOSIT || t.getType() == TransactionType.INTEREST) {
                runningBalance = runningBalance.add(t.getAmount());
            } else if (t.getType() == TransactionType.WITHDRAWAL) {
                runningBalance = runningBalance.subtract(t.getAmount());
            }

            System.out.printf("| %-8s | %-11s | %-4s | %8.2f | %8.2f |%n",
                    t.getDate(), t.getId(), t.getType(), t.getAmount(), runningBalance);
        }

        // Calculate and print interest
        BigDecimal interest = interestCalculator.calculateMonthlyInterest(accountId, startDate, endDate);
        runningBalance = runningBalance.add(interest);

        // Add interest as a transaction
        Transaction interestTransaction = new Transaction(
                endDateStr,
                "",
                TransactionType.INTEREST,
                interest);

        System.out.printf("| %-8s | %-11s | %-4s | %8.2f | %8.2f |%n",
                endDateStr, "", TransactionType.INTEREST, interest, runningBalance);
    }

    @Override
    public boolean accountExists(String accountId) {
        return accountRepository.findByAccountId(accountId).isPresent();
    }
}