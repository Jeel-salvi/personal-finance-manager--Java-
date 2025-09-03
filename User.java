import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private double savings;
    private String username;
    private String password;
    private double monthlyIncome;
    private double totalIncome;
    private double monthlyExpenses;
    private double monthlyAdditionalIncome;
    public static double balance;
    private LinkedList<Transaction> transactions;
    private int currentMonth;
    private String securityAnswer;
    private String mobileNumber;
    private Map<String, Double> expenseCategories;
    private List<Investment> investments;
    private double amountGold;
    private double amountSip;
    private double amountShare;
    private double emi;
    private boolean checkEmi;

    public User(String username, String password, double monthlyIncome, String securityAnswer, String mobileNumber,
            boolean checkEmi, double emi) {
        this.username = username;
        this.password = password;
        this.monthlyIncome = monthlyIncome;
        this.emi = emi;
        this.checkEmi = checkEmi;
        this.totalIncome = 0.0;
        this.monthlyExpenses = 0.0;
        this.monthlyAdditionalIncome = 0.0;
        this.balance = 0.0;
        this.transactions = new LinkedList<>();
        this.investments = new ArrayList<>();
        this.currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        this.securityAnswer = securityAnswer;
        this.mobileNumber = mobileNumber;
        this.savings = monthlyIncome * 0.30;
        this.expenseCategories = new HashMap<>();
        depositMonthlyIncome(); // Initialize balance on user creation
    }

    public double getEmi() {
        return emi;
    }

    public boolean isCheckEmi() {
        return checkEmi;
    }

    // Call this method to save 30% of income
    public double getSavings() {

        return (savings - getGold(getUsername()) - getShare(getUsername()) - getSip(getUsername()));
        // Optionally adjust monthly income if needed
    }

    private void saveSavingsToDatabase(double savings) {
        String sql = "UPDATE users SET savings = ? WHERE username = ?";

        try (PreparedStatement pstmt = FinanceManager.connection.prepareStatement(sql)) {
            pstmt.setDouble(1, savings);
            pstmt.setString(2, this.username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to save savings to the database: " + e.getMessage());
        }
    }

    public double getBalance() {
        return totalIncome - monthlyExpenses - (monthlyIncome * 0.3) - (checkEmi ? emi : 0); // Balance calculation
    }

    public double getMonthlyIncome() {
        return monthlyIncome;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getMonthlyExpenses() {
        return monthlyExpenses;
    }

    public double getMonthlyAdditionalIncome() {
        return monthlyAdditionalIncome;
    }

    public List<Investment> getInvestments() {
        return this.investments;
    }

    public void addInvestment(Investment investment) {
        investments.add(investment);
        // Reduce savings by the investment amount
        saveSavingsToDatabase(getSavings());
        // Save investment to database
        saveInvestmentToDatabase(investment);

    }

    private void saveInvestmentToDatabase(Investment investment) {
        String sql = "INSERT INTO investments (description, amount, category, date, username) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = FinanceManager.connection.prepareStatement(sql)) {
            pstmt.setString(1, investment.getDescription());
            pstmt.setDouble(2, investment.getAmount());
            pstmt.setString(3, investment.getCategory());
            pstmt.setTimestamp(4, new Timestamp(investment.getDate().getTime()));
            pstmt.setString(5, this.username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to save investment to the database: " + e.getMessage());
        }
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        if (transaction.getType().equals("Expense")) {
            monthlyExpenses += transaction.getAmount();
            expenseCategories.put(transaction.getCategory(),
                    expenseCategories.getOrDefault(transaction.getCategory(), 0.0) + transaction.getAmount());
        } else {
            totalIncome += transaction.getAmount();
            monthlyAdditionalIncome += transaction.getAmount();
        }
        balance = totalIncome - monthlyExpenses; // Recalculate balance
    }

    public LinkedList<Transaction> getTransactions() {
        return transactions;
    }

    public void depositMonthlyIncome() {

        totalIncome = monthlyIncome; // Set monthly income as total income
        balance = totalIncome - monthlyExpenses - getSavings() - emi; // Recalculate balance
        monthlyAdditionalIncome = 0; // Reset additional income

    }

    public boolean canAddExpense(double amount) {
        return (monthlyExpenses + amount) <= (monthlyIncome * 0.7); // Allowable expenses should not exceed 70% of
                                                                    // yearly
                                                                    // income
    }

    public void checkAndDepositMonthlyIncome() {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);

        // Check if the month has changed
        if (this.currentMonth != currentMonth) {
            this.currentMonth = currentMonth;

            // Deposit income at the beginning of each month
            depositMonthlyIncome();
        }
    }

    public Map<String, Double> getExpenseCategories() {
        return expenseCategories;
    }

    // Getters and setters for username, password, and security answer
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public double getGold(String username) {
        amountGold = 0.0;

        String sql = "SELECT * FROM investments WHERE username = ? AND category='Gold'";

        try (PreparedStatement pstmt = FinanceManager.connection.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double temp = rs.getDouble("amount");
                    amountGold = temp + amountGold;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading user from database: " + e.getMessage());
        }

        return amountGold;
    }

    public double getSip(String username) {

        amountSip = 0.0;
        String sql = "SELECT * FROM investments WHERE username = ? AND category='SIP'";

        try (PreparedStatement pstmt = FinanceManager.connection.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double temp = rs.getDouble("amount");
                    amountSip = temp + amountSip;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading user from database: " + e.getMessage());
        }

        return amountSip;
    }

    public double getShare(String username) {
        amountShare = 0.0;

        String sql = "SELECT * FROM investments WHERE username = ? AND category='Shares (Long-Term)'";

        try (PreparedStatement pstmt = FinanceManager.connection.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double temp = rs.getDouble("amount");
                    amountShare = temp + amountShare;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading user from database: " + e.getMessage());
        }

        return amountShare;
    }

}

 class Stack<T> {

    public void push(Investment investment) {
        throw new UnsupportedOperationException("Unimplemented method 'push'");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Unimplemented method 'isEmpty'");
    }

    public Investment pop() {
        throw new UnsupportedOperationException("Unimplemented method 'pop'");
    }

}

