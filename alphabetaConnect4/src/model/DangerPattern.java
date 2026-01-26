package model;

/**
 * Enumeration representing potential danger patterns in the game.
 */
public enum DangerPattern {
    DANGER1_1(new int[]{-1, 1, 1, 1}),
    DANGER1_2(new int[]{1, 1, 1, -1}),
    DANGER1_3(new int[]{1, -1, 1, 1}),
    DANGER1_4(new int[]{1, 1, -1, 1}),

    DANGER2_1(new int[]{-1, 2, 2, 2}),
    DANGER2_2(new int[]{2, 2, 2, -1}),
    DANGER2_3(new int[]{2, -1, 2, 2}),
    DANGER2_4(new int[]{2, 2, -1, 2}),

    POSSIBLE_DANGER1_1(new int[]{-1, -1, 1, 1}),
    POSSIBLE_DANGER1_5(new int[]{1, -1, -1, 1}),
    POSSIBLE_DANGER1_2(new int[]{1, 1, -1, -1}),
    POSSIBLE_DANGER1_3(new int[]{1, -1, 1, -1}),
    POSSIBLE_DANGER1_4(new int[]{-1, 1, -1, 1}),

    POSSIBLE_DANGER2_1(new int[]{-1, -1, 2, 2}),
    POSSIBLE_DANGER2_5(new int[]{2, -1, -1, 2}),
    POSSIBLE_DANGER2_2(new int[]{2, 2, -1, -1}),
    POSSIBLE_DANGER2_3(new int[]{2, -1, 2, -1}),
    POSSIBLE_DANGER2_4(new int[]{-1, 2, -1, 2});

    int[] pattern;

    /**
     * Constructor for DangerPattern.
     * @param _pattern the pattern associated with the danger
     */
    DangerPattern(int[] _pattern) {
        pattern = _pattern;
    }

    /**
     * @return the pattern
     */
    public int[] getPattern() {
        return pattern;
    }
}
