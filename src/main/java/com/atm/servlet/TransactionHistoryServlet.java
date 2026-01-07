package com.atm.servlet;

import com.atm.util.DBConnection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.*;

@WebServlet("/transactions")
public class TransactionHistoryServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("accountNo") == null) {
            response.getWriter().print("NO_SESSION");
            return;
        }

        String accountNo = session.getAttribute("accountNo").toString();

        try (Connection con = DBConnection.getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                "SELECT type, amount, date FROM transactions " +
                "WHERE account_no=? ORDER BY id DESC"
            );
            ps.setString(1, accountNo);

            ResultSet rs = ps.executeQuery();

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"type\":\"").append(rs.getString("type")).append("\",")
                    .append("\"amount\":").append(rs.getDouble("amount")).append(",")
                    .append("\"date\":\"").append(rs.getTimestamp("date")).append("\"")
                    .append("}");
            }
            json.append("]");

            response.setContentType("application/json");
            response.getWriter().print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("ERROR");
        }
    }
}
