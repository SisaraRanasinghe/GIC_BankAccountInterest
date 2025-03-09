package sg.com.gic.bankaccountinterest.console;

@FunctionalInterface
interface CommandAction {
    void execute(String input);
}
