package com.sudokuarena.model;

/**
 * The four difficulty levels. The JSP loops over Difficulty.values(),
 * so the labels and descriptions live here, not in the HTML.
 */
public enum Difficulty {
    EASY("Easy", 35, "A gentle warm-up", "Perfect for beginners.", "5–10 min", "Low"),
    MEDIUM("Medium", 45, "A balanced brain workout", "A good balance of challenge and fun.", "10–20 min", "Medium"),
    HARD("Hard", 52, "For focused thinkers", "For experienced players.", "20–40 min", "High"),
    EXPERT("Expert", 58, "A true test of logic", "The ultimate test of your skills.", "40+ min", "Very high");

    private final String label;
    private final int removals;
    private final String tagline;
    private final String description;
    private final String duration;
    private final String intensity;

    Difficulty(String label, int removals, String tagline,
               String description, String duration, String intensity) {
        this.label = label;
        this.removals = removals;
        this.tagline = tagline;
        this.description = description;
        this.duration = duration;
        this.intensity = intensity;
    }

    public String getLabel()       { return label; }
    public int getRemovals()       { return removals; }
    public String getTagline()     { return tagline; }
    public String getDescription() { return description; }
    public String getDuration()    { return duration; }
    public String getIntensity()   { return intensity; }

    /** Value stored in the Oracle CHECK constraint: EASY, MEDIUM, HARD, EXPERT. */
    public String getDbValue()     { return name(); }

    /** Accepts "Easy", "easy", "EASY"... Returns null for anything else. */
    public static Difficulty parse(String text) {
        if (text == null) return null;
        for (Difficulty d : values()) {
            if (d.name().equalsIgnoreCase(text.trim())) return d;
        }
        return null;
    }

    /** Same as parse, but falls back to EASY (used when reading from the database). */
    public static Difficulty fromDb(String text) {
        Difficulty d = parse(text);
        return d != null ? d : EASY;
    }
}
