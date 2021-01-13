package edu.client;

import edu.common.packet.server.GameEnd;
import com.google.gson.Gson;
import edu.client.gui.controllers.MainGUIController;
import edu.common.packet.*;
import edu.common.packet.server.*;

import javafx.application.Platform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EventListener  {
    private MainGUIController controller;

    public void received_data(String p) {
        JSONParser parser = new JSONParser();
        JSONObject packetJson = null;
        try {
            packetJson = (JSONObject) parser.parse(p);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String packetID = (String) packetJson.get("id");

        Platform.runLater(() -> {
            switch (packetID) {
                case "id":
                    //Game ID
                    controller.handleIDPacket(logPacket(p, GameID.class));
                    break;
                case "rs":
                    //Rule set
                    controller.handleRuleChanges(logPacket(p, RuleSet.class));
                    break;
                case "cr":
                    //Confirm rule changes
                    controller.handleRuleConfirmed(logPacket(p, ConfirmRules.class));
                    break;
                case "gf":
                    //Guest found
                    controller.handleGuestFound(logPacket(p, GuestFound.class));
                    break;
                case "gi":
                    //Game info
                    controller.handleGameInfo(logPacket(p, GameInfo.class));
                    break;
                case "jf":
                    //Join game request failed, room is full or not found
                    controller.handleJoinFailed(logPacket(p, JoinFailed.class));
                    break;
                case "gs":
                    //Game start
                    controller.handleGameStart(logPacket(p, GameStart.class));
                    break;
                case "sp":
                    //Opponent move
                    controller.handleOpponentMove(logPacket(p, StonePut.class));
                    break;
                case "ge":
                    //Game end
                    controller.handleGameEnd(logPacket(p, GameEnd.class));
                    break;
                case "ol":
                    //Opponent left
                    logPacket(p, OpponentLeft.class);
                    controller.handleOpponentLeft();
                    break;
                case "od":
                    //Offer draw
                    logPacket(p, OfferDraw.class);
                    controller.handleDrawOffer();
                    break;
                case "rb":
                    //Reset board
                    logPacket(p, ResetBoard.class);
                    controller.handleResetBoard();
            }
        });
        
    }
    
    public <T extends Packet> T logPacket(String p, Class<T> type) {
        T pkt = new Gson().fromJson(p, type);
        System.out.printf("\n%s received:\n\t%s\n", pkt.getPacketName(), p);
        return pkt;
    }

    public MainGUIController getController() {
        return controller;
    }

    public void setController(MainGUIController controller) {
        this.controller = controller;
    }
}
