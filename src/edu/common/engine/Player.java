package edu.common.engine;

import edu.server.Connection;
import edu.common.engine.GameState;
import edu.common.packet.StonePut;

public class Player {

    private String username;
    private Move move;
    private StonePut spPacket;
    private Connection connection;

    public Player(String username, Connection connection) {
        this.username = username;
        this.connection = connection;
    }

    public void setMove(Move move) {
        this.move = move;
    }

    public Move getMove(GameState state){
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
