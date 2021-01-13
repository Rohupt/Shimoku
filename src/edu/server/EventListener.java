package edu.server;

import edu.common.packet.server.GameEnd;
import edu.common.packet.RuleSet;
import com.google.gson.Gson;
import edu.common.packet.*;
import edu.common.packet.client.*;
import edu.common.packet.server.*;
import edu.common.engine.*;
import java.net.InetSocketAddress;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EventListener {

    public void received_data(String p, Connection con) {
        JSONParser parser = new JSONParser();
        JSONObject packetJson = null;
        try {
            packetJson = (JSONObject) parser.parse(p);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        InetSocketAddress remoteAddress = (InetSocketAddress) con.getSocket().getRemoteSocketAddress();
        String packetID = (String) packetJson.get("id");
        
        switch (packetID){
            case "cg":
                //Create Game
                handleCreateGame(logPacket(con, p, CreateGame.class), con);
                break;
            case "rs":
                //RuleSet
                handleRuleSet(logPacket(con, p, RuleSet.class), con);
                break;
            case "jg":
                //Join Game
                handleJoinGame(logPacket(con, p, JoinGame.class), con);
                break;
            case "sr":
                //Start Request
                logPacket(con, p, StartRequest.class);
                handleStartRequest(con);
                break;
            case "sp":
                //Stone Put
                handleStonePut(logPacket(con, p, StonePut.class), con);
                break;
            case "su":
                //Surrender
                logPacket(con, p, Surrender.class);
                handleSurPacket(con);
                break;
            case "lg":
                //Leave Game
                logPacket(con, p, LeaveGame.class);
                handleLeaveGame(con);
                break;
            case "od":
                //Offer Draw
                logPacket(con, p, OfferDraw.class);
                handleDrawOffer(con);
                break;
            case "da":
                //Draw Agree
                logPacket(con, p, DrawAgree.class);
                handleDrawAgree(con);
                break;
            case "ts":
                //Turn start
                logPacket(con, p, TurnStart.class);
                handleTurnStart(con);
                break;
            case "rb":
                //Reset board
                handleResetBoard(logPacket(con, p, ResetBoard.class), con);
            default:
                break;
        }
    }
    
    public <T extends Packet> T logPacket(Connection con, String p, Class<T> type) {
        InetSocketAddress remoteAddress = (InetSocketAddress) con.getSocket().getRemoteSocketAddress();
        T pkt = new Gson().fromJson(p, type);
        System.out.printf("\n%s from %s:%s\n\t%s\n", pkt.getPacketName(), remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort(), p);
        return pkt;
    }

    public void handleCreateGame(CreateGame cgPacket,Connection con){
        Room room = new Room();
        room.setSettings(new GameSettings());
        room.setHost(new Player(cgPacket.getUsername(),con));
        room.getHost().getConnection().setRoom(room);
        con.setRoom(room);

        // Create new ID for this room
        room.setRoomID(con.ipToCode());

        // Add room to the end of room list
        RoomList.getRoomList().addLast(room);
        RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
        GameID gameID = new GameID(room.getRoomID(), ruleSet);
        con.sendMessage(gameID);
    }

    public void handleRuleSet(RuleSet rsPacket, Connection con){
        //Only host client can set rule
        Room room = con.getRoom();
        GameSettings temp = new GameSettings(room.getSettings());
        boolean succeeded = false;
        try {
            if (!rsPacket.toGameSettings().equals(room.getSettings())) {
                room.getSettings().setSize(rsPacket.getSize());

                if (rsPacket.getGameTime() == -1) {
                    room.getSettings().setGameTimingEnabled(false);
                } else {
                    room.getSettings().setGameTimingEnabled(true);
                    room.getSettings().setGameTimeMillis(rsPacket.getGameTime());
                }

                if (rsPacket.getMoveTime() == -1) {
                    room.getSettings().setMoveTimingEnabled(false);
                } else {
                    room.getSettings().setMoveTimingEnabled(true);
                    room.getSettings().setMoveTimeMillis(rsPacket.getMoveTime());
                }
            }
            succeeded = true;
        } catch (Exception e) {
            System.out.println("Rules changes failed");
            room.setSettings(temp);
        }
        con.sendMessage(new ConfirmRules(succeeded));

        if (room.getGuest() != null && succeeded) {
            // end RuleSet packet to guest client
            room.getGuest().getConnection().sendMessage(rsPacket);
        }
    }

    public void handleJoinGame(JoinGame jgPacket, Connection con){
        Room room = findRoom(jgPacket.getRoomID());
        if(room != null)
            if (room.getGuest() == null) {
                // Add guest player to room, add connection room -> connection
                room.setGuest(new Player(jgPacket.getUsername(),con));
                room.getGuest().getConnection().setRoom(room);
                con.setRoom(room);

                // Send guest found object to host
                GuestFound guestFound = new GuestFound(jgPacket.getUsername());
                room.getHost().getConnection().sendMessage(guestFound);

                // Send room info to guest
                RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                    room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                    room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
                con.sendMessage(new GameInfo(ruleSet,room.getHost().getUsername()));
                return;
            }
        JoinFailed jfPacket = new JoinFailed(room != null);
        con.sendMessage(jfPacket);
    }

    public void handleStartRequest(Connection con){
        Room room = con.getRoom(); // Get room from connection
        room.newGame(room.getSettings());
        if (room.getGame().isHostMoveFirst()) {
            room.getGame().setPlayer1(room.getHost());
            room.getGame().setPlayer2(room.getGuest());
        } else {
            room.getGame().setPlayer1(room.getGuest());
            room.getGame().setPlayer2(room.getHost());
        }
        room.getGame().start();
    }

    public void handleStonePut(StonePut spPacket,Connection con){
        Game game = con.getRoom().getGame();
        game.setUserSpPacket(spPacket);
    }

    public void handleSurPacket(Connection con){
        // Send EndGame Packet
        Room room = con.getRoom();
        room.getGame().stop();
        room.removeGame();

        GameEnd gameEnd = new GameEnd(room.getSortedPlayers(con)[1] == room.getHost() ?
                GameEnd.EndingType.HOST_WON : GameEnd.EndingType.GUEST_WON,
                GameEnd.ReasonType.BY_OPPONENT_SURRENDER);

        room.getHost().getConnection().sendMessage(gameEnd);
        room.getGuest().getConnection().sendMessage(gameEnd);
    }

    public void handleDrawOffer(Connection con){
        con.getRoom().getSortedPlayers(con)[1].getConnection().sendMessage(new OfferDraw());
    }

    public void handleDrawAgree(Connection con){
        Room room = con.getRoom();
        room.getGame().stop();
        room.removeGame();
        GameEnd gameEnd = new GameEnd(GameEnd.EndingType.DRAW, GameEnd.ReasonType.BY_AGREEMENT);
        room.getHost().getConnection().sendMessage(gameEnd);
        room.getGuest().getConnection().sendMessage(gameEnd);
        
    }

    public void handleLeaveGame(Connection con){
        Room room = con.getRoom();
        Player[] players = room.getSortedPlayers(con);
        if (players[1] == null) {
            RoomList.getRoomList().remove(room);
            return;
        }
        if (room.getGame() != null) {
            room.getGame().stop();
            room.removeGame();
            GameEnd gameEnd = new GameEnd(room.checkHost(con) ? GameEnd.EndingType.GUEST_WON : GameEnd.EndingType.HOST_WON,
                    GameEnd.ReasonType.BY_OPPONENT_LEFT);
            players[1].getConnection().sendMessage(gameEnd);
        } else if (!room.checkHost(con)) {
            room.getHost().getConnection().sendMessage(new OpponentLeft());
        }
        if (room.checkHost(con)) {
            room.setHost(players[1]);
            room.setRoomID(room.getHost().getConnection().ipToCode());
            RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
            room.getHost().getConnection().sendMessage(new GameID(room.getRoomID(), ruleSet));
        }
        room.setGuest(null);
    }

    private void handleTurnStart(Connection con) {
        Game game = con.getRoom().getGame();
        if (game != null) {
            if (!game.isReady()) {
                try {
                    synchronized (this) {
                        this.wait();
                        System.out.println("\t\tGame thread signal not ready yet. Please wait.");
                    }
                } catch (InterruptedException ex) {
                }
                System.out.println("\t\tGame thread is ready.");
            }
            game.resumeTurn();
        } else
            System.out.println("\t\tGame already ended.");
    }

    private void handleResetBoard(ResetBoard rbPacket, Connection con) {
        con.getRoom().getGuest().getConnection().sendMessage(rbPacket);
    }

    private Room findRoom(String roomID){
        for (Room x : RoomList.getRoomList()){
            if(x.getRoomID() == null ? roomID == null : x.getRoomID().equals(roomID))
                return x;
        }
        return null;
    }

}
