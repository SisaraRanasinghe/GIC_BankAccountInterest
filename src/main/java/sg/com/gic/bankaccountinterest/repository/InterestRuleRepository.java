package sg.com.gic.bankaccountinterest.repository;

import sg.com.gic.bankaccountinterest.model.InterestRule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InterestRuleRepository {
    void saveInterestRule(InterestRule interestRule);

    List<InterestRule> getAllRules();

    Optional<InterestRule> getApplicableRuleForDate(LocalDate date);

    void deleteRulesByDate(String date);
}
