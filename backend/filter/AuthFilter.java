package com.sudokuarena.filter;

import com.sudokuarena.util.Web;
import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Keeps logged-out visitors out of the player area and non-admins out of /admin. */
@WebFilter(urlPatterns = { "/home", "/game/*", "/profile", "/friends", "/admin" })
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;
        String path = req.getServletPath();
        String ctx = req.getContextPath();

        if (!loggedIn) {
            if (path.startsWith("/game")) {
                Web.json(resp, 401, Map.of("error", "Please log in again."));
            } else {
                resp.sendRedirect(ctx + "/login.jsp");
            }
            return;
        }

        boolean admin = "ADMIN".equals(session.getAttribute("role"));

        if (path.equals("/admin") && !admin) {
            resp.sendRedirect(ctx + "/home");
            return;
        }
        if (admin && !path.equals("/admin")) {
            resp.sendRedirect(ctx + "/admin");
            return;
        }

        chain.doFilter(request, response);
    }
}
