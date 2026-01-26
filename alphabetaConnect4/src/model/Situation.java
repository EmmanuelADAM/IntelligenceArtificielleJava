package model;

import java.util.ArrayList;

/**
 * Situation class, it represents a node in the state tree.
 *
 * @author emmanuel adam
 * @version May 2013
 */
public class Situation {
    /** Name of the situation */
    private String name;

    /** Number of instances */
    private static int instanceCount = 0;

    /** Instance number */
    private int instanceNumber;

    /** Column number */
    private int columnNumber;

    /** Indicates if the state is in Max mode */
    private boolean max = true;

    /** List of states accessible from the current state */
    private ArrayList<Situation> successors;

    /** Indicates if the state/situation is a leaf of the tree */
    private boolean leaf;

    /**
     * Indicates if the state/situation is a definitive leaf of the tree
     * (i.e., it is impossible to create successors for this situation)
     */
    private boolean closed;

    /** h = heuristic, estimation of the situation's value */
    private int h;

    /** Game grid corresponding to the situation */
    private int[][] gameMatrix;

    /** Default constructor */
    public Situation() {
        instanceNumber = instanceCount++;
        name = "" + instanceNumber;
        h = 0;
        gameMatrix = new int[Connect4.HEIGHT + 1][Connect4.WIDTH + 1];
        successors = new ArrayList<>();
    }

    /**
     * Constructor initializing the heuristic estimate h
     *
     * @param _h estimation of the situation
     */
    public Situation(int _h) {
        this();
        h = _h;
    }

    /**
     * Constructor initializing the heuristic estimate h and the node type
     *
     * @param _h estimation of the situation
     * @param _isMax determines if the value of the situation should be maximized or not
     */
    public Situation(int _h, boolean _isMax) {
        this(_h);
        max = _isMax;
    }

    /**
     * Constructor initializing the heuristic estimate h, the node type, and the node position
     *
     * @param _h estimation of the situation
     * @param _isMax determines if the value of the situation should be maximized or not
     * @param _isLeaf determines if the situation is final in the tree
     */
    public Situation(int _h, boolean _isMax, boolean _isLeaf) {
        this(_h, _isMax);
        leaf = _isLeaf;
    }

    /** Function evaluating the current situation; calculates 'h' */
    void evaluate() {
        int playerCoefficient = 1;
        int machineCoefficient = 2;
        int value = -playerCoefficient * possibleFourInRow(PlayerType.PLAYER);
        value += machineCoefficient * possibleFourInRow(PlayerType.MACHINE);
        value += -playerCoefficient * possibleFourInColumn(PlayerType.PLAYER);
        value += machineCoefficient * possibleFourInColumn(PlayerType.MACHINE);
        value += -playerCoefficient * possibleFourInDiagonal(PlayerType.PLAYER);
        value += machineCoefficient * possibleFourInDiagonal(PlayerType.MACHINE);

        int lineValue = -playerCoefficient * fourAlignedInRow(PlayerType.PLAYER);
        lineValue += machineCoefficient * fourAlignedInRow(PlayerType.MACHINE);
        lineValue += -playerCoefficient * fourAlignedInColumn(PlayerType.PLAYER);
        lineValue += machineCoefficient * fourAlignedInColumn(PlayerType.MACHINE);
        lineValue += -playerCoefficient * fourAlignedInDiagonal(PlayerType.PLAYER);
        lineValue += machineCoefficient * fourAlignedInDiagonal(PlayerType.MACHINE);
        value += lineValue;

        if (lineValue != 0) {
            closed = true;
        }
        h = value;
    }

    /**
     * Function that returns the value of a row<br>
     * If 2 tokens of type tj are aligned and adjacent to an empty cell -> value = 200<br>
     * If 2 tokens of type tj are aligned and adjacent to two empty cells -> value = 400<br>
     * If 1 token of type tj is adjacent to an empty cell -> value = 30<br>
     * If 1 token of type tj is adjacent to two empty cells -> value = 60
     */
    private int possibleFourInRow(PlayerType playerType) {
        int result = 0;
        int playerValue = playerType.getType();
        for (int i = 0; i < Connect4.HEIGHT; i++) {
            if (gameMatrix[i][Connect4.WIDTH] >= 2) {
                result = dangerValue(gameMatrix[i], playerValue);
            }
        }
        return result;
    }

    /** Compute the index of danger in lines and parts of diagonals */
    private int dangerValue(int[] lineDiagonal, int playerValue) {
        int result = 0;
        int danger1, danger2, danger3, danger4;
        int possibleDanger1, possibleDanger2, possibleDanger3, possibleDanger4, possibleDanger5;

        if (playerValue == 1) {
            danger1 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER1_1.getPattern());
            danger2 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER1_2.getPattern());
            danger3 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER1_3.getPattern());
            danger4 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER1_4.getPattern());
        } else {
            danger1 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER2_1.getPattern());
            danger2 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER2_2.getPattern());
            danger3 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER2_3.getPattern());
            danger4 = Situation.findSubArray(lineDiagonal, DangerPattern.DANGER2_4.getPattern());
        }

        if (danger1 != -1) result += 300;
        if (danger2 != -1) result += 300;
        if (danger3 != -1) result += 300;
        if (danger4 != -1) result += 300;

        if (playerValue == 1) {
            possibleDanger1 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER1_1.getPattern());
            possibleDanger2 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER1_2.getPattern());
            possibleDanger3 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER1_3.getPattern());
            possibleDanger4 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER1_4.getPattern());
            possibleDanger5 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER1_5.getPattern());
        } else {
            possibleDanger1 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER2_1.getPattern());
            possibleDanger2 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER2_2.getPattern());
            possibleDanger3 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER2_3.getPattern());
            possibleDanger4 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER2_4.getPattern());
            possibleDanger5 = Situation.findSubArray(lineDiagonal, DangerPattern.POSSIBLE_DANGER2_5.getPattern());
        }

        if (possibleDanger1 != -1) result += 50;
        if (possibleDanger2 != -1) result += 50;
        if (possibleDanger3 != -1) result += 50;
        if (possibleDanger4 != -1) result += 50;
        if (possibleDanger5 != -1) result += 50;

        return result;
    }

    /**
     * Function that returns the value of a column<br>
     * If 2 tokens of type tj are aligned and adjacent to an empty cell -> value = 100<br>
     * If 1 token of type tj is adjacent to an empty cell -> value = 30<br>
     */
    private int possibleFourInColumn(PlayerType playerType) {
        int result = 0;
        int playerValue = playerType.getType();

        for (int j = 0; j < Connect4.WIDTH; j++) {
            int contiguousCount = 0;
            int tokensInColumn = gameMatrix[Connect4.HEIGHT][j];

            if (tokensInColumn >= 4) {
                for (int i = tokensInColumn - 1; i >= 0; i--) {
                    if (gameMatrix[i][j] == playerValue) {
                        contiguousCount++;
                    } else {
                        contiguousCount = 0;
                    }
                }
            }

            if (contiguousCount == 3) result += 300;
            else if (contiguousCount == 2) result += 50;
            else if (contiguousCount == 1) result += 10;
        }
        return result;
    }

    /**
     * Calculates if 4 tokens are aligned in a row
     *
     * @param playerType type of player to check if they have four tokens aligned in a row
     * @return 0 if no alignment, 2000 if four tokens are aligned
     */
    int fourAlignedInRow(PlayerType playerType) {
        int result = 0;
        boolean found = false;
        int playerValue = playerType.getType();

        for (int i = 0; i < Connect4.HEIGHT && !found; i++) {
            int contiguous = 0;
            if (gameMatrix[i][Connect4.WIDTH] >= 3) {
                for (int j = 0; j < Connect4.WIDTH && !found; j++) {
                    if (gameMatrix[i][j] != playerValue) {
                        contiguous = 0;
                    } else {
                        contiguous++;
                    }
                    found = (contiguous == 4);
                }
            }
        }

        if (found) {
            result = 2000;
            leaf = true;
        }
        return result;
    }

    /**
     * Calculates if 4 tokens are aligned in a column
     */
    int fourAlignedInColumn(PlayerType playerType) {
        int result = 0;
        boolean found = false;
        int playerValue = playerType.getType();

        for (int j = 0; j < Connect4.WIDTH && !found; j++) {
            int contiguous = 0;
            int tokensInColumn = gameMatrix[Connect4.HEIGHT][j];
            if (tokensInColumn >= 4) {
                for (int i = tokensInColumn - 1; i >= 0 && !found; i--) {
                    if (gameMatrix[i][j] != playerValue) {
                        contiguous = 0;
                    } else {
                        contiguous++;
                    }
                    found = (contiguous == 4);
                }
            }
        }

        if (found) result = 1000;
        return result;
    }

    /**
     * Calculates if 4 tokens are aligned in a diagonal
     */
    int fourAlignedInDiagonal(PlayerType playerType) {
        int result = 0;
        boolean found = false;
        int playerValue = playerType.getType();

        for (int j = 0; j < Connect4.WIDTH && !found; j++) {
            int tokensInColumn = gameMatrix[Connect4.HEIGHT][j];
            if (tokensInColumn >= 4) {
                for (int i = tokensInColumn - 1; i >= 3 && !found; i--) {
                    found = (gameMatrix[i][j] == playerValue);
                    if (found) {
                        found = fourDiagonalRight(i, j, playerValue);
                        found = found || fourDiagonalLeft(i, j, playerValue);
                    }
                }
            }
        }

        if (found) result = 1000;
        return result;
    }

    /** Returns true if 4 pieces are contiguous on the right diagonal */
    private boolean fourDiagonalRight(int i, int j, int playerValue) {
        boolean diagonal = false;
        if (i - 3 >= 0 && (j + 3) < (Connect4.WIDTH)) {
            diagonal = true;
            for (int k = 1; k <= 3; k++) {
                diagonal = diagonal && (gameMatrix[i - k][j + k] == playerValue);
            }
        }
        return diagonal;
    }

    /** Returns true if 4 pieces are contiguous on the left diagonal */
    private boolean fourDiagonalLeft(int i, int j, int playerValue) {
        boolean diagonal = false;
        if (i - 3 >= 0 && j - 3 >= 0) {
            diagonal = true;
            for (int k = 1; k <= 3; k++) {
                diagonal = diagonal && (gameMatrix[i - k][j - k] == playerValue);
            }
        }
        return diagonal;
    }

    /**
     * Calculates if 4 tokens are possible in a diagonal
     */
    int possibleFourInDiagonal(PlayerType playerType) {
        int result = 0;
        int playerValue = playerType.getType();
        result += possibleFourInLeftDiagonal(playerValue);
        result += possibleFourInRightDiagonal(playerValue);
        return result;
    }

    /**
     * Calculates if 4 tokens are possible in the left diagonal
     */
    int possibleFourInLeftDiagonal(int playerValue) {
        int result = 0;
        int length = 3;
        int end = Connect4.WIDTH + 2;

        for (int j = 3; j < end; j++) {
            int i = (j < Connect4.WIDTH ? Connect4.HEIGHT - 1 : Connect4.HEIGHT - 1 + (Connect4.WIDTH - j));
            length = length + (j < 6 ? 1 : 0);
            length = length - (j > 7 ? 1 : 0);
            int[] diagonalArray = new int[length];
            int column = (j < Connect4.WIDTH ? j : Connect4.WIDTH - 1);
            for (int k = 0; k < length; k++) {
                diagonalArray[k] = gameMatrix[i - k][column - k];
            }
            result += dangerValue(diagonalArray, playerValue);
        }
        return result;
    }

    /**
     * Calculates if 4 tokens are possible in the right diagonal
     */
    int possibleFourInRightDiagonal(int playerValue) {
        int result = 0;
        int length = 3;
        int end = Connect4.WIDTH - 2;

        for (int j = -2; j < end; j++) {
            int i = (j < 0 ? Connect4.HEIGHT - 1 + j : Connect4.HEIGHT - 1);
            length = length + (j <= 0 ? 1 : 0);
            length = length - (j > 1 ? 1 : 0);
            int[] diagonalArray = new int[length];
            int column = Math.max(j, 0);
            for (int k = 0; k < length; k++) {
                diagonalArray[k] = gameMatrix[i - k][column + k];
            }
            result += dangerValue(diagonalArray, playerValue);
        }
        return result;
    }

    /**
     * Function calculating the value of the state
     *
     * @return estimation of the current state's value
     */
    public int getHeuristic() {
        return h;
    }

    void addSuccessor(Situation s) {
        successors.add(s);
        s.name = name + "." + s.name;
    }

    /** @return true if the state is a leaf */
    public boolean isLeaf(PlayerType playerType) {
        return leaf || closed;
    }

    /**
     * @param successors the successors to set
     */
    public void setSuccessors(ArrayList<Situation> successors) {
        this.successors = successors;
    }

    /**
     * @param index index of the successor
     * @return the successor at index
     */
    public Situation getSuccessor(int index) {
        Situation result = null;
        if (successors != null && index < successors.size()) {
            result = successors.get(index);
        }
        return result;
    }

    /**
     * @return the successors
     */
    public ArrayList<Situation> getSuccesseurs() {
        return successors;
    }

    /**
     * @return the heuristic value h
     */
    public int getH() {
        return h;
    }

    /**
     * @param h the heuristic value to set
     */
    public void setH(int h) {
        this.h = h;
    }

    /**
     * @return the game matrix
     */
    public int[][] getGameMatrix() {
        return gameMatrix;
    }

    /**
     * @param gameMatrix the game matrix to set
     */
    public void setGameMatrix(int[][] gameMatrix) {
        this.gameMatrix = gameMatrix;
    }

    /**
     * @return true if the state is in Max mode
     */
    public boolean isMax() {
        return max;
    }

    /**
     * @param isMax whether the state is in Max mode
     */
    public void setMax(boolean isMax) {
        this.max = isMax;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(" S" + name + ", h=" + h + ", is max = " + max + "---");
        if (!successors.isEmpty()) {
            result.append("\n::::: I have ").append(successors.size()).append(" children :::::");
            for (Situation s : successors) {
                result.append(s.name).append("(").append(s.h).append(") ; ");
            }
        }
        result.append("\n ");
        return result.toString();
    }

    /** Displays the matrix associated with the situation on the console */
    @SuppressWarnings("unused")
    private void displayMatrix() {
        StringBuilder result = new StringBuilder();
        for (int i = Connect4.HEIGHT; i >= 0; i--) {
            result.append("\n|");
            for (int j = 0; j < Connect4.WIDTH; j++) {
                result.append(gameMatrix[i][j]).append("|");
            }
        }
        result.append("\n------------\n");
        System.out.println(result);
    }

    /**
     * Checks if a sub-array is inside a larger array
     *
     * @param largeArray large array to check
     * @param subArray small array to find in largeArray
     * @return the position of the first element of subArray in largeArray, -1 if it doesn't exist
     */
    static public int findSubArray(int[] largeArray, int[] subArray) {
        int subArrayLength = subArray.length;
        if (subArrayLength == 0) {
            return -1;
        }
        int limit = largeArray.length - subArrayLength;
        int i ;
        boolean subArrayFound = false;
        for (i = 0; i <= limit && !subArrayFound; i++) {
            subArrayFound = true;
            for (int j = 0; j < subArrayLength; j++) {
                if (subArray[j] != largeArray[i + j]) {
                    subArrayFound = false;
                    break;
                }
            }
        }
        if (!subArrayFound) {
            i = 0;
        }
        return i - 1;
    }

    /**
     * @return true if the state is a leaf
     */
    public boolean isLeaf() {
        return leaf || closed;
    }

    /**
     * @param leaf whether the state is a leaf
     */
    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if the state is closed
     */
    public boolean isClose() {
        return closed;
    }

    /**
     * @param closed whether the state is closed
     */
    public void setClose(boolean closed) {
        this.closed = closed;
    }

    /**
     * @return the column number
     */
    public int getColumnNumber() {
        return columnNumber;
    }

    /**
     * @param columnNumber the column number to set
     */
    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    /**
     * Main method to test the class
     */
    public static void main(String... args) {
        int[][] matrix1 = {
                {-1, 2, 1, 2, 1, 2, 1, 5},
                {0, 1, 2, 2, 2, 1, -1, 6},
                {0, -1, 2, 2, 1, 2, 0, 4},
                {0, 0, 1, 1, 2, 1, 0, 3},
                {0, 0, -1, -1, 1, 2, 0, 2},
                {0, 0, 0, 0, 2, -1, 0, 1},
                {0, 2, 3, 4, 6, 5, 0, 0}
        };
        Situation s1 = new Situation();
        s1.setGameMatrix(matrix1);
        System.out.println("fourAlignedInDiagonal(MACHINE)=" + s1.fourAlignedInDiagonal(PlayerType.MACHINE));
        System.out.println("fourAlignedInDiagonal(PLAYER)=" + s1.fourAlignedInDiagonal(PlayerType.PLAYER));
        System.out.println("possibleFourInDiagonal(MACHINE)=" + s1.possibleFourInDiagonal(PlayerType.MACHINE));
        System.out.println("possibleFourInDiagonal(PLAYER)=" + s1.possibleFourInDiagonal(PlayerType.PLAYER));
    }
}
