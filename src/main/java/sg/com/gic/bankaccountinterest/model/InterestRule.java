package sg.com.gic.bankaccountinterest.model;

import java.math.BigDecimal;

public class InterestRule {
    private final String date;
    private final String ruleId;
    private final BigDecimal rate;

    public InterestRule(String date, String ruleId, BigDecimal rate) {
        this.date = date;
        this.ruleId = ruleId;
        this.rate = rate;
    }

    public String getDate() {
        return date;
    }

    public String getRuleId() {
        return ruleId;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return String.format("| %-8s | %-6s | %8.2f |", date, ruleId, rate);
    }
}
