package edu.common.packet.client;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class CreateGame extends Packet {
    @SerializedName("username")
    private String username;

    public CreateGame(String username) {
        this.username = username;
        this.setId(new String("00"));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
