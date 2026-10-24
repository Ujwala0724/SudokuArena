package com.sudokuarena.model;

public class LeaderEntry {
    private int rank;
    private long userId;
    private String name;
    private int wins;
    private long points;
    private boolean me;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }

    public boolean isMe() { return me; }
    public void setMe(boolean me) { this.me = me; }

    public String getInitial() {
        return (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
    }
}
