package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class GuestFound extends Packet {
    @SerializedName("username")
    private String username;

    public GuestFound(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
