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
    
    public String getPacketName() {
        String s = this.getClass().getSimpleName();
        StringBuilder sb = new StringBuilder(s);
        int offset = 0;
        for (int i = 1; i < s.length(); i++) {
            if (Character.isUpperCase(s.charAt(i)) && !(Character.isUpperCase(s.charAt(i - 1)) && (i == s.length() - 1 ? Character.isUpperCase(s.charAt(i - 1)) : Character.isUpperCase(s.charAt(i + 1)))))
                sb.insert(i + offset++, '_');
        }
        return sb.toString().toUpperCase();
    }
}
