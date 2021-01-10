package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class GameStart extends Packet {
    @SerializedName("hostMoveFirst")
    private final boolean hostMoveFirst;
    
    public GameStart(boolean hostMoveFirst) {
        super("gs");
        this.hostMoveFirst = hostMoveFirst;
    }

    public boolean isHostMoveFirst() {
        return hostMoveFirst;
    }
}
