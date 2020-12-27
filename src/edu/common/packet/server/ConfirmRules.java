package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class ConfirmRules extends Packet {
    @SerializedName("succeeded")
    private boolean successful;

    public ConfirmRules(boolean status) {
        this.setId("cr");
        this.successful = status;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
