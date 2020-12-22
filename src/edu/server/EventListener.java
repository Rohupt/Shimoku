package edu.server;

import edu.common.packet.RuleSet;
import com.google.gson.Gson;
import edu.common.packet.*;
import edu.common.packet.client.*;
import edu.common.packet.server.GameID;
import edu.common.packet.server.GameInfo;
import edu.common.packet.server.GuestFound;
import edu.common.engine.Game;
import edu.common.engine.GameSettings;
import edu.common.engine.Move;
import edu.common.engine.Player;
import edu.common.engine.Room;
import edu.common.packet.server.ConfirmRule;
import edu.common.packet.server.JoinFailed;
import edu.server.room.RoomList;
import java.net.InetSocketAddress;
import java.util.Arrays;
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
        String packetID = (String) packetJson.get("id");
        System.out.printf("Received a packet: %s\n", p);

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

    /**
     * @param crtPacket
     * @param con
     * Handle packet create new room
     */
    public void handleCreateGame(CreateGame crtPacket,Connection con){
        Room room = new Room();
        room.setSettings(new GameSettings());
        room.setHost(new Player(crtPacket.getUsername(),con));
        room.getHost().getConnection().setRoom(room);
        con.setRoom(room);

        // Create new ID for this room
        room.setRoomID(Room.ipToHex(con));

        // Add room to the end of room list
        RoomList.getRoomList().addLast(room);
        RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
        GameID gameID = new GameID(room.getRoomID(), ruleSet);
        con.sendObject(gameID);
    }

    /**
     * @param rulePacket
     * @param con
     */
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

    /**
     * @param joinPacket
     * @param con
     */
    public void handleJoinGame(JoinGame joinPacket, Connection con){
        Room room = findRoom(joinPacket.getRoomID());
        if(room != null) {
            if(room.getGuest() == null){
                // Add guest player to room, add connection room -> connection
                room.setGuest(new Player(joinPacket.getUsername(),con));
                room.getGuest().getConnection().setRoom(room);
                con.setRoom(room);

                // Send guest found object to host
                GuestFound guestFound = new GuestFound(joinPacket.getUsername());
                room.getHost().getConnection().sendObject(guestFound);

                // Send room info to guest
                RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                    room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                    room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
                GameInfo info = new GameInfo(ruleSet,room.getHost().getUsername());
                con.sendObject(info);
                return;
            }else{
                System.out.println("Room full, cannot join");
            }
        }else{
            System.out.println("Room not found, Join Request refused");
        }
        JoinFailed jfPacket = new JoinFailed(room != null);
        con.sendObject(jfPacket);
    }

    /**
     * @param startRq
     * @param con
     */
    public void handleStartRq(StartRequest startRq, Connection con){
        Room room = con.getRoom(); // Get room from connection
        if(room != null){
            if(room.getGuest() != null){
                // Create new state and start room (room extends room)
                room.newGame(room.getSettings());
                room.getGame().start();
            }else{
                // Cho nay xu ly ben front end
                System.out.println("Guest player not found");
            }
            // Send GameStart packet to 2 client
        }else{
            System.out.println("Room not found, Start Request refused");
            //Send back to client some code
        }
    }

    /**
     * @param stone
     * @param con
     */
    public void handleStonePutRq(StonePut stone,Connection con){
        Move newMove = new Move(stone.getX(),stone.getY());
        Game game = con.getRoom().getGame();
        if(game.setUserMove(newMove)){
            //Do something
        }else{
            //Do something
        }
    }

    /**
     *
     * @param surPacket
     * @param con
     */
    public void handleSurPacket(Surrender surPacket,Connection con){
        // Send EndGame Packet
        Room room = con.getRoom();

        Player[] players = room.getSortedPlayers(con);
        Player surPlayer = players[0];
        Player winPlayer = players[1];

        GameEnd gameEnd = new GameEnd();
        gameEnd.setReason(GameEnd.ReasonType.BY_OPPONENT_SURRENDER);

        if(winPlayer == room.getHost()){
            gameEnd.setEndingType(GameEnd.EndingType.HOST_WON);
        }else if(winPlayer == room.getGuest()){
            gameEnd.setEndingType(GameEnd.EndingType.GUEST_WON);
        }

        winPlayer.getConnection().sendObject(gameEnd);
        surPlayer.getConnection().sendObject(gameEnd);
    }

    /**
     * Receive OfferDraw Packet and forward to other player
     * @param con
     */
    public void handleDrawRq(Connection con){
        Room room = con.getRoom();

        Player[] players = room.getSortedPlayers(con);
        Player drawPlayer = players[0];
        Player recvDrawPlayer = players[1];

        recvDrawPlayer.getConnection().sendObject(drawPlayer);
    }

    public  void handleDrawRs(DrawResponse drawRs,Connection con){
        Room room = con.getRoom();

        if(drawRs.isAgree()){
            GameEnd gameEnd = new GameEnd(GameEnd.EndingType.DRAW, GameEnd.ReasonType.BY_AGREEMENT);

            room.getHost().getConnection().sendObject(gameEnd);
            room.getGuest().getConnection().sendObject(gameEnd);
        }else{
            // Forward drawRs to other player to player resume Move
            Player[] players = room.getSortedPlayers(con);
            players[1].getConnection().sendObject(drawRs);
        }
    }

    /**
     * @param con
     */
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
            room.setRoomID(Room.ipToHex(room.getHost().getConnection()));
            RuleSet ruleSet = new RuleSet(room.getSettings().getSize(),
                                room.getSettings().gameTimingEnabled() ? room.getSettings().getGameTimeMillis() : -1,
                                room.getSettings().moveTimingEnabled() ? room.getSettings().getMoveTimeMillis() : -1);
            room.getHost().getConnection().sendObject(new GameID(room.getRoomID(), ruleSet));
        }
        room.setGuest(null);

        /*if (room.getGame() != null) {
            if (room.getGame().checkAlive()) {
                Player winPlayer = players[1];

                GameEnd gameEnd = new GameEnd();
                gameEnd.setReason(GameEnd.ReasonType.BY_OPPONENT_LEFT);

                if(winPlayer == room.getHost()){
                    gameEnd.setEndingType(GameEnd.EndingType.HOST_WON);
                }else if (winPlayer == room.getGuest()){
                    gameEnd.setEndingType(GameEnd.EndingType.GUEST_WON);
                }

                // In this case only need to send to winner player
                winPlayer.getConnection().sendObject(gameEnd);
            }
            

            // If remain player is not host player set it host
            if(room.checkHost(con)){
                room.setHost(players[1]);
            }
            room.setGuest(null);
        } else {
            if(players[1] == null){
                //No players in this room, remove this room
                RoomList.roomList.remove(room);
                return;
            }else if(room.checkHost(con)){
                //Set remain player as host player
                room.setHost(players[1]);
            }
            room.setGuest(null);

            //Send ClientLeft object to remain player
            players[1].getConnection().sendObject(new OpponentLeft());
        }*/
    }

    /**
     * @param roomID
     * @return room found
     * Find a room in room list with id
     */
    public Room findRoom(String roomID){
        for (Room x : RoomList.getRoomList()){
            if(x.getRoomID() == null ? roomID == null : x.getRoomID().equals(roomID))
                return x;
        }
        return null;
    }

    /**
     * @param p
     * @param con
     * Function to received object from client
     * Classify request packet and execute
     */
    public void received(Object p, Connection con){
        if(p instanceof CreateGame){
            CreateGame crtPacket = (CreateGame)p;
            handleCreateGame(crtPacket,con);
        }else if(p instanceof RuleSet){
            RuleSet rulePacket = (RuleSet) p;
            handleRuleSet(rulePacket,con);
        }else if(p instanceof JoinGame){
            JoinGame joinPacket = (JoinGame)p;
            handleJoinGame(joinPacket,con);
        }else if(p instanceof StartRequest){
            StartRequest startRq = (StartRequest)p;
            handleStartRq(startRq,con);
        }else if(p instanceof StonePut){
            StonePut stone = (StonePut)p;
            handleStonePutRq(stone,con);
        }else if(p instanceof Surrender){
            // If a player surrender -> GameEnd
            Surrender surPacket = (Surrender)p;
            handleSurPacket(surPacket,con);
        }else if(p instanceof LeaveGame){
            // If opponent left the winner is the remainder
            // Send GameEnd to only winner
            handleLeaveGame(con);
        }else if(p instanceof OfferDraw){
            // A player send draw offer request to another player
            OfferDraw drawRq = (OfferDraw)p;
            handleDrawRq(con);
        }else if(p instanceof DrawResponse){
            // A player received draw offer request and response
            DrawResponse drawRs = (DrawResponse)p;
            handleDrawRs(drawRs,con);
        }
        // GameEnd
        // Surrender
        // OpponentLeft
        // OfferDraw
        // DrawResponse
    }
}
