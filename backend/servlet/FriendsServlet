package com.sudokuarena.servlet;

import com.sudokuarena.dao.FriendDao;
import com.sudokuarena.dao.UserDao;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/friends")
public class FriendsServlet extends HttpServlet {

    private final FriendDao friendDao = new FriendDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/home?page=leaderboard");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        long userId = (Long) session.getAttribute("userId");
        String code = Web.trim(req.getParameter("code"));

        if (code.isEmpty()) {
            Web.flash(session, "error", "Enter a friend's invite code.");
        } else {
            try {
                switch (friendDao.addByInviteCode(userId, code, userDao)) {
                    case ADDED:
                        Web.flash(session, "success", "Friend added. They now appear on your leaderboard.");
                        break;
                    case ALREADY_FRIENDS:
                        Web.flash(session, "error", "This friend is already added.");
                        break;
                    case SELF:
                        Web.flash(session, "error", "That is your own invite code.");
                        break;
                    default:
                        Web.flash(session, "error", "Friend not found. Check the invite code and try again.");
                }
            } catch (SQLException e) {
                getServletContext().log("Add friend failed", e);
                Web.flash(session, "error", "The friend could not be added. Please try again.");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/home?page=leaderboard");
    }
}
