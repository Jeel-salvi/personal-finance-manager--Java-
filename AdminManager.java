import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class FinanceManager {
    private Map<String, User> users;
    private User currentUser;
    private Scanner scanner;

    static Connection connection;
    private String[] incomeCategories = { "Salary", "Investment", "Freelancing", "Other" };
    private String[] expenseCategories = { "Groceries", "Rent", "Utilities", "Entertainment", "Other" };

    public FinanceManager() {
        users = new HashMap<>();
        scanner = new Scanner(System.in);
        connectToDatabase();
        createTablesIfNotExist();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/manager";
            String username = "root"; // replace with your MySQL username
            String password = ""; // replace with your MySQL password if you have set one
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    private void createTablesIfNotExist() {
        try (Statement stmt = connection.createStatement()) {
            // Create users table
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "monthlyIncome DOUBLE NOT NULL, " +
                    "securityAnswer VARCHAR(255) NOT NULL, " +
                    "mobileNumber VARCHAR(10) NOT NULL, " +
                    "savings DOUBLE DEFAULT 0 , " +
                    "checkEmi boolean DEFAULT false , " +
                    "emi DOUBLE DEFAULT 0)";
            stmt.execute(createUserTable);

            String createInvestmentTable = "CREATE TABLE IF NOT EXISTS investments (" +
                    "I_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "description VARCHAR(255) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "username VARCHAR(255), " +
                    "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE)";
            stmt.execute(createInvestmentTable);
            System.out.println("Investment table created successfully (if not existing).");

            String createAdminTable = "CREATE TABLE IF NOT EXISTS admin (" +
                    "adminname VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "mobileNumber VARCHAR(10) NOT NULL, " +
                    "securityAnswer VARCHAR(255) NOT NULL" +
                    ")";
            stmt.executeUpdate(createAdminTable);
            System.out.println("Table 'admin' created successfully.(if not existing).");

            // Create transactions table
            String createTransactionTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "T_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "description VARCHAR(255) NOT NULL, " +
                    "amount DOUBLE NOT NULL, " +
                    "type VARCHAR(50) NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "balance DOUBLE NOT NULL, " +
                    "total_income DOUBLE NOT NULL, " + "expenses DOUBLE NOT NULL DEFAULT 0," +

                    " additional_income DOUBLE NOT NULL DEFAULT 0," +
                    "username VARCHAR(255), " +
                    "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE)";

            stmt.execute(createTransactionTable);
            String createIncomeTable = "  CREATE TABLE IF NOT EXISTS income (" +
                    " I_ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    " description VARCHAR(255) NOT NULL, " +
                    " amount DOUBLE NOT NULL, " +
                    " category VARCHAR(50) NOT NULL, " +
                    " date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " balance DOUBLE NOT NULL, " +
                    " total_income DOUBLE NOT NULL, " +

                    " additional_income DOUBLE NOT NULL DEFAULT 0, " +
                    "T_ID INT," +
                    "CONSTRAINT FT  FOREIGN KEY (T_ID) REFERENCES transactions(T_ID) ON DELETE CASCADE)";
            stmt.execute(createIncomeTable);
            String createExpenseTable = " CREATE TABLE IF NOT EXISTS expense ( " +
                    "  E_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    " description VARCHAR(255) NOT NULL, " +
                    " amount DOUBLE NOT NULL, " +
                    " category VARCHAR(50) NOT NULL, " +
                    " date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    " balance DOUBLE NOT NULL, " +

                    " expenses DOUBLE NOT NULL DEFAULT 0, " +
                    "T_ID INT," +
                    "FOREIGN KEY (T_ID) REFERENCES transactions(T_ID) ON DELETE CASCADE)";
            stmt.execute(createExpenseTable);

            String Trigger = "CREATE  TRIGGER IF NOT EXISTS after_transaction_insert " +
                    "AFTER INSERT ON transactions " +
                    "FOR EACH ROW " +
                    "BEGIN " +
                    "    IF NEW.type = 'Income' THEN " +
                    "        INSERT INTO income (description, amount, category, date, balance, total_income,   additional_income,  T_ID) "
                    +
                    "        VALUES (NEW.description, NEW.amount, NEW.category, NEW.date, NEW.balance, NEW.total_income,  NEW.additional_income, NEW.T_ID); "
                    +
                    "    ELSEIF NEW.type = 'Expense' THEN " +
                    "        INSERT INTO expense (description, amount, category, date, balance, expenses,  T_ID) " +
                    "        VALUES (NEW.description, NEW.amount, NEW.category, NEW.date, NEW.balance, NEW.expenses,  NEW.T_ID); "
                    +
                    "    END IF; " +
                    "END;";
            stmt.execute(Trigger);
            System.out.println("Trigger and table created successfully (if not existing).");
        } catch (SQLException e) {
            System.out.println("Failed to create transactions table: " + e.getMessage());
        }
    }

    private boolean saveUserToDatabase(User user) {
        String sql = "INSERT INTO users (username, password, monthlyIncome, securityAnswer, mobileNumber, savings,checkEmi,emi) VALUES (?, ?, ?, ?, ?, ?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setDouble(3, user.getMonthlyIncome());
            pstmt.setString(4, user.getSecurityAnswer());
            pstmt.setString(5, user.getMobileNumber());
            pstmt.setDouble(6, user.getSavings());
            pstmt.setBoolean(7, user.isCheckEmi());
            pstmt.setDouble(8, user.getEmi());
            pstmt.executeUpdate();
            System.out.println("User saved to the database.");
            return true;
        } catch (SQLException e) {
            System.out.println("Failed to save user to the database: " + e.getMessage());
            return false;
        }
    }

    private void saveTransactionToDatabase(Transaction transaction) {
        String sql = "INSERT INTO transactions (description, amount, type, category, balance, total_income, expenses,  additional_income, username) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set parameters from the transaction and currentUser details
            pstmt.setString(1, transaction.getDescription());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getType());
            pstmt.setString(4, transaction.getCategory());
            pstmt.setDouble(5, currentUser.getBalance()); // Current balance
            pstmt.setDouble(6, currentUser.getTotalIncome()); // Total income
            pstmt.setDouble(7, currentUser.getMonthlyExpenses()); // monthly expenses

            pstmt.setDouble(8, currentUser.getMonthlyAdditionalIncome()); // Additional income
            pstmt.setString(9, currentUser.getUsername()); // User's username

            // Execute the insert operation
            pstmt.executeUpdate();
            System.out.println("Transaction saved to the database successfully.");
        } catch (SQLException e) {
            // Improved error handling
            System.err.println("Failed to save transaction to the database: " + e.getMessage());
            e.printStackTrace(); // For debugging purposes
        }
    }

    private void deleteUser(String username, String password) {

        try {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {

                    } else {
                        System.out.println("Invalid username or password.");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }

        // Delete user from the database
        String sql = "DELETE FROM users WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
            System.out.println("User deleted successfully.");
            users.remove(username); // Remove from in-memory user map
        } catch (SQLException e) {
            System.out.println("Failed to delete user: " + e.getMessage());
        }
    }

    private void updateUserPasswordInDatabase(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            System.out.println("Password updated in the database.");
        } catch (SQLException e) {
            System.out.println("Failed to update password in the database: " + e.getMessage());
        }
    }

    private void closeDatabaseConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Failed to close the database connection: " + e.getMessage());
        }
    }

    private void addInvestment() {
        String description = "";
        double amount = 0.0;
        String category = "";

        while (true) {
            try {
                while (description.isEmpty()) {
                    System.out.print("Enter investment description: ");
                    description = scanner.nextLine().trim();
                    if (description.isEmpty()) {
                        System.out.println("Description cannot be empty.");
                    }
                }

                while (amount <= 0 || amount > currentUser.getSavings()) {
                    System.out.print("Enter investment amount: ");
                    try {
                        amount = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number for amount.");
                    }
                    if (amount <= 0) {
                        System.out.println("Amount must be greater than zero.");
                    } else if (amount > currentUser.getSavings()) {
                        System.out.println("Investment amount cannot exceed available savings.");
                    }
                }

                System.out.println("Choose investment category:");
                System.out.println("1. Shares (Long-Term)");
                System.out.println("2. Gold");
                System.out.println("3. SIP");

                int categoryChoice = Integer.parseInt(scanner.nextLine().trim());
                switch (categoryChoice) {
                    case 1:
                        category = "Shares (Long-Term)";
                        break;
                    case 2:
                        category = "Gold";
                        break;
                    case 3:
                        category = "SIP";
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose a valid category.");
                        continue;
                }

                Investment investment = new Investment(description, amount, category);
                currentUser.addInvestment(investment);
                System.out.println("Investment added successfully.");
                break;

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public void run() {
        while (true) {
            System.out.println("\nWelcome to Personal Finance Manager");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Forgot Password");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        login();
                        break;
                    case 2:
                        register();
                        break;
                    case 3:
                        forgotPassword();
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        closeDatabaseConnection();
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void deleteUserPrompt() {
        System.out.print("Enter username to delete: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter passord: ");
        String password = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }
        if (password.isEmpty()) {
            System.out.println("password cannot be empty.");
            return;
        }

        deleteUser(username, password);
    }

    private void login() {
        String roleChoice;
        boolean isAuthenticated = false;

        // Loop until a valid role choice is provided
        while (true) {
            System.out.print("Are you a (1) User or (2) Admin? ");
            roleChoice = scanner.nextLine().trim();

            if (roleChoice.equals("1") || roleChoice.equals("2")) {
                break; // Exit the loop if a valid choice is entered
            } else {
                System.out.println("Invalid choice. Please select 1 for User or 2 for Admin.");
            }
        }

        // Loop until valid username and password are provided
        while (true) {
            String usernamePrompt = roleChoice.equals("1") ? "Enter username: " : "Enter admin name: ";
            String passwordPrompt = "Enter password: ";

            System.out.print(usernamePrompt);
            String username = scanner.nextLine().trim();
            System.out.print(passwordPrompt);
            String password = scanner.nextLine().trim();

            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Username and password cannot be empty.");
                continue; // Ask for username and password again
            }

            try {
                if (roleChoice.equals("1")) {
                    // User login
                    isAuthenticated = authenticateUser(username, password);
                    if (isAuthenticated) {
                        currentUser = loadUserFromDatabase(username);
                        currentUser.checkAndDepositMonthlyIncome();
                        showDashboard();
                        break; // Exit the loop after successful login
                    } else {
                        System.out.println("Invalid username or password for User. Please try again.");
                    }
                } else if (roleChoice.equals("2")) {
                    // Admin login
                    isAuthenticated = authenticateAdmin(username, password);
                    if (isAuthenticated) {
                        System.out.println("Admin login successful.");
                        adminMenu();
                        break; // Exit the loop after successful login
                    } else {
                        System.out.println("Invalid admin name or password. Please try again.");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error during login: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
    }

    private boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if user exists
            }
        }
    }

    private boolean authenticateAdmin(String adminname, String password) throws SQLException {
        String sql = "SELECT * FROM admin WHERE adminname = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, adminname);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if admin exists
            }
        }
    }

    private User loadUserFromDatabase(String username) {
        User user = null;
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    double MonthlyIncome = rs.getDouble("MonthlyIncome"); // Check for correct column name
                    String securityAnswer = rs.getString("securityAnswer");
                    String mobileNumber = rs.getString("mobileNumber");
                    double savings = rs.getDouble("savings");
                    boolean checkEmi = rs.getBoolean("checkEmi");
                    double emi = rs.getDouble("emi");

                    user = new User(username, password, MonthlyIncome, securityAnswer, mobileNumber, checkEmi, emi);
                    loadTransactionsForUser(user); // Load transactions for the user
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading user from database: " + e.getMessage());
        }
        return user;
    }

    private void loadTransactionsForUser(User user) {
        String sql = "SELECT * FROM transactions WHERE username = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String description = rs.getString("description");
                    double amount = rs.getDouble("amount");
                    String type = rs.getString("type"); // "Expense" or "Income"
                    String category = rs.getString("category");

                    Transaction transaction = new Transaction(description, amount, type, category);
                    user.addTransaction(transaction);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading transactions for user: " + e.getMessage());
        }
    }

    public void adminMenu() {
        while (true) {
            System.out.println("\nAdmin Menu");
            System.out.println("1. View All Users");
            System.out.println("2. add Admin");
            System.out.println("3. View Admin");
            System.out.println("4. view all Transactions");
            System.out.println("5. view all Investments");
            System.out.println("6. Delete User");

            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        viewAllUsers();
                        break;
                    case 2:
                        addAdmin();
                        break;
                    case 3:
                        viewAdmin();
                        break;

                    case 4:
                        viewAllTransactions();
                        break;

                    case 5:
                        viewAllInvestments();
                        break;
                    case 6:
                        deleteUserPrompt();
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    public void viewAllTransactions() {
        String sql = "SELECT * FROM transactions ORDER BY username, date";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            // Print column headers
            System.out.println("All Transactions:");
            System.out.println(String.format("%-20s %-30s %-10s %-20s %-20s", "Username", "Description", "Amount",
                    "Type", "Date"));

            // Loop through the result set and print each row
            while (rs.next()) {
                String username = rs.getString("username");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");
                java.sql.Timestamp date = rs.getTimestamp("date");

                // Print transaction details
                System.out.println(String.format("%-20s %-30s %-10.2f %-20s %-20s",
                        username, description, amount, type, date));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving transactions: " + e.getMessage());
        }
    }

    public void viewAllInvestments() {
        String sql = "SELECT * FROM investments ORDER BY username, date";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            // Print column headers
            System.out.println("All Investments:");
            System.out
                    .println(String.format("%-20s %-20s %-10s %-20s", "Username", "Investment Type", "Amount", "Date"));

            // Loop through the result set and print each row
            while (rs.next()) {
                String username = rs.getString("username");
                String investmentType = rs.getString("category");
                double amount = rs.getDouble("amount");
                java.sql.Timestamp date = rs.getTimestamp("date");

                // Print investment details
                System.out.println(String.format("%-20s %-20s %-10.2f %-20s",
                        username, investmentType, amount, date != null ? date.toString() : "N/A"));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving investments: " + e.getMessage());
        }
    }

    public void viewAdmin() {
        String sql = "SELECT * FROM admin";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            // Print column headers
            System.out.println("Admin Details:");
            System.out.println(String.format("%-20s %-20s %-15s %-30s", "Username", "Password", "Mobile Number",
                    "Security Answer"));

            // Loop through the result set and print each row
            while (rs.next()) {
                String adminname = rs.getString("adminname");
                String password = rs.getString("password");
                String mobileNumber = rs.getString("mobileNumber");
                String securityAnswer = rs.getString("securityAnswer");

                // Print admin details
                System.out.println(String.format("%-20s %-20s %-15s %-30s",
                        adminname, password, mobileNumber, securityAnswer));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving admin data: " + e.getMessage());
        }
    }

    private void addAdmin() {
        String adminName = "";
        String password = "";
        String mobileNumber = "";
        String securityAnswer = "";

        while (true) {
            try {
                // Admin Name
                while (adminName.isEmpty()) {
                    System.out.print("Enter full name: ");
                    adminName = scanner.nextLine().trim();
                    if (adminName.isEmpty()) {
                        System.out.println("Full name cannot be empty.");
                    }
                }

                // Password
                while (password.isEmpty() || !isValidPassword(password)) {
                    System.out.print("Enter password: ");
                    password = scanner.nextLine().trim();
                    if (!isValidPassword(password)) {
                        System.out.println(
                                "Password must be exactly 8 characters long and include at least one letter, one number, and one special character.");
                        password = "";
                    }
                }

                // Mobile Number
                while (mobileNumber.isEmpty() || !mobileNumber.matches("\\d{10}")) {
                    System.out.print("Enter mobile number: ");
                    mobileNumber = scanner.nextLine().trim();
                    if (!mobileNumber.matches("\\d{10}")) {
                        System.out.println("Mobile number must be exactly 10 digits and contain only numbers.");
                        mobileNumber = "";
                    }
                }

                // Security Answer
                while (securityAnswer.isEmpty()) {
                    System.out.print("Enter security answer (hometown): ");
                    securityAnswer = scanner.nextLine().trim();
                    if (securityAnswer.isEmpty()) {
                        System.out.println("Security answer cannot be empty.");
                    }
                }

                // Debugging: Print collected values
                System.out.println("Admin Name: " + adminName);
                System.out.println("Password: " + password);
                System.out.println("Mobile Number: " + mobileNumber);
                System.out.println("Security Answer: " + securityAnswer);

                // Create Admin object
                Admin newAdmin = new Admin(adminName, password, securityAnswer, mobileNumber);

                // Add Admin to database
                if (addAdminValueInDatabase(newAdmin)) {
                    System.out.println("Admin added successfully.");
                    break; // Exit the loop
                } else {
                    System.out.println("Failed to add admin. Please try again.");
                }

            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    public boolean addAdminValueInDatabase(Admin admin) {
        String sql = "INSERT INTO admin (adminname, password, mobileNumber, securityAnswer) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, admin.getAdminname()); // Ensure this is correct
            pstmt.setString(2, admin.getPassword());
            pstmt.setString(3, admin.getMobileNumber());
            pstmt.setString(4, admin.getSecurityAnswer());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error adding admin to the database: " + e.getMessage());
            return false;
        }
    }

    public Admin getAdminByAdminname(String adminname) {
        String sql = "SELECT * FROM admin WHERE adminname = ?";
        Admin admin = null;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, adminname);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    String mobileNumber = rs.getString("mobileNumber");
                    String securityAnswer = rs.getString("securityAnswer");

                    admin = new Admin(adminname, password, securityAnswer, mobileNumber);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving admin from the database: " + e.getMessage());
        }
        return admin;
    }

    private void viewAllUsers() {
        String sql = "SELECT * FROM users";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\nList of All Users:");
            System.out.println("-------------------");

            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                double MonthlyIncome = rs.getDouble("MonthlyIncome");
                String securityAnswer = rs.getString("securityAnswer");
                String mobileNumber = rs.getString("mobileNumber");
                double savings = rs.getDouble("savings");
                boolean checkEmi = rs.getBoolean("checkEmi");
                double emi = rs.getDouble("emi");

                // Display user details
                System.out.println("Username: " + username);
                System.out.println("Password: " + password); // Consider omitting or obfuscating passwords in real
                                                             // applications
                System.out.println("Monthly Income: $" + String.format("%.2f", MonthlyIncome));
                System.out.println("Security Answer: " + securityAnswer);
                System.out.println("Mobile Number: " + mobileNumber);
                System.out.println("Savings: $" + String.format("%.2f", savings));
                System.out.println("Check EMI: " + (checkEmi ? "Yes" : "No"));
                System.out.println("EMI: $" + String.format("%.2f", emi));
                System.out.println("-------------------");

            }

        } catch (SQLException e) {
            System.out.println("Error loading users from the database: " + e.getMessage());
        }
    }

    public void register() {
        String fullName = "";
        String birthdate = "";
        String password = "";
        String mobileNumber = "";
        String securityAnswer = "";
        double monthlyIncome = 0.0;
        boolean checkEmi = false; // Initialize checkEmi
        double emi = 0.0; // Initialize emi

        while (true) {
            try {
                // Full Name
                while (fullName.isEmpty()) {
                    System.out.print("Enter full name: ");
                    fullName = scanner.nextLine().trim();
                    if (fullName.isEmpty()) {
                        System.out.println("Full name cannot be empty.");
                    }
                }

                // Birthdate
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                sdf.setLenient(false);
                while (birthdate.isEmpty()) {
                    System.out.print("Enter birthdate (dd-MM-yyyy): ");
                    birthdate = scanner.nextLine().trim();
                    try {
                        sdf.parse(birthdate);
                    } catch (ParseException e) {
                        System.out.println("Please enter a valid date in the format dd-MM-yyyy.");
                        birthdate = "";
                    }
                }

                // Password
                while (password.isEmpty() || !isValidPassword(password)) {
                    System.out.print("Enter password: ");
                    password = scanner.nextLine().trim();
                    if (!isValidPassword(password)) {
                        System.out.println(
                                "Password must be exactly 8 characters long and include at least one letter, one number, and one special character.");
                        password = "";
                    }
                }

                // Mobile Number
                while (mobileNumber.isEmpty() || !mobileNumber.matches("\\d{10}")) {
                    System.out.print("Enter mobile number: ");
                    mobileNumber = scanner.nextLine().trim();
                    if (!mobileNumber.matches("\\d{10}")) {
                        System.out.println("Mobile number must be exactly 10 digits and contain only numbers.");
                        mobileNumber = "";
                    }
                }

                while (monthlyIncome <= 0) {
                    System.out.print("Enter monthly income: ");
                    try {
                        monthlyIncome = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number for Monthly income.");
                    }
                    if (monthlyIncome <= 0) {
                        System.out.println("monthly income must be greater than zero.");
                    }
                }

                // Security Answer
                while (securityAnswer.isEmpty()) {
                    System.out.print("Enter security answer (hometown): ");
                    securityAnswer = scanner.nextLine().trim();
                    if (securityAnswer.isEmpty()) {
                        System.out.println("Security answer cannot be empty.");
                    }
                }

                // Check if EMI should be entered
                while (true) {
                    System.out.print("Do you have EMI to enter? (yes/no): ");
                    String checkEmiStr = scanner.nextLine().trim().toLowerCase();
                    if (checkEmiStr.equals("yes")) {
                        checkEmi = true;
                        break;
                    } else if (checkEmiStr.equals("no")) {
                        checkEmi = false;
                        break;
                    } else {
                        System.out.println("Please enter 'yes' or 'no'.");
                    }
                }

                // EMI Value
                if (checkEmi) {
                    while (emi <= 0) {
                        System.out.print("Enter EMI amount: ");
                        try {
                            emi = Double.parseDouble(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid number for EMI.");
                        }
                        if (emi <= 0) {
                            System.out.println("EMI amount must be greater than zero.");
                        }
                    }
                }

                // Generate Username
                String username = generateUsername(fullName, birthdate);
                // Check for existing username
                if (!users.containsKey(username)) {
                    // Create User with checkEmi and emi
                    User newUser = new User(username, password, monthlyIncome, securityAnswer, mobileNumber, checkEmi,
                            emi);
                    newUser.depositMonthlyIncome();
                    boolean isSaved = saveUserToDatabase(newUser);
                    if (isSaved) {
                        users.put(username, newUser);
                        System.out.println("Registration successful. Your username is: " + username);
                        break;
                    } else {
                        System.out.println("Registration Failed");
                        break;
                    }
                } else {
                    System.out.println("Username already exists. Try a different name or birthdate.");
                    // Reset the loop in case of a username conflict.
                    fullName = "";
                    birthdate = "";
                    password = "";
                    mobileNumber = "";
                    securityAnswer = "";
                    monthlyIncome = 0.0;
                    checkEmi = false;
                    emi = 0.0;
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private void forgotPassword() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter security answer (hometown): ");
        String securityAnswer = scanner.nextLine().trim();

        if (username.isEmpty() || securityAnswer.isEmpty()) {
            System.out.println("Username and security answer cannot be empty.");
            return;
        }

        try {
            // Check if the user exists and the security answer matches
            String sql = "SELECT securityAnswer FROM users WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String correctAnswer = rs.getString("securityAnswer");

                        if (correctAnswer.equals(securityAnswer)) {
                            // Security answer is correct, now allow password reset
                            String newPassword = "";
                            while (newPassword.isEmpty() || !isValidPassword(newPassword)) {
                                System.out.print("Enter new password: ");
                                newPassword = scanner.nextLine().trim();
                                if (!isValidPassword(newPassword)) {
                                    System.out.println(
                                            "Password must be exactly 8 characters long and include at least one letter, one number, and one special character.");
                                    newPassword = "";
                                }
                            }

                            // Update password in the database
                            updateUserPasswordInDatabase(username, newPassword);
                            System.out.println("Password reset successful.");
                        } else {
                            System.out.println("Incorrect security answer.");
                        }
                    } else {
                        System.out.println("Username not found.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during password reset: " + e.getMessage());
        }
    }

    private boolean isValidPassword(String password) {
        // Regular expression to check for at least one letter, one number, and one
        // special character
        String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8}$";
        return password.matches(regex);
    }

    private void showDashboard() {
        while (true) {
            System.out.println("\nDashboard");
            System.out.println("Balance: $" + String.format("%.2f", currentUser.getBalance()));
            System.out.println("Total Income: $" + String.format("%.2f", currentUser.getTotalIncome()));
            System.out.println("Savings: $" + String.format("%.2f", currentUser.getSavings()));
            System.out.println("Expenses: $" + String.format("%.2f", currentUser.getMonthlyExpenses()));
            System.out
                    .println("Additional Income: $" + String.format("%.2f", currentUser.getMonthlyAdditionalIncome()));

            // Display investments
            System.out.println(
                    "Investment (Share): $" + String.format("%.2f", currentUser.getShare(currentUser.getUsername())));
            System.out.println(
                    "Investment (Gold): $" + String.format("%.2f", currentUser.getGold(currentUser.getUsername())));
            System.out.println(
                    "Investment (SIP): $" + String.format("%.2f", currentUser.getSip(currentUser.getUsername())));

            // Display EMI details
            if (currentUser.isCheckEmi()) {
                System.out.println("EMI: $" + String.format("%.2f", currentUser.getEmi()));
            } else {
                System.out.println("EMI: Not Applicable");
            }

            // Dashboard menu options
            System.out.println("1. Add Transaction");
            System.out.println("2. View Transactions");
            System.out.println("3. Add Investment");
            System.out.println("4. View Investments");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                switch (choice) {
                    case 1:
                        addTransaction();
                        break;
                    case 2:
                        viewTransactions();
                        break;
                    case 3:
                        addInvestment();
                        break;
                    case 4:
                        viewInvestments();
                        break;
                    case 5:
                        currentUser = null;
                        return;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void viewInvestments() {
        System.out.println("Investments:");
        try {
            String sql = "SELECT * FROM investments WHERE username = ? ";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, currentUser.getUsername());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.err
                                .println(String.format("Investment [description=%s, amount=%.2f, category=%s, date=%s]",
                                        rs.getString("description"), rs.getDouble("amount"), rs.getString("category"),
                                        rs.getDate("date")));

                    } else {
                        System.out.println("Invalid username or password.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

    private void addTransaction() {
        String description = "";
        double amount = 0.0;
        int typeChoice = 0;
        while (true) {
            try {
                while (description.isEmpty()) {
                    System.out.print("Enter description: ");
                    description = scanner.nextLine().trim();
                    if (description.isEmpty()) {
                        System.out.println("Description cannot be empty.");
                    }
                }

                while (amount <= 0) {
                    System.out.print("Enter amount: ");
                    try {
                        amount = Double.parseDouble(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number for amount.");
                    }
                    if (amount <= 0) {
                        System.out.println("Amount must be greater than zero.");
                    }
                }

                while (typeChoice != 1 && typeChoice != 2) {
                    System.out.println("Type (1-Income, 2-Expense): ");
                    System.out.print("Enter choice: ");
                    try {
                        typeChoice = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number for type.");
                    }
                    if (typeChoice != 1 && typeChoice != 2) {
                        System.out.println("TypeChoice can only be 1 or 2.");
                    }
                }

                String type = typeChoice == 1 ? "Income" : "Expense";
                String[] categories = type.equals("Income") ? incomeCategories : expenseCategories;
                System.out.println("Choose a category:");
                for (int i = 0; i < categories.length; i++) {
                    System.out.println((i + 1) + ". " + categories[i]);
                }

                int categoryChoice = Integer.parseInt(scanner.nextLine().trim());
                String category = categories[categoryChoice - 1];

                if (type.equals("Expense") && !currentUser.canAddExpense(amount)) {
                    System.out.println("Expense exceeds the allowable limit.");
                    break;
                } else {
                    Transaction transaction = new Transaction(description, amount, type, category);
                    currentUser.addTransaction(transaction); // This uses LinkedList internally
                    saveTransactionToDatabase(transaction); // Save to database
                    System.out.println("Transaction added successfully.");
                    break; // Break out of the loop upon successful transaction
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Invalid category choice.");
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void viewTransactions() {
        System.out.println("Transactions:");
        for (Transaction transaction : currentUser.getTransactions()) { // This iterates over LinkedList
            System.out.println(transaction);
        }
    }

    private String generateUsername(String fullName, String birthdate) {
        // Split the full name into parts
        String[] nameParts = fullName.split(" ");
        String name = nameParts[0];

        // Convert birthdate to a date object
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date birthDate = null;
        try {
            birthDate = sdf.parse(birthdate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Extract year of the birthdate
        Calendar cal = Calendar.getInstance();
        cal.setTime(birthDate);
        String year = String.valueOf(cal.get(Calendar.YEAR));

        // Create username using first initial, last name, and year
        String username = name + year;
        return username;
    }

}
