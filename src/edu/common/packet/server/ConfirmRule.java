package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class ConfirmRule extends Packet {
    @SerializedName("status")
    private boolean status;

    public ConfirmRule(boolean status) {
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
