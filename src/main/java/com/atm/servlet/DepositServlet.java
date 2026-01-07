package com.atm.servlet;

import com.atm.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/deposit")
public class DepositServlet extends HttpServlet {

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

            // 🔐 TRANSACTION START
            con.setAutoCommit(false);

            // 1️⃣ Update balance
            PreparedStatement ps1 = con.prepareStatement(
                "UPDATE users SET balance = balance + ? WHERE account_no = ?"
            );
            ps1.setDouble(1, amount);
            ps1.setString(2, accountNo);
            ps1.executeUpdate();

            // 2️⃣ Insert transaction
            PreparedStatement ps2 = con.prepareStatement(
                "INSERT INTO transactions (account_no, type, amount) VALUES (?, ?, ?)"
            );
            ps2.setString(1, accountNo);
            ps2.setString(2, "DEPOSIT");
            ps2.setDouble(3, amount);
            ps2.executeUpdate();

            // ✅ COMMIT
            con.commit();
            response.getWriter().print("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}
