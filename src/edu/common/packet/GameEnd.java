package edu.common.packet;

import com.google.gson.annotations.SerializedName;

public class GameEnd extends Packet{
    public enum EndingType { HOST_WON, GUEST_WON, DRAW };
    public enum ReasonType {
        BY_WINNING_MOVE,
        BY_OPPONENT_SURRENDER,
        BY_OPPONENT_LEFT,
        BY_AGREEMENT,
        BY_BOARD_FULL,
        BY_BOTH_DISCONNECTION,
        BY_TIMEOUT
    }
    @SerializedName("endingType")
    private EndingType endingType;
    @SerializedName("reason")
    private ReasonType reason;

    public GameEnd() {
        this.setId("ge");
    }

    public GameEnd(EndingType endingType, ReasonType reason) {
        this();
        this.endingType = endingType;
        this.reason = reason;
    }

    public EndingType getEndingType() {
        return endingType;
    }

    public void setEndingType(EndingType endingType) {
        this.endingType = endingType;
    }

    public ReasonType getReason() {
        return reason;
    }

    public void setReason(ReasonType reason) {
        this.reason = reason;
    }
}
