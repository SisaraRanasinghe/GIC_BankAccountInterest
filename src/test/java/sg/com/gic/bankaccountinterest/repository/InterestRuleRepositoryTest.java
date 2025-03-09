package sg.com.gic.bankaccountinterest.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.com.gic.bankaccountinterest.model.InterestRule;
import sg.com.gic.bankaccountinterest.repository.repositoryimpl.InterestRuleRepositoryImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InterestRuleRepositoryTest {
    private InterestRuleRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InterestRuleRepositoryImpl();
    }

    @Test
    void testShouldAddRuleToRepository() {
        InterestRule rule = new InterestRule("20250101", "RULE01", new BigDecimal("3.5"));
        repository.saveInterestRule(rule);

        List<InterestRule> rules = repository.getAllRules();
        assertEquals(1, rules.size());
        assertEquals("20250101", rules.get(0).getDate());
        assertEquals("RULE01", rules.get(0).getRuleId());
        assertEquals(0, new BigDecimal("3.5").compareTo(rules.get(0).getRate()));
    }

    @Test
    void testShouldReturnMostRecentRuleNotExceedingDate() {
        InterestRule rule1 = new InterestRule("20220101", "RULE01", new BigDecimal("3.0"));
        InterestRule rule2 = new InterestRule("20230101", "RULE02", new BigDecimal("3.5"));
        InterestRule rule3 = new InterestRule("20240101", "RULE03", new BigDecimal("4.0"));

        repository.saveInterestRule(rule1);
        repository.saveInterestRule(rule2);
        repository.saveInterestRule(rule3);

        // Date exactly matches a rule
        Optional<InterestRule> result1 = repository.getApplicableRuleForDate(LocalDate.of(2023, 1, 1));
        assertTrue(result1.isPresent());
        assertEquals("20230101", result1.get().getDate());
        assertEquals("RULE02", result1.get().getRuleId());

        // Date is between two rules
        Optional<InterestRule> result2 = repository.getApplicableRuleForDate(LocalDate.of(2023, 6, 1));
        assertTrue(result2.isPresent());
        assertEquals("20230101", result2.get().getDate());

        // Date is before all rules
        Optional<InterestRule> result3 = repository.getApplicableRuleForDate(LocalDate.of(2021, 1, 1));
        assertFalse(result3.isPresent());

        // Date is after all rules
        Optional<InterestRule> result4 = repository.getApplicableRuleForDate(LocalDate.of(2025, 1, 1));
        assertTrue(result4.isPresent());
        assertEquals("20240101", result4.get().getDate());
    }
}
