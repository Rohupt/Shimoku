package edu.common.engine;
/**
 * Created by Doston Hamrakulov
 */


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * State for a Gomoku game.
 */
public class GameState {

    private final int size;
    private final int[][] board;
    private final LinkedList<Move> moves;
    private int currentIndex;

    /**
     * Create a new game state.
     * @param size Board size
     */
    public GameState(int size) {
        this.size = size;
        this.board = new int[size][size];
        this.moves = new LinkedList<>();
        this.currentIndex = 1;
    }

    /**
     * Return the terminal status of the game.
     * @return 0 if not terminal, the player index of the winning player, or
     * 3 if the game ended in a draw.
     */
    protected int terminal() {
        if(isWinner(1)) return 1;
        if(isWinner(2)) return 2;
        if(moves.size() == size * size) return 3;
        return 0;
    }

    /**
     * Get the current player index for this state
     * @return Current player # who has to make a move
     */
    protected int getCurrentIndex() {
        return this.currentIndex;
    }

    /**
     * Get an ordered list of moves that were made on this state.
     * @return ArrayList of moves, ordered from first move to last move made
     */
    public List<Move> getMoves() {
        return new ArrayList(moves);
    }

    /**
     * Return the last move made on this state.
     * @return Previous move that was made
     */
    public Move getLastMove() {
        return !moves.isEmpty() ? moves.peek() : null;
    }

    /**
     * Make a move on this state.
     * @param move Move to make
     */
    protected void makeMove(Move move) {
        this.moves.addFirst(move);
        this.board[move.row][move.col] = currentIndex;
        this.currentIndex = currentIndex == 1 ? 2 : 1;
    }

    /**
     * Determine if the specified player has won the game
     * @param playerIndex Player index (1 or 2)
     * @return True if the index has won
     */
    private boolean isWinner(int playerIndex) {
        if(moves.size() < 5) return false;
        Move lastMove = getLastMove();
        int row = lastMove.row;
        int col = lastMove.col;
        if(board[row][col] == playerIndex) {
            return checkDirection(row, col, -1, 1)
                || checkDirection(row, col, 1, 1)
                || checkDirection(row, col, 1, 0)
                || checkDirection(row, col, 0, 1)
                || checkSurround(row, col);
        }
        return false;
    }

    /**
     * Helper method to check if an index lies within the bounds of the board.
     * @param index Value to check
     * @return True if this value lies between the bounds of the board (0 to
     * size - 1)
     */
    private boolean inBounds(int index) {
        return index >= 0 && index < size;
    }

    private boolean checkDirection(int row, int col, int rowVector, int colVector) {
        int count = 0;
        int index = board[row][col];
        int opIndex = 3 - index;
        for (int i = 1; i < size; i++) {
            if (inBounds(row + rowVector * i) && inBounds(col + colVector * i)) {
                if (board[row + rowVector * i][col + colVector * i] == index)
                    count++;
                else if (board[row + rowVector * i][col + colVector * i] == opIndex)
                    return false;
            } else break;
        }
        for (int i = 1; i < size; i++) {
            if (inBounds(row + rowVector * -i) && inBounds(col + colVector * -i)) {
                if (board[row + rowVector * -i][col + colVector * -i] == index)
                    count++;
                else if (board[row + rowVector * -i][col + colVector * -i] == opIndex)
                    return false;
            } else break;
        }
        return count >= 3;
    }
    
    private boolean checkSurround(int row, int col) {
        int index = board[row][col];
        int opIndex = 3 - index;
        if (inBounds(row - 2) && inBounds(col - 1) && inBounds(col + 1)) {
            if (    (board[row - 1][col] == opIndex)
                &&  (board[row - 2][col] == index)
                &&  (board[row - 1][col - 1] == index)
                &&  (board[row - 1][col + 1] == index))
                    return true;
        }
        if (inBounds(row + 2) && inBounds(col - 1) && inBounds(col + 1)) {
            if (    (board[row + 1][col] == opIndex)
                &&  (board[row + 2][col] == index)
                &&  (board[row + 1][col - 1] == index)
                &&  (board[row + 1][col + 1] == index))
                    return true;
        }
        if (inBounds(col - 2) && inBounds(row - 1) && inBounds(row + 1)) {
            if (    (board[row][col - 1] == opIndex)
                &&  (board[row][col - 2] == index)
                &&  (board[row - 1][col - 1] == index)
                &&  (board[row + 1][col - 1] == index))
                    return true;
        }
        if (inBounds(col + 2) && inBounds(row - 1) && inBounds(row + 1)) {
            if (    (board[row][col + 1] == opIndex)
                &&  (board[row][col + 2] == index)
                &&  (board[row - 1][col + 1] == index)
                &&  (board[row + 1][col + 1] == index))
                    return true;
        }
        return false;
    }

    public int getSize() {
        return size;
    }
}
