package com.sudokuarena.dao;

import com.sudokuarena.model.Difficulty;
import com.sudokuarena.model.Stats;
import com.sudokuarena.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameDao {

    /** Marks any unfinished games of this player as ABANDONED (counted as losses). */
    public void abandonOpenGames(long userId) throws SQLException {
        String sql = "UPDATE games SET status = 'ABANDONED', completed_at = CURRENT_TIMESTAMP "
                   + "WHERE user_id = ? AND status = 'IN_PROGRESS'";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    /** Creates an IN_PROGRESS row and returns its game_id. */
    public long start(long userId, Difficulty difficulty) throws SQLException {
        String sql = "INSERT INTO games (game_id, user_id, difficulty) VALUES (games_seq.NEXTVAL, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "GAME_ID" })) {
            ps.setLong(1, userId);
            ps.setString(2, difficulty.getDbValue());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    /** Returns false if the game was not IN_PROGRESS (already finished or abandoned). */
    public boolean complete(long gameId, long userId, int seconds, int points) throws SQLException {
        String sql = "UPDATE games SET status = 'COMPLETED', time_taken = ?, points_earned = ?, "
                   + "xp_earned = ?, completed_at = CURRENT_TIMESTAMP "
                   + "WHERE game_id = ? AND user_id = ? AND status = 'IN_PROGRESS'";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, seconds);
            ps.setInt(2, points);
            ps.setInt(3, points);
            ps.setLong(4, gameId);
            ps.setLong(5, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Played = completed + abandoned, wins = completed, losses = abandoned.
     * Points and XP are summed from the games table, and streaks are worked out
     * from the order the finished games were played in.
     */
    public Stats statsFor(long userId) throws SQLException {
        Stats stats = new Stats();

        String totals = "SELECT "
                + "COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS wins, "
                + "COUNT(CASE WHEN status = 'ABANDONED' THEN 1 END) AS losses, "
                + "NVL(SUM(points_earned), 0) AS points, "
                + "NVL(SUM(xp_earned), 0) AS xp "
                + "FROM games WHERE user_id = ?";

        String history = "SELECT status FROM games WHERE user_id = ? "
                + "AND status IN ('COMPLETED', 'ABANDONED') ORDER BY game_id";

        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(totals)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    stats.setWins(rs.getInt("wins"));
                    stats.setLosses(rs.getInt("losses"));
                    stats.setPlayed(stats.getWins() + stats.getLosses());
                    stats.setPoints(rs.getLong("points"));
                    stats.setXp(rs.getLong("xp"));
                }
            }

            int current = 0, best = 0;
            try (PreparedStatement ps = con.prepareStatement(history)) {
                ps.setLong(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        if ("COMPLETED".equals(rs.getString(1))) {
                            current++;
                            best = Math.max(best, current);
                        } else {
                            current = 0;
                        }
                    }
                }
            }
            stats.setCurrentStreak(current);
            stats.setBestStreak(best);
        }
        return stats;
    }
}
