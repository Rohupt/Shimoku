package edu.common.packet;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class RuleSet extends Packet {
    @SerializedName("size")
    private int size;
    @SerializedName("gameTime")
    private long gameTime;
    @SerializedName("moveTime")
    private long moveTime;

    public RuleSet(int size, long gameTime, long moveTime) {
        this.setId("02");
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
}
