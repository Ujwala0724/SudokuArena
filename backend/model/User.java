package com.sudokuarena.model;

import java.sql.Timestamp;

public class User {
    private long userId;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String interests;
    private String difficulty = "Easy";   // display label: Easy, Medium, Hard, Expert
    private String inviteCode;
    private String role;
    private String status;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    // ---- Convenience getters used by the JSPs ----

    public String getInitial() {
        return (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
    }

    public String getFirstName() {
        if (name == null || name.trim().isEmpty()) return "";
        return name.trim().split("\\s+")[0];
    }

    public boolean isActive() { return "ACTIVE".equals(status); }
    public boolean isAdmin()  { return "ADMIN".equals(role); }
}
