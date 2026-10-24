package com.sudokuarena.servlet;

import com.sudokuarena.dao.UserDao;
import com.sudokuarena.model.User;
import com.sudokuarena.util.PasswordUtil;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final int REMEMBER_SECONDS = 7 * 24 * 60 * 60;

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = Web.trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password");
        boolean remember = req.getParameter("remember") != null;

        String error = null;
        try {
            User user = email.isEmpty() ? null : userDao.findByEmail(email);

            if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
                error = "Invalid email or password.";
            } else if (!user.isActive()) {
                error = "This account has been deactivated. Please contact an administrator.";
            } else {
                HttpSession session = Web.login(req, user);
                userDao.touchLastLogin(user.getUserId());

                if (remember) {
                    session.setMaxInactiveInterval(REMEMBER_SECONDS);
                    Cookie cookie = new Cookie("JSESSIONID", session.getId());
                    cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(REMEMBER_SECONDS);
                    resp.addCookie(cookie);
                }

                resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/home"));
                return;
            }
        } catch (SQLException e) {
            getServletContext().log("Login failed", e);
            error = "We could not log you in right now. Please try again.";
        }

        req.setAttribute("error", error);
        req.setAttribute("email", email);
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
}
