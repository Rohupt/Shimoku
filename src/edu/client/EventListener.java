package edu.client;

import com.google.gson.Gson;
import edu.client.events.GameListener;
import edu.client.gui.controllers.MainGUIController;
import edu.common.packet.*;
import edu.common.packet.server.*;

import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EventListener  {
    private final List<GameListener> listeners;
    private final Client client;
    private MainGUIController controller;

    public EventListener(Client client) {
        this.listeners = new LinkedList<>();
        this.client = client;
    }

    public void addListener(GameListener listener) {
        this.listeners.add(listener);
    }

    public List<GameListener> getListeners() {
        return listeners;
    }

    public void received_data(String p) {
        JSONParser parser = new JSONParser();
        JSONObject packetJson = null;
        try {
            packetJson = (JSONObject) parser.parse(p);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String packetID = (String) packetJson.get("id");
        System.out.printf("Received a packet:\n\t%s\n", p);

        Gson gson = new Gson();
        Platform.runLater(() -> {
            switch (packetID) {
                case "01":
                    //Game ID
                    GameID idPacket = gson.fromJson(p, GameID.class);
                    controller.handleIDPacket(idPacket);
                    break;
                case "02":
                    //Rule set
                    RuleSet rsPacket = gson.fromJson(p, RuleSet.class);
                    controller.handleRuleChanges(rsPacket, true);
                    break;
                case "03":
                    //Confirm rule changes
                    ConfirmRule cfPacket = gson.fromJson(p, ConfirmRule.class);
                    controller.handleRuleConfirmed(cfPacket);
                    break;
                case "05":
                    //Guest found
                    GuestFound gfPacket = gson.fromJson(p, GuestFound.class);
                    controller.handleGuestFound(gfPacket);
                    break;
                case "06":
                    GameInfo giPacket = gson.fromJson(p, GameInfo.class);
                    controller.handleGameInfo(giPacket);
                    break;
                case "08":
                    //Game start
                    GameStart gsPacket = gson.fromJson(p, GameStart.class);
                    controller.handleGameStart(gsPacket);
                    break;
                case "09":
                    //Opponent move
                    StonePut spPacket = gson.fromJson(p, StonePut.class);
                    controller.handleOpponentMove(spPacket);
                    break;
                case "0f":
                    //Game end
                    GameEnd gePacket = gson.fromJson(p, GameEnd.class);
                    controller.handleGameEnd(gePacket);
                    break;
                case "0c":
                    //Opponent left
                    OpponentLeft olPacket = gson.fromJson(p, OpponentLeft.class);
                    controller.handleOpponentLeft(olPacket);
                    break;
                case "0d":
                    //Offer draw - the receiving end
                    OfferDraw odPacket = gson.fromJson(p, OfferDraw.class);
                    controller.handleDrawOffer(odPacket);
                    break;
                case "0e":
                    //Draw response - the receiving end
                    DrawResponse drPacket = gson.fromJson(p, DrawResponse.class);
                    controller.handleDrawResponse(drPacket);
                    break;
                case "10":
                    //Join game request failed, room is full or not found
                    JoinFailed jfPacket = gson.fromJson(p, JoinFailed.class);
                    controller.handleJoinFailed(jfPacket);
                    break;
            }
        });
        
    }

    public MainGUIController getController() {
        return controller;
    }

    public void setController(MainGUIController controller) {
        this.controller = controller;
    }
}
