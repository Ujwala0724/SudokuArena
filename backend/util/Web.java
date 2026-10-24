package com.sudokuarena.util;

import com.google.gson.Gson;
import com.sudokuarena.model.User;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Small helpers shared by the servlets. */
public final class Web {

    private static final Gson GSON = new Gson();

    private Web() { }

    /** One-shot message shown as a toast on the next page load. type: "success" or "error". */
    public static void flash(HttpSession session, String type, String message) {
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }

    /** Moves the flash message (if any) from the session to request attributes, once. */
    public static void consumeFlash(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return;
        Object message = session.getAttribute("flashMessage");
        if (message != null) {
            req.setAttribute("flashMessage", message);
            req.setAttribute("flashType", session.getAttribute("flashType"));
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashType");
        }
    }

    /** Starts a fresh session for the user (prevents session fixation). */
    public static HttpSession login(HttpServletRequest req, User user) {
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getUserId());   // stored as Long
        session.setAttribute("role", user.getRole());
        return session;
    }

    public static void json(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(GSON.toJson(body));
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
