package sg.com.gic.bankaccountinterest.service.serviceimpl;

import sg.com.gic.bankaccountinterest.model.InterestRule;
import sg.com.gic.bankaccountinterest.model.Transaction;
import sg.com.gic.bankaccountinterest.repository.AccountRepository;
import sg.com.gic.bankaccountinterest.repository.InterestRuleRepository;
import sg.com.gic.bankaccountinterest.service.InterestCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class InterestCalculatorImpl implements InterestCalculator {
    private final InterestRuleRepository interestRuleRepository;
    private final AccountRepository accountRepository;
    private static final int DAYS_IN_YEAR = 365;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public InterestCalculatorImpl(InterestRuleRepository interestRuleRepository, AccountRepository accountRepository) {
        this.interestRuleRepository = interestRuleRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public BigDecimal calculateMonthlyInterest(String accountId, LocalDate startDate, LocalDate endDate) {
        // Check if we have interest rules defined
        List<InterestRule> rules = interestRuleRepository.getAllRules();
        if (rules.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Create a sorted list of all dates where the balance changes
        Set<LocalDate> balanceChangeDates = new TreeSet<>();

        // Add start date of the month
        balanceChangeDates.add(startDate);

        // Add transaction dates that change balance
        List<String> transactionDates = accountRepository.getTransactionsInPeriod(accountId,
                        startDate.format(DATE_FORMAT),
                        endDate.format(DATE_FORMAT))
                .stream()
                .map(Transaction::getDate)
                .distinct()
                .collect(Collectors.toList());

        for (String txnDate : transactionDates) {
            balanceChangeDates.add(LocalDate.parse(txnDate, DATE_FORMAT));
        }

        // Add interest rule effective dates that fall in this month
        for (InterestRule rule : rules) {
            LocalDate ruleDate = LocalDate.parse(rule.getDate(), DATE_FORMAT);
            if (!ruleDate.isBefore(startDate) && !ruleDate.isAfter(endDate)) {
                balanceChangeDates.add(ruleDate);
            }
        }

        // Convert to sorted list
        List<LocalDate> datePoints = new ArrayList<>(balanceChangeDates);
        Collections.sort(datePoints);

        // Add end date if not already in the list
        if (!datePoints.contains(endDate)) {
            datePoints.add(endDate);
        }

        BigDecimal annualizedInterestSum = BigDecimal.ZERO;

        // Calculate interest for each period
        for (int i = 0; i < datePoints.size() - 1; i++) {
            LocalDate periodStart = datePoints.get(i);

            // IMPORTANT CHANGE: Don't subtract a day from the next date point
            // Instead, use the date before the next date point as the period end
            LocalDate periodEnd;
            if (i < datePoints.size() - 2) {
                // For all periods except the last one, end just before the next change
                periodEnd = datePoints.get(i + 1).minusDays(1);
            } else {
                // For the last period, use the endDate directly
                periodEnd = endDate;
            }

            // Skip if period is invalid
            if (periodEnd.isBefore(periodStart)) {
                continue;
            }

            // Get balance at start of period (which is the EOD balance for the entire period)
            BigDecimal periodBalance = accountRepository.calculateBalanceAtDate(
                    accountId, periodStart.format(DATE_FORMAT));

            // Get applicable interest rule
            Optional<InterestRule> applicableRuleOpt = interestRuleRepository.getApplicableRuleForDate(periodStart);
            if (!applicableRuleOpt.isPresent()) {
                continue; // Skip if no rule is applicable
            }

            InterestRule applicableRule = applicableRuleOpt.get();

            // Calculate number of days in this period
            long daysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1; // +1 because end date is inclusive

            // Calculate annualized interest for this period (Balance * Rate% * Days)
            BigDecimal periodAnnualizedInterest = periodBalance
                    .multiply(applicableRule.getRate())
                    .multiply(BigDecimal.valueOf(daysInPeriod));

            annualizedInterestSum = annualizedInterestSum.add(periodAnnualizedInterest);
        }

        // Calculate actual interest by dividing the sum by (365 * 100)
        // The division by 100 is to convert percentage to decimal
        BigDecimal actualInterest = annualizedInterestSum
                .divide(BigDecimal.valueOf(DAYS_IN_YEAR * 100), 10, RoundingMode.HALF_UP);

        // Round to 2 decimal places using HALF_UP rounding mode
        // Print the final rounded interest value
        return actualInterest.setScale(2, RoundingMode.HALF_UP);
    }
}
