package edu.common.packet;

import com.google.gson.annotations.SerializedName;

public class StonePut extends Packet {
    @SerializedName("x")
    private int x;
    @SerializedName("y")
    private int y;
    @SerializedName("time")
    private long time;

    public StonePut(int x, int y) {
        super("sp");
        this.x = x;
        this.y = y;
        this.time = 0;
    }

    public StonePut(int x, int y, long time) {
        this(x, y);
        this.time = time;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    public boolean timeOut() {
        return time == -1;
    }
}
