package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;
import edu.common.packet.RuleSet;

public class GameInfo extends Packet {
    @SerializedName("ruleSet")
    private RuleSet ruleSet;
    @SerializedName("hostUsername")
    private String hostUsername;

    public GameInfo(RuleSet ruleSet, String hostUsername) {
        this.setId("gi");
        this.ruleSet = ruleSet;
        this.hostUsername = hostUsername;
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public void setHostUsername(String hostUsername) {
        this.hostUsername = hostUsername;
    }
}
