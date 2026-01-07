package com.atm.servlet;

import com.atm.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/withdraw")
public class WithdrawServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountNo") == null) {
            response.getWriter().print("NO_SESSION");
            return;
        }

        String accountNo = session.getAttribute("accountNo").toString();
        double amount;

        try {
            amount = Double.parseDouble(request.getParameter("amount"));
        } catch (Exception e) {
            response.getWriter().print("INVALID_AMOUNT");
            return;
        }

        if (amount <= 0) {
            response.getWriter().print("INVALID_AMOUNT");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // 🔐 START TRANSACTION
            con.setAutoCommit(false);

            // 1️⃣ Get current balance (LOCK ROW)
            PreparedStatement ps1 = con.prepareStatement(
                "SELECT balance FROM users WHERE account_no=? FOR UPDATE"
            );
            ps1.setString(1, accountNo);

            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) {
                con.rollback();
                response.getWriter().print("NOT_FOUND");
                return;
            }

            double currentBalance = rs.getDouble("balance");

            if (amount > currentBalance) {
                con.rollback();
                response.getWriter().print("INSUFFICIENT");
                return;
            }

            // 2️⃣ Update balance
            PreparedStatement ps2 = con.prepareStatement(
                "UPDATE users SET balance=? WHERE account_no=?"
            );
            ps2.setDouble(1, currentBalance - amount);
            ps2.setString(2, accountNo);
            ps2.executeUpdate();

            // 3️⃣ Insert transaction record
            PreparedStatement ps3 = con.prepareStatement(
                "INSERT INTO transactions (account_no, type, amount) VALUES (?, ?, ?)"
            );
            ps3.setString(1, accountNo);
            ps3.setString(2, "WITHDRAW");
            ps3.setDouble(3, amount);
            ps3.executeUpdate();

            // ✅ COMMIT
            con.commit();
            response.getWriter().print("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}
