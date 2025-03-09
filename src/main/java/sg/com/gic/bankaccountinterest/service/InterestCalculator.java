package sg.com.gic.bankaccountinterest.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InterestCalculator {
    BigDecimal calculateMonthlyInterest(String accountId, LocalDate startDate, LocalDate endDate);
}
