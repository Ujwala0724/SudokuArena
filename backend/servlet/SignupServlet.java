package com.sudokuarena.servlet;

import com.sudokuarena.dao.UserDao;
import com.sudokuarena.model.Difficulty;
import com.sudokuarena.model.User;
import com.sudokuarena.util.PasswordUtil;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-() ]{0,15}$");

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/signup.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = Web.trim(req.getParameter("name"));
        String email = Web.trim(req.getParameter("email")).toLowerCase();
        String phone = Web.trim(req.getParameter("phone"));
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String interests = Web.trim(req.getParameter("interests"));
        Difficulty difficulty = Difficulty.parse(req.getParameter("difficulty"));

        String error = null;
        if (name.isEmpty() || name.length() > 100) {
            error = "Please enter your full name (up to 100 characters).";
        } else if (!EMAIL.matcher(email).matches() || email.length() > 100) {
            error = "Please enter a valid email address.";
        } else if (!PHONE.matcher(phone).matches()) {
            error = "Phone number can only contain digits, spaces and + - ( ).";
        } else if (password.length() < 8) {
            error = "Password must be at least 8 characters.";
        } else if (interests.length() > 500) {
            error = "Interests can be at most 500 characters.";
        } else if (difficulty == null) {
            error = "Please choose a preferred difficulty.";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone.isEmpty() ? null : phone);
        user.setInterests(interests.isEmpty() ? null : interests);
        user.setDifficulty(difficulty == null ? "Easy" : difficulty.getLabel());

        try {
            if (error == null && userDao.emailTaken(email, 0)) {
                error = "An account with this email already exists.";
            }

            if (error == null) {
                user.setPasswordHash(PasswordUtil.hash(password));
                user.setInviteCode(newInviteCode());
                user.setUserId(userDao.create(user));
                user.setRole("PLAYER");

                HttpSession session = Web.login(req, user);
                Web.flash(session, "success", "Account created successfully!");
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {            // ORA-00001: unique constraint (email)
                error = "An account with this email already exists.";
            } else {
                getServletContext().log("Signup failed", e);
                error = "We could not create your account right now. Please try again.";
            }
        }

        req.setAttribute("error", error);
        req.setAttribute("form", user);
        req.getRequestDispatcher("/signup.jsp").forward(req, resp);
    }

    /** SA + 6 digits, repeated until unused (same format as the original script). */
    private String newInviteCode() throws SQLException {
        String code;
        do {
            code = "SA" + ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (userDao.inviteCodeExists(code));
        return code;
    }
}
