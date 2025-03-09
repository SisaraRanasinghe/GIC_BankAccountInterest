# GIC_BankAccountInterest - Bank Account Interest Application

This console application implements a simple banking system that handles operations on bank accounts including transaction processing, interest calculation, and statement printing.

## Getting Started

### Clone the Repository
```
git clone https://github.com/SisaraRanasinghe/GIC_BankAccountInterest.git
cd repository-name
```

## Prerequisites
* Java 8 or higher
* Maven 3.6.0 or higher

## Building the Application
```
mvn clean install -DskipTests
```

## Running Tests
```
mvn test
```

## Code Coverage Report
To generate and view the JaCoCo code coverage report:
```
mvn verify
```
The coverage report will be available at: `target/site/jacoco/index.html`

## Running the Application
```
java -cp target/BankAccountInterest-1.0-SNAPSHOT.jar [main-class-path]
```
Replace `[jar-name]` with your actual JAR file name and `[main-class-path]` with your main class path.

## Application Usage

### Main Menu
Upon starting the application, you'll see the main menu:
```
Welcome to AwesomeGIC Bank! What would you like to do?
[T] Input transactions
[I] Define interest rules
[P] Print statement
[Q] Quit
>
```

### Input Transactions
To record deposits and withdrawals:
```
Please enter transaction details in <Date> <Account> <Type> <Amount> format 
(or enter blank to go back to main menu):
> 20230626 AC001 W 100.00
```
* Date format: YYYYMMDD
* Account: Account ID (free format)
* Type: 'D' for deposit, 'W' for withdrawal (case insensitive)
* Amount: Transaction amount (must be > 0, up to 2 decimal places)

The system automatically creates accounts when the first transaction for the account is recorded.
Each transaction is assigned a unique ID in YYYYMMdd-xx format, where xx is a running number.

**Constraints:**
* An account's balance cannot be less than 0
* The first transaction for a new account must be a deposit
* Any transaction that would make the balance go below 0 will be rejected

After entering a transaction, the system shows the account statement:
```
Account: AC001
| Date     | Txn Id      | Type | Amount |
| 20230505 | 20230505-01 | D    | 100.00 |
| 20230601 | 20230601-01 | D    | 150.00 |
| 20230626 | 20230626-01 | W    |  20.00 |
| 20230626 | 20230626-02 | W    | 100.00 |
```

### Define Interest Rules
To set interest rates:
```
Please enter interest rule details in <Date> <RuleId> <Rate in %> format 
(or enter blank to go back to main menu):
> 20230615 RULE03 2.20
```
* Date format: YYYYMMDD
* RuleId: Unique identifier for the interest rule (free format)
* Rate: Interest rate percentage (must be > 0 and < 100)

**Constraints:**
* If there are any existing rules on the same day, the latest one is kept

After entering a rule, the system shows all interest rules ordered by date:
```
Interest rules:
| Date     | RuleId | Rate (%) |
| 20230101 | RULE01 |     1.95 |
| 20230520 | RULE02 |     1.90 |
| 20230615 | RULE03 |     2.20 |
```

### Print Statement
To generate an account statement for a specific month:
```
Please enter account and month to generate the statement <Account> <Year><Month> 
(or enter blank to go back to main menu):
> AC001 202306
```
* Account: Account ID
* YearMonth: In YYYYMM format

The system will display all transactions for the specified account and month, including the interest calculation at the end of the month:
```
Account: AC001
| Date     | Txn Id      | Type | Amount | Balance |
| 20230601 | 20230601-01 | D    | 150.00 |  250.00 |
| 20230626 | 20230626-01 | W    |  20.00 |  230.00 |
| 20230626 | 20230626-02 | W    | 100.00 |  130.00 |
| 20230630 |             | I    |   0.39 |  130.39 |
```

**Interest Calculation:**
* Interest is applied on end-of-day balance
* The system calculates interest based on the applicable rules for each day
* Interest is credited on the last day of the month
* Interest is calculated daily and then summed for the entire month
* The daily interest is: Balance * Rate * Days / 365

### Quit
To exit the application:
```
Q
```

The system will respond with:
```
Thank you for banking with AwesomeGIC Bank.
Have a nice day!
```
