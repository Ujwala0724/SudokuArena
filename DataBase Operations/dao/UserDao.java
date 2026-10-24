package com.sudokuarena.dao;

import com.sudokuarena.model.Difficulty;
import com.sudokuarena.model.User;
import com.sudokuarena.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private static final String COLUMNS =
            "user_id, name, email, phone, password_hash, interests, preferred_difficulty, "
          + "invite_code, role, account_status, created_at, last_login";

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPhone(rs.getString("phone"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setInterests(rs.getString("interests"));
        u.setDifficulty(Difficulty.fromDb(rs.getString("preferred_difficulty")).getLabel());
        u.setInviteCode(rs.getString("invite_code"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("account_status"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        u.setLastLogin(rs.getTimestamp("last_login"));
        return u;
    }

    private User findOne(String where, Object param) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE " + where;
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findById(long id) throws SQLException {
        return findOne("user_id = ?", id);
    }

    public User findByEmail(String email) throws SQLException {
        return findOne("email = ?", email.toLowerCase());
    }

    public User findByInviteCode(String code) throws SQLException {
        return findOne("invite_code = ?", code.toUpperCase());
    }

    /** True if another user already uses this email (excludeId = 0 for a new account). */
    public boolean emailTaken(String email, long excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND user_id <> ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public boolean inviteCodeExists(String code) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE invite_code = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** Inserts a new PLAYER account and returns its generated user_id. */
    public long create(User u) throws SQLException {
        String sql = "INSERT INTO users (user_id, name, email, phone, password_hash, interests, "
                   + "preferred_difficulty, invite_code) "
                   + "VALUES (users_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] { "USER_ID" })) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail().toLowerCase());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getInterests());
            ps.setString(6, Difficulty.fromDb(u.getDifficulty()).getDbValue());
            ps.setString(7, u.getInviteCode());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public void updateProfile(User u) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, interests = ?, "
                   + "preferred_difficulty = ? WHERE user_id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getName());
            ps.setString(2, u.getEmail().toLowerCase());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getInterests());
            ps.setString(5, Difficulty.fromDb(u.getDifficulty()).getDbValue());
            ps.setLong(6, u.getUserId());
            ps.executeUpdate();
        }
    }

    public void touchLastLogin(long userId) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    // ---------------- admin ----------------

    public int count() throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /** Search by name or email (blank search returns everyone, newest limit 200). */
    public List<User> search(String text) throws SQLException {
        String sql = "SELECT * FROM (SELECT " + COLUMNS + " FROM users "
                   + "WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ORDER BY user_id) "
                   + "WHERE ROWNUM <= 200";
        String like = "%" + (text == null ? "" : text.trim().toLowerCase()) + "%";
        List<User> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public void setStatus(long userId, String status) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET account_status = ? WHERE user_id = ?")) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }
}
