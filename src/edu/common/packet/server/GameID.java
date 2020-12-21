package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class GameID extends Packet {
    @SerializedName("roomID")
    private final String roomID;

    public GameID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomID() {
        return roomID;
    }
}
