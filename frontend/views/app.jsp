<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SudokuArena</title>
    <link rel="stylesheet" href="${ctx}/style.css">
</head>
<body>

<div class="app-shell" id="appShell"
     data-ctx="${ctx}"
     data-start-page="${startPage}"
     data-flash-type="<c:out value='${flashType}'/>"
     data-flash="<c:out value='${flashMessage}'/>">

    <!-- ==================== SIDEBAR ==================== -->
    <aside class="sidebar">
        <div class="sidebar-brand">
            <span class="logo-mark">S</span>
            <div>
                <h2>SudokuArena</h2>
                <p>Think · Solve · Grow</p>
            </div>
        </div>

        <p class="sidebar-label">Menu</p>
        <nav class="sidebar-nav">
            <button type="button" class="nav-item" data-page="home"><span>⌂</span><span class="nav-text">Home</span></button>
            <button type="button" class="nav-item" data-page="levels"><span>▦</span><span class="nav-text">Play</span></button>
            <button type="button" class="nav-item" data-page="leaderboard"><span>♛</span><span class="nav-text">Leaderboard</span></button>
            <button type="button" class="nav-item" data-page="profile"><span>◎</span><span class="nav-text">Profile</span></button>
        </nav>

        <div class="sidebar-bottom">
            <form action="${ctx}/logout" method="post">
                <button type="submit" class="nav-item"><span>↗</span><span class="nav-text">Log out</span></button>
            </form>
        </div>
    </aside>

    <!-- ==================== MAIN ==================== -->
    <div class="main-content">
        <header class="topbar">
            <span class="topbar-title">Welcome back, <c:out value="${user.firstName}"/></span>
            <div class="user-chip">
                <span class="avatar"><c:out value="${user.initial}"/></span>
                <span><c:out value="${user.name}"/></span>
            </div>
        </header>

        <div class="page-container">

            <!-- ==================== HOME ==================== -->
            <section class="page ${startPage == 'home' ? 'active' : ''}" id="page-home">
                <div class="welcome-banner">
                    <div>
                        <h1>Make every move count.</h1>
                        <p>Challenge your mind. Build your focus. Celebrate progress.</p>
                        <div class="button-row">
                            <button type="button" class="primary-button" data-page="levels">Start playing →</button>
                            <button type="button" class="secondary-button" data-page="profile">View profile</button>
                        </div>
                    </div>
                    <div class="banner-decoration">▦</div>
                </div>

                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon">▦</div>
                        <p>Games played</p>
                        <h2>${stats.played}</h2>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">♛</div>
                        <p>Victories</p>
                        <h2>${stats.wins}</h2>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">✦</div>
                        <p>Points earned</p>
                        <h2>${stats.points}</h2>
                    </div>
                </div>

                <div class="section-heading">
                    <div>
                        <h2>Choose your challenge</h2>
                        <p class="muted">Every level is a new opportunity to grow.</p>
                    </div>
                    <button type="button" class="text-link" data-page="levels">View all</button>
                </div>

                <div class="level-grid">
                    <c:forEach var="lv" items="${levels}">
                        <button type="button" class="level-card" data-level="${lv.label}">
                            <div class="level-icon">▦</div>
                            <h3>${lv.label}</h3>
                            <p>${lv.tagline}</p>
                            <div class="level-meta">
                                <span>◷ ${lv.duration}</span>
                                <span>${lv.intensity}</span>
                            </div>
                        </button>
                    </c:forEach>
                </div>
            </section>

            <!-- ==================== DIFFICULTY ==================== -->
            <section class="page ${startPage == 'levels' ? 'active' : ''}" id="page-levels">
                <div class="page-heading">
                    <div>
                        <h1>Choose your challenge</h1>
                        <p>Pick your pace. Each puzzle is generated fresh for you.</p>
                    </div>
                </div>

                <div class="difficulty-grid">
                    <c:forEach var="lv" items="${levels}">
                        <button type="button" class="difficulty-option" data-level="${lv.label}">
                            <h3>${lv.label}
                                <c:if test="${lv.label == user.difficulty}"><span class="badge">Your preference</span></c:if>
                            </h3>
                            <p>${lv.description}</p>
                            <p>◷ ~ ${lv.duration} · ${lv.intensity}</p>
                        </button>
                    </c:forEach>
                </div>
            </section>

            <!-- ==================== GAME (filled in by script.js) ==================== -->
            <section class="page" id="page-game">
                <div class="game-top-info">
                    <button type="button" class="secondary-button" data-page="levels">← Back to levels</button>
                    <span class="game-level-label" id="gameLevel">Easy</span>
                    <span class="game-timer" id="timer">00:00</span>
                </div>

                <div class="game-layout">
                    <div class="game-main">
                        <div class="game-panel">
                            <div class="sudoku-board" id="sudokuBoard" role="grid" aria-label="Sudoku board"></div>

                            <div class="number-pad" id="numberPad" aria-label="Number pad"></div>

                            <div class="game-tools">
                                <button type="button" class="secondary-button" id="undoButton">↶ Undo</button>
                                <button type="button" class="secondary-button" id="hintButton">✦ Hint</button>
                                <button type="button" class="secondary-button" id="eraseButton">⌫ Erase</button>
                                <button type="button" class="secondary-button" id="notesButton">✎ Notes off</button>
                                <button type="button" class="secondary-button" id="checkButton">✓ Check</button>
                                <button type="button" class="secondary-button" id="pauseButton">Ⅱ Pause</button>
                                <button type="button" class="secondary-button" id="resetButton">↻ Reset</button>
                            </div>

                            <p class="game-message" id="gameMessage" aria-live="polite">
                                Select an empty cell, then choose a number.
                            </p>
                        </div>
                    </div>

                    <aside class="game-sidebar">
                        <div class="panel">
                            <h3>Puzzle progress</h3>
                            <div class="progress-track"><div class="progress-fill" id="progressBar"></div></div>
                            <div class="progress-info">
                                <span id="filledCount">0 / 81 cells</span>
                                <strong id="progressPercent">0%</strong>
                            </div>
                        </div>

                        <div class="panel">
                            <h3>This game</h3>
                            <div class="game-stat-row"><span>Wrong moves</span><strong id="wrongMovesCount">0</strong></div>
                            <div class="game-stat-row"><span>Hints left</span><strong id="hintsLeft">3</strong></div>
                            <div class="game-stat-row"><span>Notes mode</span><strong id="notesStatus">Off</strong></div>
                        </div>

                        <div class="panel">
                            <h3>How to play</h3>
                            <p class="muted">
                                Fill each row, column and 3 × 3 box with the numbers 1–9.
                                Each number can appear only once in each.
                                Matching numbers are highlighted to help you focus.
                            </p>
                        </div>
                    </aside>
                </div>
            </section>

            <!-- ==================== RESULTS (only after a win) ==================== -->
            <c:if test="${not empty result}">
                <section class="page ${startPage == 'results' ? 'active' : ''}" id="page-results">
                    <div class="results-container">
                        <div class="result-icon">♛</div>
                        <span class="badge">Puzzle complete</span>
                        <h1>Well done!</h1>
                        <p>You made every move count.</p>

                        <div class="result-stats">
                            <div class="result-stat"><p>Completion time</p><h2>${result.timeText}</h2></div>
                            <div class="result-stat"><p>Accuracy</p><h2>${result.accuracy}%</h2></div>
                            <div class="result-stat"><p>Points earned</p><h2>${result.points}</h2></div>
                        </div>

                        <p class="praise-line">${result.praise}</p>
                        <p class="muted">Difficulty: ${result.difficulty}</p>

                        <div class="result-stats">
                            <div class="result-stat"><p>Level</p><h2>${stats.level}</h2></div>
                            <div class="result-stat"><p>Total XP</p><h2>${stats.xp}</h2></div>
                            <div class="result-stat"><p>Best streak</p><h2>${stats.bestStreak}</h2></div>
                        </div>

                        <div class="progress-track level-track"><div class="progress-fill" style="width:${stats.levelProgress}%"></div></div>
                        <p class="record-line">
                            Games: ${stats.played} · Wins: ${stats.wins} · Losses: ${stats.losses}
                        </p>

                        <div class="results-actions">
                            <button type="button" class="primary-button" data-page="home">⌂ Return home</button>
                            <button type="button" class="secondary-button" data-level="${result.difficulty}">↻ Play again</button>
                        </div>
                    </div>
                </section>
            </c:if>

            <!-- ==================== PROFILE ==================== -->
            <section class="page ${startPage == 'profile' ? 'active' : ''}" id="page-profile">
                <div class="page-heading">
                    <div>
                        <h1>Player profile</h1>
                        <p>Make it yours. Keep track of your progress.</p>
                    </div>
                </div>

                <div class="profile-card">
                    <div class="profile-header">
                        <div class="profile-avatar"><c:out value="${user.initial}"/></div>
                        <div>
                            <h2><c:out value="${user.name}"/></h2>
                            <p><c:out value="${user.email}"/></p>
                        </div>
                    </div>

                    <form action="${ctx}/profile" method="post">
                        <div class="form-grid">
                            <div class="form-group">
                                <label for="profileName">Full name</label>
                                <input type="text" id="profileName" name="name" maxlength="100"
                                       value="<c:out value='${user.name}'/>" required>
                            </div>

                            <div class="form-group">
                                <label for="profileEmail">Email address</label>
                                <input type="email" id="profileEmail" name="email" maxlength="100"
                                       value="<c:out value='${user.email}'/>" required>
                            </div>

                            <div class="form-group">
                                <label for="profilePhone">Phone number</label>
                                <input type="tel" id="profilePhone" name="phone" maxlength="15"
                                       placeholder="Optional phone number"
                                       value="<c:out value='${user.phone}'/>">
                            </div>

                            <div class="form-group">
                                <label for="profileDifficulty">Preferred difficulty</label>
                                <select id="profileDifficulty" name="difficulty">
                                    <c:forEach var="lv" items="${levels}">
                                        <option value="${lv.label}" ${lv.label == user.difficulty ? 'selected' : ''}>${lv.label}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group full-width">
                                <label for="profileInterests">Interests</label>
                                <input type="text" id="profileInterests" name="interests" maxlength="500"
                                       placeholder="Puzzles, reading, logic"
                                       value="<c:out value='${user.interests}'/>">
                            </div>

                            <div class="form-group full-width">
                                <div class="button-row">
                                    <button type="submit" class="primary-button">Save profile</button>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>

                <h2 class="profile-section-title">Your statistics</h2>
                <div class="profile-stats">
                    <div class="profile-stat"><p>Games</p><h3>${stats.played}</h3></div>
                    <div class="profile-stat"><p>Wins</p><h3>${stats.wins}</h3></div>
                    <div class="profile-stat"><p>Losses</p><h3>${stats.losses}</h3></div>
                    <div class="profile-stat"><p>Points</p><h3>${stats.points}</h3></div>
                </div>
            </section>

            <!-- ==================== LEADERBOARD ==================== -->
            <section class="page ${startPage == 'leaderboard' ? 'active' : ''}" id="page-leaderboard">
                <div class="page-heading">
                    <div>
                        <h1>Leaderboard</h1>
                        <p>Celebrate progress and challenge your friends.</p>
                    </div>
                </div>

                <div class="panel panel-spaced">
                    <h3>Your invite code</h3>
                    <p class="muted">Share it with friends so they can add you and compare scores.</p>

                    <div class="invite-section">
                        <input id="inviteCode" readonly value="<c:out value='${user.inviteCode}'/>">
                        <button type="button" class="secondary-button" id="copyInviteButton">Copy code</button>
                    </div>

                    <form class="invite-section" action="${ctx}/friends" method="post">
                        <input type="text" name="code" maxlength="16"
                               placeholder="Enter a friend's code" aria-label="Friend invite code" required>
                        <button type="submit" class="primary-button">Add friend</button>
                    </form>
                </div>

                <div class="leaderboard-panel">
                    <div class="leaderboard-header">
                        <h2>Player rankings</h2>
                    </div>

                    <table class="leaderboard-table">
                        <thead>
                            <tr><th>Rank</th><th>Player</th><th>Wins</th><th>Points</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="entry" items="${leaders}">
                                <tr class="${entry.me ? 'me' : ''}">
                                    <td class="leader-rank">#${entry.rank}</td>
                                    <td>
                                        <div class="leader-user">
                                            <span class="leader-avatar"><c:out value="${entry.initial}"/></span>
                                            <span><c:out value="${entry.name}"/><c:if test="${entry.me}"> (you)</c:if></span>
                                        </div>
                                    </td>
                                    <td>${entry.wins}</td>
                                    <td>${entry.points}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </section>

        </div>
    </div>
</div>

<div class="toast" id="toast" role="status" aria-live="polite"></div>

<script src="${ctx}/script.js"></script>
</body>
</html>
