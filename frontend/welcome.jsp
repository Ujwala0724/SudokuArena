<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    if (session.getAttribute("userId") != null) {
        response.sendRedirect("home");
        return;
    }

    int[][] sample = {
        {5,3,0,0,7,0,0,0,0},
        {6,0,0,1,9,5,0,0,0},
        {0,9,8,0,0,0,0,6,0},
        {8,0,0,0,6,0,0,0,3},
        {4,0,0,8,0,3,0,0,1},
        {7,0,0,0,2,0,0,0,6},
        {0,6,0,0,0,0,2,8,0},
        {0,0,0,4,1,9,0,0,5},
        {0,0,0,0,8,0,0,7,9}
    };
    pageContext.setAttribute("sample", sample);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SudokuArena | Think · Solve · Grow</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <section class="welcome-screen">
        <div class="welcome-content">
            <div class="welcome-info">
                <div class="welcome-logo">S</div>

                <h1>Find your <span>quiet focus.</span></h1>

                <p>
                    A space to slow down, think clearly and grow
                    one number at a time.
                </p>

                <div class="welcome-actions">
                    <a href="signup.jsp" class="primary-button">Create account</a>
                    <a href="login.jsp" class="secondary-button">Log in</a>
                </div>
            </div>

            <div class="welcome-art">
                <div class="welcome-board">
                    <div class="welcome-grid">
                        <c:forEach var="row" items="${sample}">
                            <c:forEach var="value" items="${row}">
                                <div class="welcome-cell ${value == 0 ? 'highlight' : ''}">
                                    <c:if test="${value != 0}">${value}</c:if>
                                </div>
                            </c:forEach>
                        </c:forEach>
                    </div>

                    <div class="welcome-footer">
                        <span>Think logically</span>
                        <span>Play smarter</span>
                    </div>
                </div>
            </div>
        </div>
    </section>
</body>
</html>
