package edu.server;

import edu.common.engine.Room;
import java.util.LinkedList;

public class RoomList {
    private static final LinkedList<Room> roomList = new LinkedList<>();

    public static LinkedList<Room> getRoomList() {
        return roomList;
    }
}
