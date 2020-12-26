package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;
import edu.common.packet.RuleSet;

public class GameID extends Packet {
    @SerializedName("roomID")
    private final String roomID;
    @SerializedName("ruleSet")
    private final RuleSet ruleSet;

    public GameID(String roomID, RuleSet ruleSet) {
        this.setId("id");
        this.roomID = roomID;
        this.ruleSet = ruleSet;
    }

    public String getRoomID() {
        return roomID;
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }
}
