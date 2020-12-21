package edu.common.engine;

import edu.server.Connection;

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
    public Player[] getPlayerByConnection(Connection con){
        Player[] roomPlayers = new Player[2];
        if(getGuest().getConnection() == con){
            roomPlayers[0] = getGuest();
            roomPlayers[1] = getHost();
        }else if(getHost().getConnection() == con){
            roomPlayers[0] = getHost();
            roomPlayers[1] = getGuest();
        }else{
            return null;
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

}
