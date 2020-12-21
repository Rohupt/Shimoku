
package edu.server;

public class ServerMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server server = new Server(27013);
        server.start();
    }

}
