
package edu.client;

import edu.common.engine.Room;
import javafx.application.Application;

public class ClientMain {
    private Room room;
    private Client client;
    
	public static void main(String[] args) {
        Client client = new Client("localhost", 27013);
        if (client.connect()) {
            //Application.launch(App.class, args);
            client.run();
        }
        //Application.launch(App.class, args);
	}
}
