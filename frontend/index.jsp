<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    // Entry point: players go to their dashboard, everyone else to the welcome page.
    response.sendRedirect(session.getAttribute("userId") != null ? "home" : "welcome.jsp");
%>
