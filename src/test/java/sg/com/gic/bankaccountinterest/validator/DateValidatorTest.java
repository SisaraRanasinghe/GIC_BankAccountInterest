package sg.com.gic.bankaccountinterest.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.com.gic.bankaccountinterest.validator.validatorimpl.DateValidatorImpl;

import static org.junit.jupiter.api.Assertions.*;

public class DateValidatorTest {
    private DateValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new DateValidatorImpl();
    }

    @Test
    void testValidateDateWithValidDateShouldNotThrowException() {
        assertDoesNotThrow(() -> validator.validateDate("20250101"));
    }

    @Test
    void testValidateDateWithInvalidDateShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateDate("invalid")
        );

        assertEquals("Invalid date. Date should be in YYYYMMdd format and must be a valid calendar date.",
                exception.getMessage());
    }
}
