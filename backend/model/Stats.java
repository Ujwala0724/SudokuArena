package com.sudokuarena.model;

public class Stats {
    private int played;
    private int wins;
    private int losses;
    private long points;
    private long xp;
    private int bestStreak;
    private int currentStreak;

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public long getPoints() { return points; }
    public void setPoints(long points) { this.points = points; }

    public long getXp() { return xp; }
    public void setXp(long xp) { this.xp = xp; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    /** Same rule as the original script: one level per 1000 XP. */
    public int getLevel() { return (int) (xp / 1000) + 1; }

    /** Percentage progress towards the next level (0-99). */
    public int getLevelProgress() { return (int) ((xp % 1000) / 10); }
}
