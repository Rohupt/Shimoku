package edu.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private int port;
    private ServerSocket serverSocket;
    private boolean running = false;
    private int id = 0;

    public Server(int port) {
        this.port = port;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(this).start();
    }


    private void initSocket(Socket socket) {
        Connection connection = new Connection(socket, id);
        new Thread(connection).start();
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        System.out.printf("\nClient connected: %s:%d\n", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
        id++;
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Server started on port: " + port);

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                initSocket(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        shutdown();
    }

    public void shutdown() {
        running = false;

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
