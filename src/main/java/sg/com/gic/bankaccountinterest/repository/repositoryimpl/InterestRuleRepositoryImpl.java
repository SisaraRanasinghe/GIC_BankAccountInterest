package sg.com.gic.bankaccountinterest.repository.repositoryimpl;

import sg.com.gic.bankaccountinterest.model.InterestRule;
import sg.com.gic.bankaccountinterest.repository.InterestRuleRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InterestRuleRepositoryImpl implements InterestRuleRepository {
    private final List<InterestRule> interestRules = new ArrayList<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void saveInterestRule(InterestRule interestRule) {
        interestRules.add(interestRule);
        interestRules.sort(Comparator.comparing(InterestRule::getDate));
    }

    @Override
    public List<InterestRule> getAllRules() {
        return new ArrayList<>(interestRules);
    }

    @Override
    public Optional<InterestRule> getApplicableRuleForDate(LocalDate date) {
        String dateStr = date.format(DATE_FORMAT);
        return interestRules.stream()
                .filter(rule -> rule.getDate().compareTo(dateStr) <= 0)
                .max(Comparator.comparing(InterestRule::getDate));
    }

    @Override
    public void deleteRulesByDate(String date) {
        interestRules.removeIf(rule -> rule.getDate().equals(date));
    }
}
