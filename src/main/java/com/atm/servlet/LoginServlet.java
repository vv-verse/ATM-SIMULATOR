package com.atm.servlet;

import com.atm.util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ✅ Get & trim inputs
        String accountNo = request.getParameter("accountNo");
        String pin = request.getParameter("pin");

        accountNo = accountNo != null ? accountNo.trim() : "";
        pin = pin != null ? pin.trim() : "";
        
        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("AccountNo from UI = [" + accountNo + "]");
        System.out.println("PIN from UI       = [" + pin + "]");


        try {
            Connection con = DBConnection.getConnection();

            String sql =
                "SELECT name, balance FROM users WHERE account_no = ? AND pin = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, accountNo);
            ps.setString(2, pin);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // ✅ Create session
                HttpSession session = request.getSession();
                session.setAttribute("accountNo", accountNo);
                session.setAttribute("name", rs.getString("name"));
                session.setAttribute("balance", rs.getDouble("balance"));

                response.getWriter().print("success"); // ⚠️ lowercase
            } else {
                response.getWriter().print("invalid");
            }

            // ✅ Close resources
            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().print("error");
        }
    }
}
