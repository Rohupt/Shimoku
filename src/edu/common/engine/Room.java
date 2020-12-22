package edu.common.engine;

import edu.server.Connection;
import java.net.InetSocketAddress;
import java.util.Arrays;

import java.util.Objects;

public class Room {
    private Game game;
    private String roomID;
    private Player host;
    private Player guest;
    private GameSettings settings;

    /**
     * @param con
     * @return players[]
     * players[0] = player with this con
     * players[1] = player remainder
     */
    public Player[] getSortedPlayers(Connection con){
        Player[] roomPlayers = new Player[2];
        if (ipToHex(host.getConnection()).equals(ipToHex(con))) {
            roomPlayers[0] = host;
            roomPlayers[1] = guest;
        } else {
            roomPlayers[0] = guest;
            roomPlayers[1] = host;
        }
        return roomPlayers;
    }

    public boolean checkHost(Connection con){
        return getHost().getConnection() == con;
    }

    public Player getHost() {
        return host;
    }

    public void setHost(Player host) {
        this.host = host;
    }

    public Player getGuest() {
        return guest;
    }

    public void setGuest(Player guest) {
        this.guest = guest;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    public Game getGame() {
        return game;
    }
    
    public void removeGame() {
        this.game = null;
    }
    
    public void newGame(GameSettings settings) {
        Game newGame = new Game(settings);
        newGame.setPlayer1(host);
        newGame.setPlayer2(guest);
        this.game = newGame;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.roomID);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Room) {
            Room room = (Room) obj;
            return (room.roomID == null ? this.roomID == null : room.roomID.equals(this.roomID));
        }
        return false;
    }

    public static String ipToHex(Connection con) {
        InetSocketAddress isa = (InetSocketAddress) con.getSocket().getRemoteSocketAddress();
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
            sb.append(set.charAt(r));
            code = code / set.length();
        }
        sb.append(set.charAt(port % set.length())).append(set.charAt(port / set.length() % set.length()));
        return sb.toString();
    }
}
