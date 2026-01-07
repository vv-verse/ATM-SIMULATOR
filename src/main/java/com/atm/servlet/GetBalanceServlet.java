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

@WebServlet("/getBalance")
public class GetBalanceServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("=== GET BALANCE SERVLET HIT ===");

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.getWriter().write("NO_SESSION");
            return;
        }

        String accountNo = (String) session.getAttribute("accountNo");
        System.out.println("AccountNo from session = " + accountNo);

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT balance FROM users WHERE account_no = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, accountNo);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.println("Balance from DB = " + balance);
                response.getWriter().write(String.valueOf(balance));
            } else {
                response.getWriter().write("NOT_FOUND");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("ERROR");
        }
    }
}
