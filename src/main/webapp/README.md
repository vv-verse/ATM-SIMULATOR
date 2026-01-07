# ATM Simulator 🏧

A full-stack **ATM Simulator Web Application** built using **Java, Servlets, JDBC, MySQL, HTML, CSS, and JavaScript**.  
This project simulates the real-world working of an ATM system with secure authentication, database-backed transactions, and session management.

---

## 📌 Project Overview

The ATM Simulator allows users to:
- Create a bank account
- Log in securely using account number and PIN
- Perform ATM operations such as deposit, withdrawal, transfer, and PIN change
- View real-time balance and transaction history fetched from the database

All core operations are **connected to a MySQL database**, making this a **real backend-driven application**, not a dummy simulation.

---

## 🚀 Features

- Account Creation
- Secure Login (Session-based)
- View Balance (Fetched from Database)
- Deposit Money
- Withdraw Money
- Transfer Money Between Accounts
- Change PIN
- Transaction History
- Logout
- Proper Error Handling

---

## 🛠️ Tech Stack

**Backend**
- Java
- Servlets
- JDBC

**Frontend**
- HTML
- CSS
- JavaScript

**Database**
- MySQL

**Server**
- Apache Tomcat 9

**Version Control**
- Git & GitHub

---

## 🗄️ Database Schema

```sql
CREATE TABLE users (
  account_no VARCHAR(6) PRIMARY KEY,
  name VARCHAR(100),
  pin VARCHAR(4),
  balance DOUBLE
);

CREATE TABLE transactions (
  id INT AUTO_INCREMENT PRIMARY KEY,
  account_no VARCHAR(6),
  type VARCHAR(50),
  amount DOUBLE,
  date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

▶️ How to Run the Project Locally

1️⃣ Clone the Repository
```sql
git clone https://github.com/vv-verse/ATM-SIMULATOR.git
```

2️⃣ Open Project in Eclipse

Open Eclipse IDE

File → Import → Existing Projects into Workspace

Select the cloned project folder

3️⃣ Configure Apache Tomcat

Add Apache Tomcat 9

Right click project → Run on Server

Select Tomcat

4️⃣ Setup MySQL Database

Open MySQL Workbench

Create database:

```sql
CREATE DATABASE atm_db;
USE atm_db;
```


Create tables using schema provided above

5️⃣ Configure Database Connection

Update credentials in:

src/main/java/com/atm/util/DBConnection.java


Example:

String url = "jdbc:mysql://localhost:3306/atm_db";
String user = "root";
String password = "your_password";

6️⃣ Start Server & Access App

Start Tomcat and open browser:

```http://localhost:8081/ATM-SIMULATOR/```

⚠️ Important Notes

This application runs locally

Requires MySQL and Tomcat running

Not deployed on cloud yet

Suitable for:

College projects
Backend practice
Resume projects


👤 Author

Vivek Kumar
B.Tech (CSE)
GitHub: https://github.com/vv-verse

📄 License
This project is for educational purposes.
