package com.atm.servlet;

import com.atm.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/transfer")
public class TransferServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountNo") == null) {
            response.getWriter().print("NO_SESSION");
            return;
        }

        String sender = session.getAttribute("accountNo").toString();
        String receiver = request.getParameter("toAccount");
        double amount;

        if (sender.equals(receiver)) {
            response.getWriter().print("SAME_ACCOUNT");
            return;
        }

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

            con.setAutoCommit(false);

            // 1️⃣ Lock sender row
            PreparedStatement ps1 = con.prepareStatement(
                "SELECT balance FROM users WHERE account_no=? FOR UPDATE"
            );
            ps1.setString(1, sender);
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) {
                con.rollback();
                response.getWriter().print("SENDER_NOT_FOUND");
                return;
            }

            double senderBalance = rs1.getDouble("balance");
            if (amount > senderBalance) {
                con.rollback();
                response.getWriter().print("INSUFFICIENT");
                return;
            }

            // 2️⃣ Lock receiver row
            PreparedStatement ps2 = con.prepareStatement(
                "SELECT balance FROM users WHERE account_no=? FOR UPDATE"
            );
            ps2.setString(1, receiver);
            ResultSet rs2 = ps2.executeQuery();

            if (!rs2.next()) {
                con.rollback();
                response.getWriter().print("RECEIVER_NOT_FOUND");
                return;
            }

            double receiverBalance = rs2.getDouble("balance");

            // 3️⃣ Update balances
            PreparedStatement debit = con.prepareStatement(
                "UPDATE users SET balance=? WHERE account_no=?"
            );
            debit.setDouble(1, senderBalance - amount);
            debit.setString(2, sender);
            debit.executeUpdate();

            PreparedStatement credit = con.prepareStatement(
                "UPDATE users SET balance=? WHERE account_no=?"
            );
            credit.setDouble(1, receiverBalance + amount);
            credit.setString(2, receiver);
            credit.executeUpdate();

            // 4️⃣ Insert transactions
            PreparedStatement tx1 = con.prepareStatement(
                "INSERT INTO transactions (account_no, type, amount) VALUES (?, ?, ?)"
            );
            tx1.setString(1, sender);
            tx1.setString(2, "TRANSFER_OUT");
            tx1.setDouble(3, amount);
            tx1.executeUpdate();

            PreparedStatement tx2 = con.prepareStatement(
                "INSERT INTO transactions (account_no, type, amount) VALUES (?, ?, ?)"
            );
            tx2.setString(1, receiver);
            tx2.setString(2, "TRANSFER_IN");
            tx2.setDouble(3, amount);
            tx2.executeUpdate();

            con.commit();
            response.getWriter().print("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}
