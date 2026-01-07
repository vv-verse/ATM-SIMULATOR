package com.atm.servlet;

import com.atm.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/changePin")
public class ChangePinServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountNo") == null) {
            response.getWriter().print("NO_SESSION");
            return;
        }

        String accountNo = session.getAttribute("accountNo").toString();
        String currentPin = request.getParameter("currentPin");
        String newPin = request.getParameter("newPin");

        if (currentPin == null || newPin == null ||
            newPin.length() != 4 || !newPin.matches("\\d{4}")) {
            response.getWriter().print("INVALID_PIN");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            // 1️⃣ Verify current PIN
            PreparedStatement ps1 = con.prepareStatement(
                "SELECT pin FROM users WHERE account_no=? FOR UPDATE"
            );
            ps1.setString(1, accountNo);
            ResultSet rs = ps1.executeQuery();

            if (!rs.next() || !rs.getString("pin").equals(currentPin)) {
                con.rollback();
                response.getWriter().print("WRONG_PIN");
                return;
            }

            // 2️⃣ Update PIN
            PreparedStatement ps2 = con.prepareStatement(
                "UPDATE users SET pin=? WHERE account_no=?"
            );
            ps2.setString(1, newPin);
            ps2.setString(2, accountNo);
            ps2.executeUpdate();

            // 3️⃣ Log transaction
            PreparedStatement ps3 = con.prepareStatement(
                "INSERT INTO transactions (account_no, type, amount) VALUES (?, ?, 0)"
            );
            ps3.setString(1, accountNo);
            ps3.setString(2, "PIN_CHANGE");
            ps3.executeUpdate();

            con.commit();
            response.getWriter().print("SUCCESS");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}
