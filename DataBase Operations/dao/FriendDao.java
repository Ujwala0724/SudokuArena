package com.sudokuarena.dao;

import com.sudokuarena.model.LeaderEntry;
import com.sudokuarena.model.User;
import com.sudokuarena.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    public enum AddResult { ADDED, NOT_FOUND, SELF, ALREADY_FRIENDS }

    /** The player plus their accepted friends, ranked by points. */
    public List<LeaderEntry> leaderboard(long userId) throws SQLException {
        String sql = "SELECT u.user_id, u.name, "
                + "NVL(SUM(CASE WHEN g.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS wins, "
                + "NVL(SUM(g.points_earned), 0) AS points "
                + "FROM users u LEFT JOIN games g ON g.user_id = u.user_id "
                + "WHERE u.account_status = 'ACTIVE' AND (u.user_id = ? "
                + "  OR u.user_id IN (SELECT friend_id FROM friends WHERE user_id = ? AND status = 'ACCEPTED') "
                + "  OR u.user_id IN (SELECT user_id FROM friends WHERE friend_id = ? AND status = 'ACCEPTED')) "
                + "GROUP BY u.user_id, u.name "
                + "ORDER BY points DESC, wins DESC, u.name";

        List<LeaderEntry> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
            ps.setLong(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    LeaderEntry e = new LeaderEntry();
                    e.setRank(rank++);
                    e.setUserId(rs.getLong("user_id"));
                    e.setName(rs.getString("name"));
                    e.setWins(rs.getInt("wins"));
                    e.setPoints(rs.getLong("points"));
                    e.setMe(e.getUserId() == userId);
                    list.add(e);
                }
            }
        }
        return list;
    }

    /** Adds the owner of an invite code as an accepted friend. */
    public AddResult addByInviteCode(long userId, String code, UserDao userDao) throws SQLException {
        User friend = userDao.findByInviteCode(code);
        if (friend == null || !friend.isActive()) return AddResult.NOT_FOUND;
        if (friend.getUserId() == userId) return AddResult.SELF;

        String check = "SELECT COUNT(*) FROM friends WHERE (user_id = ? AND friend_id = ?) "
                     + "OR (user_id = ? AND friend_id = ?)";
        String insert = "INSERT INTO friends (friendship_id, user_id, friend_id, status) "
                      + "VALUES (friends_seq.NEXTVAL, ?, ?, 'ACCEPTED')";

        try (Connection con = DBUtil.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(check)) {
                ps.setLong(1, userId);
                ps.setLong(2, friend.getUserId());
                ps.setLong(3, friend.getUserId());
                ps.setLong(4, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) return AddResult.ALREADY_FRIENDS;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(insert)) {
                ps.setLong(1, userId);
                ps.setLong(2, friend.getUserId());
                ps.executeUpdate();
            }
        }
        return AddResult.ADDED;
    }
}
