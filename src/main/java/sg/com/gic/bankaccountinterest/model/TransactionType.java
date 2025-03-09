package sg.com.gic.bankaccountinterest.model;

public enum TransactionType {
    DEPOSIT("D"),
    WITHDRAWAL("W"),
    INTEREST("I");

    private final String code;

    TransactionType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static TransactionType fromCode(String code) {
        if (code.equalsIgnoreCase("D")) {
            return DEPOSIT;
        } else if (code.equalsIgnoreCase("W")) {
            return WITHDRAWAL;
        } else if (code.equalsIgnoreCase("I")) {
            return INTEREST;
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + code);
        }
    }

    @Override
    public String toString() {
        return code;
    }
}
