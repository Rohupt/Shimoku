
package edu.common.packet.server;

import com.google.gson.annotations.SerializedName;
import edu.common.packet.Packet;

public class JoinFailed extends Packet {
    @SerializedName("found")
    private boolean found;

    public JoinFailed(boolean found) {
        super("jf");
        this.found = found;
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }
}
