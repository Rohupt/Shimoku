package edu.common.packet.client;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class JoinGame extends Packet {
    @SerializedName("roomID")
    private String roomID;
    @SerializedName("username")
    private String username;

    public JoinGame(String roomID, String username) {
        this.setId("jg");
        this.roomID = roomID;
        this.username = username;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
