package sg.com.gic.bankaccountinterest.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionTypeTest {
    @Test
    void testEnumConstantsShouldHaveCorrectCodes() {
        assertEquals("D", TransactionType.DEPOSIT.getCode());
        assertEquals("W", TransactionType.WITHDRAWAL.getCode());
        assertEquals("I", TransactionType.INTEREST.getCode());
    }

    @ParameterizedTest
    @MethodSource("provideValidCodes")
    void testFromCodeShouldReturnCorrectEnum(String code, TransactionType expectedType) {
        assertEquals(expectedType, TransactionType.fromCode(code));
    }

    private static Stream<Arguments> provideValidCodes() {
        return Stream.of(
                // Test both uppercase and lowercase for each code
                Arguments.of("D", TransactionType.DEPOSIT),
                Arguments.of("d", TransactionType.DEPOSIT),
                Arguments.of("W", TransactionType.WITHDRAWAL),
                Arguments.of("w", TransactionType.WITHDRAWAL),
                Arguments.of("I", TransactionType.INTEREST),
                Arguments.of("i", TransactionType.INTEREST)
        );
    }
}