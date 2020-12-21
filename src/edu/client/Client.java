package edu.client;

import com.google.gson.Gson;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import javafx.application.Application;

public class Client implements Runnable{
    private final String host;
    private final int port;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private boolean running = false;
    private EventListener listener;

    public String playerName;

    private boolean isServerDied = false;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.listener = new EventListener(this);
        
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            listener = new EventListener(this);
            new Thread(this).start();
            System.out.println("Connected to server.");
            Application.launch(App.class);
            
            return true;
        } catch (ConnectException e) {
            System.out.println("Unable to connect to the server");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            running = false;
            if (socket != null) {
                if (!socket.isClosed() && !isServerDied) {
//                    RemoveConnectionPacket removeConnectionPacket = new RemoveConnectionPacket(this.id, this.playerName);
//                    sendObject(removeConnectionPacket);
                }

                if (!socket.isClosed()) {
                    in.close();
                    out.close();
                    socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object packet) {
        try {
            Gson gson = new Gson();
            String data = gson.toJson(packet);
            System.out.printf("Sent a packet: %s\n", data);
            out.writeUTF(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            running = true;
            while (running) {
                try {
                    String data = in.readUTF();
                    listener.received_data(data);
                } catch (SocketException e) {
                    if (e.getMessage().equals("Connection reset"))
                        System.err.println("Server collapsed.");
                    close();
                } catch (EOFException e) {
                    e.printStackTrace();
                    isServerDied = true;
                    close();
                    System.out.println("Disconnected from server!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EventListener getListener() {
        return listener;
    }
}
