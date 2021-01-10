package edu.server;

import com.google.gson.Gson;
import edu.common.engine.Room;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

public class Connection implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private EventListener listener;
    private boolean running = false;

    public int id;
    public String playerName;
    public Room room = null;

    public Connection(Socket socket, int id) {
        this.socket = socket;
        this.id = id;

        try {
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(socket.getInputStream());
            listener = new EventListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

        while (running) {
            try {
                String data = in.readUTF();
//              Execute object received
                listener.received_data(data, this);
            } catch (IOException e) {
                System.out.printf("Client disconnected: %s:%d\n", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
                break;
            }
        }
        if (!socket.isClosed()) {
            close();

        }
    }

    public void close() {
        try {
//          Missing some code
            running = false;
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object packet) {
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        try {
//            out.reset();
            Gson gson = new Gson();
            //Convert Object to json and to string
            String data = gson.toJson(packet);
            System.out.printf("Sent a packet: %s:%s\n\t%s\n", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort(), data);
            out.writeUTF(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public EventListener getListener() {
        return listener;
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
    
    public String ipToCode() {
        InetSocketAddress isa = (InetSocketAddress) this.getSocket().getRemoteSocketAddress();
        if (isa == null) return null;
        byte[] ia = isa.getAddress().getAddress();
        byte[] ial4 = Arrays.copyOfRange(ia, ia.length - 4, ia.length);
        int port = isa.getPort();
        int code = ial4[0] * (int) Math.pow(256, 3) + ial4[1] * (int) Math.pow(256, 2)
                + ial4[3] * (int) Math.pow(256, 1) + ial4[3] * (int) Math.pow(256, 0);
        String set = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        int r;
        while (code != 0) {
            r =(int) (code % set.length());
            sb.append(set.charAt(r >= 0 ? r : set.length() + r));
            code /= set.length();
        }
        sb.append(set.charAt(port % set.length())).append(set.charAt(port / set.length() % set.length()));
        return sb.toString();
    }
}
