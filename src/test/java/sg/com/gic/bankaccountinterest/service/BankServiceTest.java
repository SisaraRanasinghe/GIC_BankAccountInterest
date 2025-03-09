package sg.com.gic.bankaccountinterest.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.bankaccountinterest.model.InterestRule;
import sg.com.gic.bankaccountinterest.repository.InterestRuleRepository;
import sg.com.gic.bankaccountinterest.service.serviceimpl.BankServiceImpl;
import sg.com.gic.bankaccountinterest.validator.DateValidator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankServiceTest {
    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private InterestRuleRepository interestRuleRepository;

    @Mock
    private DateValidator dateValidator;
    private BankServiceImpl bankService;
    private ByteArrayOutputStream outputStreamCaptor;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        bankService = new BankServiceImpl(
                accountService,
                transactionService,
                interestRuleRepository,
                dateValidator
        );
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testConstructorInitializesDependencies() {
        assertNotNull(bankService);
    }

    @Test
    void testProcessTransactionValidInputCallsTransactionService() {
        String validInput = "20250101 AC001 D 100.00";
        bankService.processTransaction(validInput);

        verify(transactionService).processTransaction("20250101", "AC001", "D", "100.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "20250101 AC001 D", // Missing amount
            "20250101 AC001", // Missing type and amount
            "20250101", // Missing account, type, and amount
            "", // Empty string
            "20250101 AC001 D 100.00 EXTRA" // Extra parameter
    })
    void testProcessTransactionInvalidInputThrowsIllegalArgumentException(String invalidInput) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankService.processTransaction(invalidInput)
        );

        assertEquals("Invalid transaction format. Use <Date> <Account> <Type> <Amount>", exception.getMessage());
        verifyNoInteractions(transactionService);

    }

    @Test
    void testAddInterestRuleValidInputSavesRuleAndPrintsAllRules() {
        String validInput = "20250101 RULE01 5.5";

        List<InterestRule> allRules = Arrays.asList(
                new InterestRule("20250101", "RULE01", new BigDecimal("5.5")),
                new InterestRule("20250201", "RULE02", new BigDecimal("6.0"))
        );

        doNothing().when(dateValidator).validateDate("20250101");
        when(interestRuleRepository.getAllRules()).thenReturn(allRules);
        bankService.addInterestRule(validInput);
        verify(interestRuleRepository).deleteRulesByDate("20250101");

        // Capture and verify the saved rule
        ArgumentCaptor<InterestRule> ruleCaptor = ArgumentCaptor.forClass(InterestRule.class);
        verify(interestRuleRepository).saveInterestRule(ruleCaptor.capture());

        InterestRule capturedRule = ruleCaptor.getValue();
        assertEquals("20250101", capturedRule.getDate());
        assertEquals("RULE01", capturedRule.getRuleId());
        assertEquals(new BigDecimal("5.5"), capturedRule.getRate());

        verify(interestRuleRepository).getAllRules();

        String output = outputStreamCaptor.toString().trim();
        assertTrue(output.contains("Interest rules:"));
        assertTrue(output.contains("| Date     | RuleId | Rate (%) |"));
        assertTrue(output.contains("RULE01"));
        assertTrue(output.contains("RULE02"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "20250101 RULE01", // Missing rate
            "20250101", // Missing ruleId and rate
            "", // Empty string
            "20230101 RULE01 5.5 EXTRA" // Extra parameter
    })
    void testAddInterestRuleInvalidInputFormatThrowsIllegalArgumentException(String invalidInput) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankService.addInterestRule(invalidInput)
        );

        assertEquals("Invalid interest rule format. Use <Date> <RuleId> <Rate>", exception.getMessage());
        verifyNoInteractions(interestRuleRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2025010", // Too short
            "202501011", // Too long
            "abcdefgh", // Non-numeric
            "2025/01/01" // Wrong format
    })
    void testAddInterestRuleInvalidDateFormatThrowsIllegalArgumentException(String invalidDate) {
        String invalidInput = invalidDate + " RULE01 5.5";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankService.addInterestRule(invalidInput)
        );

        assertEquals("Invalid date format. Date should be in YYYYMMdd format (8 digits only).", exception.getMessage());
        verifyNoInteractions(interestRuleRepository);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0", "0.0", "-1", "-0.1", "100", "100.0", "101", "200"
    })
    void testAddInterestRuleInvalidRateValueThrowsIllegalArgumentException(String rateStr) {
        String invalidInput = "20250101 RULE01 " + rateStr;
        doNothing().when(dateValidator).validateDate("20250101");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bankService.addInterestRule(invalidInput)
        );

        assertEquals("Interest rate must be between 0 and 100.", exception.getMessage());
        verifyNoInteractions(interestRuleRepository);
    }

    @Test
    void testPrintStatementValidInputCallsAccountService() {
        String validInput = "AC001 202501";
        bankService.printStatement(validInput);

        verify(accountService).printStatement("AC001", "202501");
    }
}
