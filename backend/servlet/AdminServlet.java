package com.sudokuarena.servlet;

import com.sudokuarena.dao.UserDao;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String search = Web.trim(req.getParameter("search"));
            req.setAttribute("users", userDao.search(search));
            req.setAttribute("totalUsers", userDao.count());
            req.setAttribute("search", search);
        } catch (SQLException e) {
            getServletContext().log("Admin list failed", e);
            req.setAttribute("error", "The user list could not be loaded.");
        }
        Web.consumeFlash(req);
        req.getRequestDispatcher("/WEB-INF/views/adminDashboard.jsp").forward(req, resp);
    }

    /** Activate or deactivate a player account. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        long adminId = (Long) session.getAttribute("userId");
        String action = Web.trim(req.getParameter("action"));
        String search = Web.trim(req.getParameter("search"));

        try {
            long targetId = Long.parseLong(Web.trim(req.getParameter("id")));

            if (targetId == adminId) {
                Web.flash(session, "error", "You cannot change your own account status.");
            } else if ("deactivate".equals(action)) {
                userDao.setStatus(targetId, "INACTIVE");
                Web.flash(session, "success", "Account deactivated.");
            } else if ("activate".equals(action)) {
                userDao.setStatus(targetId, "ACTIVE");
                Web.flash(session, "success", "Account activated.");
            } else {
                Web.flash(session, "error", "Unknown action.");
            }
        } catch (NumberFormatException e) {
            Web.flash(session, "error", "Invalid user.");
        } catch (SQLException e) {
            getServletContext().log("Admin update failed", e);
            Web.flash(session, "error", "The change could not be saved.");
        }

        String back = search.isEmpty() ? "" : "?search=" + URLEncoder.encode(search, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/admin" + back);
    }
}
