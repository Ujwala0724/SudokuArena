package com.sudokuarena.model;

/** Shown once on the results page after a puzzle is solved. */
public class GameResult {
    private final String difficulty;
    private final String timeText;
    private final int accuracy;
    private final int points;
    private final String praise;

    public GameResult(String difficulty, String timeText, int accuracy, int points, String praise) {
        this.difficulty = difficulty;
        this.timeText = timeText;
        this.accuracy = accuracy;
        this.points = points;
        this.praise = praise;
    }

    public String getDifficulty() { return difficulty; }
    public String getTimeText()   { return timeText; }
    public int getAccuracy()      { return accuracy; }
    public int getPoints()        { return points; }
    public String getPraise()     { return praise; }
}
