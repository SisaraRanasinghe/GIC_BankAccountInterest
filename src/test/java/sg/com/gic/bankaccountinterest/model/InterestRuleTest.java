package sg.com.gic.bankaccountinterest.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterestRuleTest {
    @Test
    void testConstructorAndGetters() {
        String date = "20250101";
        String ruleId = "RULE01";
        BigDecimal rate = new BigDecimal("0.05");

        InterestRule interestRule = new InterestRule(date, ruleId, rate);

        assertEquals(date, interestRule.getDate());
        assertEquals(ruleId, interestRule.getRuleId());
        assertEquals(rate, interestRule.getRate());
    }
}