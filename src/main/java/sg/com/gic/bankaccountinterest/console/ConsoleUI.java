package sg.com.gic.bankaccountinterest.console;

import sg.com.gic.bankaccountinterest.service.BankService;
import java.util.Scanner;

public class ConsoleUI {
    private final Scanner scanner = new Scanner(System.in);
    private final BankService bankService;
    private boolean firstRun = true;

    public ConsoleUI(BankService bankService) {
        this.bankService = bankService;
    }

    public void start() {
        System.out.println("Welcome to AwesomeGIC Bank! What would you like to do?");

        while (true) {
            if (!firstRun) {
                System.out.println("Is there anything else you'd like to do?");
            }
            firstRun = false;

            displayOptions();

            System.out.print("> ");
            String choice = scanner.nextLine().trim().toUpperCase();
            switch (choice) {
                case "T":
                    inputTransaction();
                    break;
                case "I":
                    defineInterestRule();
                    break;
                case "P":
                    printStatement();
                    break;
                case "Q":
                    System.out.println("Thank you for banking with AwesomeGIC Bank. Have a nice day!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void displayOptions() {
        System.out.println("[T] Input transactions");
        System.out.println("[I] Define interest rules");
        System.out.println("[P] Print statement");
        System.out.println("[Q] Quit");
    }

    private void inputTransaction() {
        System.out.println("Please enter transaction details in <Date> <Account> <Type> <Amount> format");
        System.out.println("(or enter blank to go back to main menu):");
        handleUserInput(bankService::processTransaction);
    }

    private void defineInterestRule() {
        System.out.println("Please enter interest rule details in <Date> <RuleId> <Rate in %> format");
        System.out.println("(or enter blank to go back to main menu):");
        handleUserInput(bankService::addInterestRule);
    }

    private void printStatement() {
        System.out.println("Please enter account and month to generate the statement <Account> <Year><Month>");
        System.out.println("(or enter blank to go back to main menu):");
        handleUserInput(bankService::printStatement);
    }

    private void handleUserInput(CommandAction action) {
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (input.trim().isEmpty() || input.equals(" ")) {
                return;
            }

            try {
                action.execute(input);
                break;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}