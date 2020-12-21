package edu.common.engine;

/**
 * Represents settings for a Gomoku game.
 */
public class GameSettings {

    private int size;
    private boolean gameTimingEnabled;
    private boolean moveTimingEnabled;
    private long gameTimeMillis;
    private long moveTimeMillis;

    /**
     * Create a new GameSettings instance with default values.
     */
    public GameSettings() {
        this.gameTimingEnabled = true;
        this.moveTimingEnabled = false;
        this.gameTimeMillis = 1200000;
        this.moveTimeMillis = 5000;
        this.size = 15;
    }

    /**
     * Register a listener to receive updates when settings change.
     * @param listener Listener to register
     */
//    public void addListener(SettingsListener listener) {
//        this.listeners.add(listener);
//    }

    /**
     * Get the board size for this game.
     * @return Board size value (e.g. 15 for 15x15)
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Set the board size for this game.
     * @param size New board size value (e.g. 15 for 15x15)
     */
    public void setSize(int size) {
        this.size = size;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Check if game timing is enabled for this game.
     */
    public boolean gameTimingEnabled() {
        return this.gameTimingEnabled;
    }

    /**
     * Check if move timing is enabled for this game.
     */
    public boolean moveTimingEnabled() {
        return this.moveTimingEnabled;
    }

    /**
     * Enable or disable game timing.
     */
    public void setGameTimingEnabled(boolean enabled) {
        this.gameTimingEnabled = enabled;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Enable or disable move timing.
     */
    public void setMoveTimingEnabled(boolean enabled) {
        this.moveTimingEnabled = enabled;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Get the game timeout value. The game ends if the player exceeds this
     * value and game timing is enabled.
     * @return Game timeout in milliseconds
     */
    public long getGameTimeMillis() {
        return gameTimingEnabled ? this.gameTimeMillis : 0;
    }

    /**
     * Get the move timeout value. The game ends if the player exceeds this
     * value for a single move and move timing is enabled.
     * @return Move timeout in milliseconds
     */
    public long getMoveTimeMillis() {
        return moveTimingEnabled ? this.moveTimeMillis : 0;
    }

    /**
     * Set the game timeout value.
     * @param millis Game timeout in milliseconds
     */
    public void setGameTimeMillis(long millis) {
        this.gameTimeMillis = millis;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    /**
     * Set the move timeout value.
     * @param millis Move timeout in milliseconds
     */
    public void setMoveTimeMillis(long millis) {
        this.moveTimeMillis = millis;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

}
