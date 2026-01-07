package com.atm.servlet;

import com.atm.util.DBConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/createAccount")
public class CreateAccountServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String accountNo = request.getParameter("accountNo");
        String name = request.getParameter("name");
        String pin = request.getParameter("pin");
        String balanceStr = request.getParameter("balance");

        System.out.println("=== CREATE ACCOUNT DEBUG ===");
        System.out.println("AccountNo = " + accountNo);
        System.out.println("Name      = " + name);
        System.out.println("PIN       = " + pin);
        System.out.println("Balance   = " + balanceStr);

        try {
            double balance = Double.parseDouble(balanceStr);
            Connection con = DBConnection.getConnection();

            // 🔒 Check if account already exists
            PreparedStatement check =
                    con.prepareStatement(
                            "SELECT account_no FROM users WHERE account_no=?");
            check.setString(1, accountNo);

            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                response.getWriter().print("EXISTS");
                return;
            }

            // ✅ Insert new account
            PreparedStatement ps =
                    con.prepareStatement(
                            "INSERT INTO users (account_no, name, pin, balance) VALUES (?, ?, ?, ?)");
            ps.setString(1, accountNo);
            ps.setString(2, name);
            ps.setString(3, pin);
            ps.setDouble(4, balance);

            ps.executeUpdate();

            System.out.println("✅ ACCOUNT CREATED SUCCESSFULLY");
            response.getWriter().print("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}
