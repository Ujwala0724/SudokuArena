package com.sudokuarena.servlet;

import com.sudokuarena.dao.FriendDao;
import com.sudokuarena.dao.GameDao;
import com.sudokuarena.dao.UserDao;
import com.sudokuarena.model.Difficulty;
import com.sudokuarena.model.GameResult;
import com.sudokuarena.model.User;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Loads everything the player dashboard needs, then hands it to app.jsp. */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final Set<String> PAGES =
            Set.of("home", "levels", "leaderboard", "profile", "results");

    private final UserDao userDao = new UserDao();
    private final GameDao gameDao = new GameDao();
    private final FriendDao friendDao = new FriendDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        long userId = (Long) session.getAttribute("userId");

        try {
            User user = userDao.findById(userId);
            if (user == null || !user.isActive()) {
                session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login.jsp");
                return;
            }

            String page = req.getParameter("page");
            if (page == null || !PAGES.contains(page)) page = "home";

            GameResult result = (GameResult) session.getAttribute("lastResult");
            if ("results".equals(page)) {
                if (result == null) page = "home";
                session.removeAttribute("lastResult");      // show it once
            } else {
                result = null;
            }

            req.setAttribute("user", user);
            req.setAttribute("stats", gameDao.statsFor(userId));
            req.setAttribute("leaders", friendDao.leaderboard(userId));
            req.setAttribute("levels", Difficulty.values());
            req.setAttribute("result", result);
            req.setAttribute("startPage", page);
            Web.consumeFlash(req);

            resp.setHeader("Cache-Control", "no-store");
            req.getRequestDispatcher("/WEB-INF/views/app.jsp").forward(req, resp);

        } catch (SQLException e) {
            getServletContext().log("Could not load the dashboard", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The database is not available. Check db.properties and that Oracle is running.");
        }
    }
}
