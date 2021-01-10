package edu.common.packet;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Packet implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private final String id;
    
    public Packet(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
