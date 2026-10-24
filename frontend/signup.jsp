<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (session.getAttribute("userId") != null) {
        response.sendRedirect("home");
        return;
    }
    // "form" is set by SignupServlet when validation fails, so the fields stay filled in.
    pageContext.setAttribute("levels", com.sudokuarena.model.Difficulty.values());
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account | SudokuArena</title>
    <link rel="stylesheet" href="style.css">
</head>
<body class="auth-page">
    <main class="auth-card">
        <div class="auth-logo">SudokuArena</div>
        <h1>Create your account</h1>
        <p class="auth-subtitle">Begin your journey of logic.</p>

        <c:if test="${not empty error}">
            <div class="auth-error"><c:out value="${error}"/></div>
        </c:if>

        <form class="auth-form" action="signup" method="post">
            <div class="form-group">
                <label for="name">Full name</label>
                <input type="text" id="name" name="name" maxlength="100"
                       autocomplete="name" placeholder="Enter your full name"
                       value="<c:out value='${form.name}'/>" required>
            </div>

            <div class="form-group">
                <label for="email">Email address</label>
                <input type="email" id="email" name="email" maxlength="100"
                       autocomplete="email" placeholder="you@example.com"
                       value="<c:out value='${form.email}'/>" required>
            </div>

            <div class="form-group">
                <label for="phone">Phone number</label>
                <input type="tel" id="phone" name="phone" maxlength="15"
                       autocomplete="tel" placeholder="Enter phone number"
                       value="<c:out value='${form.phone}'/>" required>
            </div>

            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" minlength="8"
                       autocomplete="new-password" placeholder="At least 8 characters" required>
            </div>

            <div class="form-group">
                <label for="interests">Interests</label>
                <input type="text" id="interests" name="interests" maxlength="500"
                       placeholder="Puzzles, reading, logic"
                       value="<c:out value='${form.interests}'/>">
            </div>

            <div class="form-group">
                <label for="difficulty">Preferred difficulty</label>
                <select id="difficulty" name="difficulty" required>
                    <c:forEach var="lv" items="${levels}">
                        <option value="${lv.label}" ${lv.label == form.difficulty ? 'selected' : ''}>${lv.label}</option>
                    </c:forEach>
                </select>
            </div>

            <button type="submit" class="primary-button">Create account</button>
        </form>

        <p class="auth-footer">
            Already registered? <a href="login.jsp">Log in</a>
        </p>
        <a class="auth-back" href="welcome.jsp">Back to welcome</a>
    </main>
</body>
</html>
