package com.sudokuarena.model;

/**
 * The puzzle currently being played. It lives in the HttpSession, so the
 * solution never reaches the browser (hints are asked from the server).
 */
public class ActiveGame {
    private final long gameId;
    private final Difficulty difficulty;
    private final int[][] puzzle;
    private final int[][] solution;
    private final long startedAtMillis;
    private int hintsLeft = 3;

    public ActiveGame(long gameId, Difficulty difficulty, int[][] puzzle, int[][] solution) {
        this.gameId = gameId;
        this.difficulty = difficulty;
        this.puzzle = puzzle;
        this.solution = solution;
        this.startedAtMillis = System.currentTimeMillis();
    }

    public long getGameId()          { return gameId; }
    public Difficulty getDifficulty(){ return difficulty; }
    public int[][] getPuzzle()       { return puzzle; }
    public int[][] getSolution()     { return solution; }
    public long getStartedAtMillis() { return startedAtMillis; }
    public int getHintsLeft()        { return hintsLeft; }
    public void useHint()            { hintsLeft--; }
}
