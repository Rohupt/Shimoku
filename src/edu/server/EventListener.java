package edu.server;

import edu.common.packet.RuleSet;
import com.google.gson.Gson;
import edu.common.packet.*;
import edu.common.packet.client.*;
import edu.common.packet.server.*;
import edu.common.engine.*;
import edu.server.room.RoomList;
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
        System.out.printf("Received a packet: %s:%s\n\t%s\n", remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort(), p);

        Gson gson = new Gson();
        switch (packetID){
            case "00":
                //Create Game
                CreateGame crtPacket = gson.fromJson(p,CreateGame.class);
                handleCreateGame(crtPacket,con);
                break;
            case "02":
                //RuleSet
                RuleSet rulePacket = gson.fromJson(p,RuleSet.class);
                handleRuleSet(rulePacket,con);
                break;
            case "04":
                //Join Game
                JoinGame joinPacket = gson.fromJson(p,JoinGame.class);
                handleJoinGame(joinPacket,con);
                break;
            case "07":
                //Start Request
                StartRequest startRq = gson.fromJson(p,StartRequest.class);
                handleStartRq(startRq,con);
                break;
            case "09":
                //Stone Put
                StonePut stone = gson.fromJson(p,StonePut.class);
                handleStonePutRq(stone,con);
                break;
            case "0a":
                //Surrender
                // If a player surrender -> GameEnd
                Surrender surPacket = gson.fromJson(p,Surrender.class);
                handleSurPacket(surPacket,con);
                break;
            case "0b":
                //Leave Game
                // If opponent left the winner is the remainder
                // Send GameEnd to only winner
                handleLeaveGame(con);
                break;
            case "0d":
                //Offer Draw
                handleDrawRq(con);
                break;
            case "0e":
                //Draw Response
                DrawResponse drawRs = gson.fromJson(p,DrawResponse.class);
                handleDrawRs(drawRs,con);
                break;
            default:
                break;
        }
    }

    public void handleCreateGame(CreateGame cgPacket,Connection con){
        Room room = new Room();
        room.setSettings(new GameSettings());
        room.setHost(new Player(cgPacket.getUsername(),con));
        room.getHost().getConnection().setRoom(room);
        con.setRoom(room);

        // Create new ID for this room
        room.setRoomID(con.ipToHex());

        // Add room to the end of room list
        RoomList.getRoomList().addLast(room);
        RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
        GameID gameID = new GameID(room.getRoomID(), ruleSet);
        con.sendObject(gameID);
    }

    public void handleRuleSet(RuleSet rulePacket, Connection con){
        //Only host client can set rule
        Room room = con.getRoom();
        GameSettings temp = new GameSettings(room.getSettings());
        boolean successed = false;
        try {
            room.getSettings().setSize(rulePacket.getSize());
            
            if (rulePacket.getGameTime() == -1) {
                room.getSettings().setGameTimingEnabled(false);
            } else {
                room.getSettings().setGameTimingEnabled(true);
                room.getSettings().setGameTimeMillis(rulePacket.getGameTime());
            }
            
            if (rulePacket.getMoveTime() == -1) {
                room.getSettings().setMoveTimingEnabled(false);
            } else {
                room.getSettings().setMoveTimingEnabled(true);
                room.getSettings().setMoveTimeMillis(rulePacket.getMoveTime());
            }
            successed = true;
        } catch (Exception e) {
            System.out.println("Rules changes failed");
            room.setSettings(temp);
        }
        
        ConfirmRule crPacket = new ConfirmRule(successed);
        con.sendObject(crPacket);

        if (room.getGuest() != null && successed) {
            // end RuleSet packet to guest client
            room.getGuest().getConnection().sendObject(rulePacket);
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
                room.getHost().getConnection().sendObject(guestFound);

                // Send room info to guest
                RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                    room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                    room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
                con.sendObject(new GameInfo(ruleSet,room.getHost().getUsername()));
                return;
            }
        JoinFailed jfPacket = new JoinFailed(room != null);
        con.sendObject(jfPacket);
    }

    public void handleStartRq(StartRequest srPacket, Connection con){
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

    public void handleStonePutRq(StonePut spPacket,Connection con){
        Game game = con.getRoom().getGame();
        game.setUserMove(new Move(spPacket.getX(),spPacket.getY()));
    }

    public void handleSurPacket(Surrender surPacket,Connection con){
        // Send EndGame Packet
        Room room = con.getRoom();
        room.getGame().stop();
        room.removeGame();

        GameEnd gameEnd = new GameEnd(room.getSortedPlayers(con)[1] == room.getHost() ?
                GameEnd.EndingType.HOST_WON : GameEnd.EndingType.GUEST_WON,
                GameEnd.ReasonType.BY_OPPONENT_SURRENDER);

        room.getHost().getConnection().sendObject(gameEnd);
        room.getGuest().getConnection().sendObject(gameEnd);
    }

    public void handleDrawRq(Connection con){
        con.getRoom().getSortedPlayers(con)[1].getConnection().sendObject(new OfferDraw());
    }

    public void handleDrawRs(DrawResponse drawRs, Connection con){
        Room room = con.getRoom();
        room.getGame().stop();
        room.removeGame();
        GameEnd gameEnd = new GameEnd(GameEnd.EndingType.DRAW, GameEnd.ReasonType.BY_AGREEMENT);
        room.getHost().getConnection().sendObject(gameEnd);
        room.getGuest().getConnection().sendObject(gameEnd);
        
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
            players[1].getConnection().sendObject(gameEnd);
        } else {
            players[1].getConnection().sendObject(new OpponentLeft());
        }
        if (room.checkHost(con)) {
            room.setHost(players[1]);
            room.setRoomID(room.getHost().getConnection().ipToHex());
            RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
            room.getHost().getConnection().sendObject(new GameID(room.getRoomID(), ruleSet));
        }
        room.setGuest(null);
    }

    public Room findRoom(String roomID){
        for (Room x : RoomList.getRoomList()){
            if(x.getRoomID() == null ? roomID == null : x.getRoomID().equals(roomID))
                return x;
        }
        return null;
    }
}
