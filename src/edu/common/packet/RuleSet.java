package edu.common.packet;

import com.google.gson.annotations.SerializedName;
import edu.common.engine.GameSettings;

public class RuleSet extends Packet {
    @SerializedName("size")
    private int size;
    @SerializedName("gameTime")
    private long gameTime;
    @SerializedName("moveTime")
    private long moveTime;

    public RuleSet(int size, long gameTime, long moveTime) {
        super("rs");
        this.size = size;
        this.gameTime = gameTime;
        this.moveTime = moveTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public long getMoveTime() {
        return moveTime;
    }

    public void setMoveTime(long moveTime) {
        this.moveTime = moveTime;
    }
    
    public GameSettings toGameSettings() {
        GameSettings gs = new GameSettings();
        gs.setSize(this.getSize());
        gs.setGameTimingEnabled(this.getGameTime() != -1);
        gs.setMoveTimingEnabled(this.getMoveTime() != -1);
        if (gs.gameTimingEnabled())
            gs.setGameTimeMillis(this.getGameTime());
        if (gs.moveTimingEnabled())
            gs.setMoveTimeMillis(this.getMoveTime());
        return gs;
    }
}
