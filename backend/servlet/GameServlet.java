package com.sudokuarena.servlet;

import com.sudokuarena.dao.GameDao;
import com.sudokuarena.model.ActiveGame;
import com.sudokuarena.model.Difficulty;
import com.sudokuarena.model.GameResult;
import com.sudokuarena.util.SudokuGenerator;
import com.sudokuarena.util.Web;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * AJAX endpoints used by the Sudoku board:
 *   POST game/start    difficulty            -> new puzzle (solution stays on the server)
 *   POST game/hint     row, col              -> the correct value for one empty cell
 *   POST game/finish   board, seconds, wrong -> verifies the board and saves the result
 *   POST game/abandon                        -> records the unfinished game as a loss
 */
@WebServlet("/game/*")
public class GameServlet extends HttpServlet {

    private static final String ACTIVE = "activeGame";

    private final GameDao gameDao = new GameDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getPathInfo() == null ? "" : req.getPathInfo();
        HttpSession session = req.getSession();
        long userId = (Long) session.getAttribute("userId");

        try {
            switch (action) {
                case "/start":   start(req, resp, session, userId);   break;
                case "/hint":    hint(req, resp, session);            break;
                case "/finish":  finish(req, resp, session, userId);  break;
                case "/abandon": abandon(resp, session, userId);      break;
                default:         Web.json(resp, 404, Map.of("error", "Unknown action."));
            }
        } catch (SQLException e) {
            getServletContext().log("Game request failed", e);
            Web.json(resp, 500, Map.of("error", "Database error. Please try again."));
        }
    }

    private void start(HttpServletRequest req, HttpServletResponse resp,
                       HttpSession session, long userId) throws SQLException, IOException {

        Difficulty difficulty = Difficulty.parse(req.getParameter("difficulty"));
        if (difficulty == null) {
            Web.json(resp, 400, Map.of("error", "Unknown difficulty."));
            return;
        }

        gameDao.abandonOpenGames(userId);               // an unfinished game counts as a loss

        int[][][] generated = SudokuGenerator.generate(difficulty);
        long gameId = gameDao.start(userId, difficulty);
        ActiveGame game = new ActiveGame(gameId, difficulty, generated[0], generated[1]);
        session.setAttribute(ACTIVE, game);

        Web.json(resp, 200, Map.of(
                "gameId", gameId,
                "difficulty", difficulty.getLabel(),
                "puzzle", game.getPuzzle(),
                "hintsLeft", game.getHintsLeft()));
    }

    private void hint(HttpServletRequest req, HttpServletResponse resp, HttpSession session)
            throws IOException {

        ActiveGame game = (ActiveGame) session.getAttribute(ACTIVE);
        if (game == null) {
            Web.json(resp, 409, Map.of("error", "No game in progress."));
            return;
        }

        int row = toInt(req.getParameter("row"), -1);
        int col = toInt(req.getParameter("col"), -1);
        if (row < 0 || row > 8 || col < 0 || col > 8) {
            Web.json(resp, 400, Map.of("error", "Select a cell first."));
            return;
        }
        if (game.getPuzzle()[row][col] != 0) {
            Web.json(resp, 400, Map.of("error", "Choose a cell that was originally empty."));
            return;
        }
        if (game.getHintsLeft() <= 0) {
            Web.json(resp, 400, Map.of("error", "No hints remaining."));
            return;
        }

        game.useHint();
        Web.json(resp, 200, Map.of(
                "value", game.getSolution()[row][col],
                "hintsLeft", game.getHintsLeft()));
    }

    private void finish(HttpServletRequest req, HttpServletResponse resp,
                        HttpSession session, long userId) throws SQLException, IOException {

        ActiveGame game = (ActiveGame) session.getAttribute(ACTIVE);
        if (game == null) {
            Web.json(resp, 409, Map.of("error", "No game in progress."));
            return;
        }

        String raw = Web.trim(req.getParameter("board"));
        if (!raw.matches("[0-9]{81}")) {
            Web.json(resp, 400, Map.of("error", "The board could not be read."));
            return;
        }

        int[][] board = new int[9][9];
        for (int i = 0; i < 81; i++) board[i / 9][i % 9] = raw.charAt(i) - '0';

        if (!SudokuGenerator.isValidSolution(board, game.getPuzzle())) {
            Web.json(resp, 422, Map.of("error", "Some answers are incorrect. Check your board."));
            return;
        }

        // Trust the clock on the server: the client time can never be longer than the real time.
        long serverSeconds = (System.currentTimeMillis() - game.getStartedAtMillis()) / 1000;
        int seconds = (int) Math.max(0, Math.min(toInt(req.getParameter("seconds"), 0), serverSeconds));
        int wrong = Math.max(0, Math.min(toInt(req.getParameter("wrong"), 0), 999));

        int points = Math.max(50, 500 - seconds / 5 - wrong * 10);
        int accuracy = (int) Math.round(81.0 * 100 / (81 + wrong));

        if (!gameDao.complete(game.getGameId(), userId, seconds, points)) {
            Web.json(resp, 409, Map.of("error", "This game was already finished."));
            return;
        }

        session.removeAttribute(ACTIVE);
        session.setAttribute("lastResult", new GameResult(
                game.getDifficulty().getLabel(),
                String.format("%02d:%02d", seconds / 60, seconds % 60),
                accuracy,
                points,
                wrong == 0 ? "Perfect focus! No incorrect entries." : "Excellent work!"));

        Web.json(resp, 200, Map.of("ok", true, "redirect", "home?page=results"));
    }

    private void abandon(HttpServletResponse resp, HttpSession session, long userId)
            throws SQLException, IOException {
        gameDao.abandonOpenGames(userId);
        session.removeAttribute(ACTIVE);
        Web.json(resp, 200, Map.of("ok", true));
    }

    private static int toInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
