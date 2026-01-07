package com.atm.servlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/getUser")
public class GetUserServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("name") == null) {
            response.getWriter().print("NO_SESSION");
            return;
        }

        String name = session.getAttribute("name").toString();
        response.getWriter().print(name);
    }
}
