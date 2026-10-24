<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard | SudokuArena</title>
    <link rel="stylesheet" href="${ctx}/style.css">
</head>
<body>
    <main class="admin-container">
        <header class="admin-header">
            <div>
                <h1>SudokuArena Admin</h1>
                <p>Manage registered players.</p>
            </div>
            <form action="${ctx}/logout" method="post">
                <button class="secondary-button" type="submit">Log out</button>
            </form>
        </header>

        <section class="admin-card">
            <p class="muted">Registered users</p>
            <h2>${empty totalUsers ? 0 : totalUsers}</h2>
        </section>

        <section class="admin-card">
            <h2>User management</h2>

            <c:if test="${not empty error}">
                <p class="admin-note error"><c:out value="${error}"/></p>
            </c:if>
            <c:if test="${not empty flashMessage}">
                <p class="admin-note ${flashType}"><c:out value="${flashMessage}"/></p>
            </c:if>

            <form class="admin-search" action="${ctx}/admin" method="get">
                <input type="text" name="search" placeholder="Search by name or email"
                       value="<c:out value='${search}'/>">
                <button class="primary-button" type="submit">Search</button>
            </form>

            <div class="table-wrap">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>User ID</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Joined</th>
                            <th>Last login</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="u" items="${users}">
                            <tr>
                                <td>${u.userId}</td>
                                <td><c:out value="${u.name}"/></td>
                                <td><c:out value="${u.email}"/></td>
                                <td>${u.role}</td>
                                <td class="${u.active ? 'status-active' : 'status-inactive'}">${u.status}</td>
                                <td><fmt:formatDate value="${u.createdAt}" pattern="dd MMM yyyy"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${empty u.lastLogin}">—</c:when>
                                        <c:otherwise><fmt:formatDate value="${u.lastLogin}" pattern="dd MMM yyyy, HH:mm"/></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${u.userId != sessionScope.userId}">
                                        <form action="${ctx}/admin" method="post">
                                            <input type="hidden" name="id" value="${u.userId}">
                                            <input type="hidden" name="search" value="<c:out value='${search}'/>">
                                            <input type="hidden" name="action" value="${u.active ? 'deactivate' : 'activate'}">
                                            <button type="submit" class="${u.active ? 'danger-button' : 'secondary-button'} small-button">
                                                ${u.active ? 'Deactivate' : 'Activate'}
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty users}">
                            <tr><td colspan="8">No users found.</td></tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</body>
</html>
