package edu.common.engine;

import edu.server.Connection;
import edu.common.engine.GameState;
import edu.common.packet.StonePut;

public class Player {

    private String username;
    private Move move;
    private StonePut spPacket;
    private Connection connection;

    /**
     * Create a new player.
     * @param username Game information
     */
    public Player(String username, Connection connection) {
        this.username = username;
        this.connection = connection;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    /**
     * Request a move from this player.
     * @param state Current game state
     * @return Move the player wants to make
     */
    public Move getMove(GameState state){
        // Suspend until the user clicks a valid move (handled by the game)
        return move;
    }

    public StonePut getSpPacket() {
        try {
            synchronized(this) {
                this.wait();
            }
        } catch(InterruptedException e) {
            return null;
        }
        return spPacket;
    }

    public void setSpPacket(StonePut spPacket) {
        this.spPacket = spPacket;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
