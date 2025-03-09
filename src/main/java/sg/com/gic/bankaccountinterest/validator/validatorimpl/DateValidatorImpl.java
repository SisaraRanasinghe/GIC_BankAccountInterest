package sg.com.gic.bankaccountinterest.validator.validatorimpl;

import sg.com.gic.bankaccountinterest.validator.DateValidator;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateValidatorImpl implements DateValidator {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void validateDate(String date) {
        if (!isValidDate(date)) {
            throw new IllegalArgumentException("Invalid date. Date should be in YYYYMMdd format and must be a valid calendar date.");
        }
    }

    @Override
    public boolean isValidDate(String date) {
        if (date == null || !date.matches("^\\d{8}$")) {
            return false;
        }

        try {
            dateFormat.setLenient(false);
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}