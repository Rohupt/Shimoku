package edu.client;

import com.google.gson.Gson;
import edu.common.packet.Packet;

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

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            listener = new EventListener();
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

    public void sendMessage(Packet packet) {
        try {
            Gson gson = new Gson();
            String data = gson.toJson(packet);
            System.out.printf("\n%s sent:\n\t%s\n", packet.getPacketName(), data);
            out.writeUTF(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public EventListener getListener() {
        return listener;
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
                    close();
                    System.out.println("Disconnected from server!");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
