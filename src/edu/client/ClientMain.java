
package edu.client;

import edu.common.engine.Room;

public class ClientMain {
    private static Room room;
    private static Client client;

    public static Client getClient() {
        return client;
    }

    public static Room getRoom() {
        return room;
    }

    public static void setRoom(Room room) {
        ClientMain.room = room;
    }
    
    public static void main(String[] args) {
        client = new Client(args[0], Integer.parseInt(args[1]));
        if (client.connect()) {
            client.run();
        }
	}
}
