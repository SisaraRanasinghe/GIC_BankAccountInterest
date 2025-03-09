package sg.com.gic.bankaccountinterest.console;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.com.gic.bankaccountinterest.service.BankService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsoleUITest {
    @Mock
    private BankService bankService;

    private ConsoleUI consoleUI;

    // Setup for capturing console output
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // Store original System.in to restore after tests
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void provideInput(String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.getBytes());
        System.setIn(inputStream);
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private ConsoleUI createConsoleUIWithInput(String input) {
        provideInput(input);
        return new ConsoleUI(bankService);
    }

    @Test
    void testStartQuitImmediatelyShouldExitGracefully() {
        consoleUI = createConsoleUIWithInput("Q\n");
        consoleUI.start();

        String output = getOutput();
        assertTrue(output.contains("Welcome to AwesomeGIC Bank"));
        assertTrue(output.contains("Thank you for banking with AwesomeGIC Bank"));

        // Verify no interactions with bankService
        verifyNoInteractions(bankService);
    }

    @Test
    void testStartInputTransactionThenQuitShouldProcessTransaction() {
        String transactionDetails = "20250101 AC001 D 100.00";
        consoleUI = createConsoleUIWithInput("T\n" + transactionDetails + "\n\nQ\n");
        consoleUI.start();

        String output = getOutput();
        assertTrue(output.contains("Please enter transaction details in <Date> <Account> <Type> <Amount> format"));
        assertTrue(output.contains("Is there anything else you'd like to do?"));

        // Verify interaction with bankService
        verify(bankService, times(1)).processTransaction(transactionDetails);
    }

    @Test
    void testStartDefineInterestRuleThenQuitShouldAddInterestRule() {
        String interestRuleDetails = "20250101 RULE01 5.0";
        consoleUI = createConsoleUIWithInput("I\n" + interestRuleDetails + "\n\nQ\n");
        consoleUI.start();

        String output = getOutput();
        assertTrue(output.contains("Please enter interest rule details in <Date> <RuleId> <Rate in %> format"));

        // Verify interaction with bankService
        verify(bankService, times(1)).addInterestRule(interestRuleDetails);
    }

    @Test
    void testStartPrintStatementThenQuitShouldPrintStatement() {
        String statementDetails = "AC001 202501";
        consoleUI = createConsoleUIWithInput("P\n" + statementDetails + "\n\nQ\n");
        consoleUI.start();

        String output = getOutput();
        assertTrue(output.contains("Please enter account and month to generate the statement <Account> <Year><Month>"));

        // Verify interaction with bankService
        verify(bankService, times(1)).printStatement(statementDetails);
    }
}