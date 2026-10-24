<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (session.getAttribute("userId") != null) {
        response.sendRedirect("home");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Log in | SudokuArena</title>
    <link rel="stylesheet" href="style.css">
</head>
<body class="auth-page">
    <main class="auth-card">
        <div class="auth-logo">SudokuArena</div>
        <h1>Welcome back</h1>
        <p class="auth-subtitle">Your next challenge awaits.</p>

        <c:if test="${not empty error}">
            <div class="auth-error"><c:out value="${error}"/></div>
        </c:if>

        <form class="auth-form" action="login" method="post">
            <div class="form-group">
                <label for="email">Email address</label>
                <input type="email" id="email" name="email" maxlength="100"
                       autocomplete="email" placeholder="you@example.com"
                       value="<c:out value='${email}'/>" required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password"
                       autocomplete="current-password" placeholder="Your password" required>
            </div>

            <label class="checkbox-row">
                <input type="checkbox" name="remember" value="1">
                Remember me
            </label>

            <button type="submit" class="primary-button">Log in</button>
        </form>

        <p class="auth-footer">
            New here? <a href="signup.jsp">Create account</a>
        </p>
        <a class="auth-back" href="welcome.jsp">Back to welcome</a>
    </main>
</body>
</html>
