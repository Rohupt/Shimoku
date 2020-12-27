
package edu.server;

public class ServerMain {

    public static void main(String[] args) {
        Server server = new Server(27013);
        server.start();
    }

}
