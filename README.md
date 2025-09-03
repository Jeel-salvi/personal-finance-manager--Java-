📌 Personal Finance Manager (Console Application)

-- A comprehensive, console-based personal finance management application built with Java and MySQL.
   This tool allows users to manage income, expenses, and investments, while also providing a separate
   administrative panel for oversight and user management.

-- The application is built using core Java principles, featuring custom data structures and direct JDBC connectivity,    
   demonstrating strong software development fundamentals.

✨ Features

-- The application supports two distinct roles: User and Admin, each with a dedicated set of features.

👤 User Features

-- Secure Registration & Login
   Create an account with security features, including "Forgot Password" with security question.
   
-- Dynamic Dashboard 
   Displays current balance, income, monthly expenses, savings, and investments.
   
-- Transaction Management
   Add income/expense transactions (with description, amount, category).
   View transaction history.
   Investment Tracking
   Log investments in Shares, Gold, and SIP.
   View investment summary on the dashboard.
   Monthly income automatically deposited at the start of each new month.
   EMI Management
   Optionally add EMI, automatically factored into balance.

🛠️ Admin Features

 -Admin Dashboard with secure login.
 -User Oversigh
 -View all registered users and details (except passwords).
 -Delete users from the system.
 -System-Wide Monitoring
 -View logs of all transactions and all investments across users.
 -Admin Management
 -Add new admin accounts.

🏗 Tech Stack :-

Language: Java
Database: MySQL
Connectivity: JDBC
Core Concepts: OOP, SQL Triggers

📋 Usage Example
Main Menu
Welcome to Personal Finance Manager
1. Login
2. Register
3. Forgot Password
4. Exit

User Dashboard:

Balance: $3450.00
Total Income: $5000.00
Savings: $1500.00
Expenses: $50.00
Investments: Shares=$0, Gold=$0, SIP=$0
EMI: Not Applicable

Admin Menu :
1. View All Users
2. Add Admin
3. View Admin
4. View All Transactions
5. View All Investments
6. Delete User
