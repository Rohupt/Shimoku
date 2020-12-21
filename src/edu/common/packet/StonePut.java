package edu.common.packet;

import com.google.gson.annotations.SerializedName;

public class StonePut extends Packet {
    @SerializedName("x")
    private int x;
    @SerializedName("x")
    private int y;

    public StonePut(int x, int y) {
        this.x = x;
        this.y = y;
        this.setId("09");
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
}
