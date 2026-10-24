package com.sudokuarena.servlet;

import com.sudokuarena.dao.UserDao;
import com.sudokuarena.model.Difficulty;
import com.sudokuarena.model.User;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-() ]{0,15}$");

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/home?page=profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        long userId = (Long) session.getAttribute("userId");

        String name = Web.trim(req.getParameter("name"));
        String email = Web.trim(req.getParameter("email")).toLowerCase();
        String phone = Web.trim(req.getParameter("phone"));
        String interests = Web.trim(req.getParameter("interests"));
        Difficulty difficulty = Difficulty.parse(req.getParameter("difficulty"));

        String error = null;
        if (name.isEmpty() || name.length() > 100) {
            error = "Please enter a name (up to 100 characters).";
        } else if (!EMAIL.matcher(email).matches() || email.length() > 100) {
            error = "Please enter a valid email address.";
        } else if (!PHONE.matcher(phone).matches()) {
            error = "Phone number can only contain digits, spaces and + - ( ).";
        } else if (interests.length() > 500) {
            error = "Interests can be at most 500 characters.";
        } else if (difficulty == null) {
            error = "Please choose a preferred difficulty.";
        }

        try {
            if (error == null && userDao.emailTaken(email, userId)) {
                error = "This email is already registered.";
            }

            if (error == null) {
                User user = new User();
                user.setUserId(userId);
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone.isEmpty() ? null : phone);
                user.setInterests(interests.isEmpty() ? null : interests);
                user.setDifficulty(difficulty.getLabel());
                userDao.updateProfile(user);
                Web.flash(session, "success", "Profile updated successfully.");
            } else {
                Web.flash(session, "error", error);
            }
        } catch (SQLException e) {
            getServletContext().log("Profile update failed", e);
            Web.flash(session, "error", e.getErrorCode() == 1
                    ? "This email is already registered."
                    : "Your profile could not be saved. Please try again.");
        }

        resp.sendRedirect(req.getContextPath() + "/home?page=profile");
    }
}
