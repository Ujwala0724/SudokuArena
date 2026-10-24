package com.sudokuarena.util;

import com.sudokuarena.model.Difficulty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Java port of the original JavaScript generator (randomized backtracking). */
public final class SudokuGenerator {

    private static final Random RANDOM = new Random();

    private SudokuGenerator() { }

    /** Returns {puzzle, solution}. Zero means an empty cell. */
    public static int[][][] generate(Difficulty difficulty) {
        int[][] solution = new int[9][9];
        fill(solution);

        int[][] puzzle = copy(solution);
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 81; i++) positions.add(i);
        Collections.shuffle(positions, RANDOM);

        for (int i = 0; i < difficulty.getRemovals(); i++) {
            int p = positions.get(i);
            puzzle[p / 9][p % 9] = 0;
        }
        return new int[][][] { puzzle, solution };
    }

    /**
     * A finished board is accepted when it keeps every given clue and every
     * row, column and 3x3 box holds 1-9 exactly once. Checking the rules (rather
     * than comparing with one stored solution) means any valid solution counts,
     * even if the puzzle happens to have more than one.
     */
    public static boolean isValidSolution(int[][] board, int[][] puzzle) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzle[r][c] != 0 && board[r][c] != puzzle[r][c]) return false;
            }
        }
        for (int i = 0; i < 9; i++) {
            boolean[] row = new boolean[10];
            boolean[] col = new boolean[10];
            boolean[] box = new boolean[10];
            for (int j = 0; j < 9; j++) {
                int a = board[i][j];
                int b = board[j][i];
                int c = board[(i / 3) * 3 + j / 3][(i % 3) * 3 + j % 3];
                if (a < 1 || a > 9 || b < 1 || b > 9 || c < 1 || c > 9) return false;
                if (row[a] || col[b] || box[c]) return false;
                row[a] = true;
                col[b] = true;
                box[c] = true;
            }
        }
        return true;
    }

    private static boolean fill(int[][] grid) {
        int row = -1, col = -1;
        outer:
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) { row = r; col = c; break outer; }
            }
        }
        if (row == -1) return true;

        List<Integer> numbers = new ArrayList<>();
        for (int n = 1; n <= 9; n++) numbers.add(n);
        Collections.shuffle(numbers, RANDOM);

        for (int n : numbers) {
            if (!isSafe(grid, row, col, n)) continue;
            grid[row][col] = n;
            if (fill(grid)) return true;
            grid[row][col] = 0;
        }
        return false;
    }

    private static boolean isSafe(int[][] grid, int row, int col, int n) {
        for (int i = 0; i < 9; i++) {
            if (grid[row][i] == n || grid[i][col] == n) return false;
        }
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++) {
            for (int c = bc; c < bc + 3; c++) {
                if (grid[r][c] == n) return false;
            }
        }
        return true;
    }

    private static int[][] copy(int[][] source) {
        int[][] result = new int[9][];
        for (int i = 0; i < 9; i++) result[i] = source[i].clone();
        return result;
    }
}
