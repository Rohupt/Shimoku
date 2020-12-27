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
                case "id":
                    //Game ID
                    controller.handleIDPacket(gson.fromJson(p, GameID.class));
                    break;
                case "rs":
                    //Rule set
                    controller.handleRuleChanges(gson.fromJson(p, RuleSet.class));
                    break;
                case "cr":
                    //Confirm rule changes
                    controller.handleRuleConfirmed(gson.fromJson(p, ConfirmRule.class));
                    break;
                case "gf":
                    //Guest found
                    controller.handleGuestFound(gson.fromJson(p, GuestFound.class));
                    break;
                case "gi":
                    //Game info
                    controller.handleGameInfo(gson.fromJson(p, GameInfo.class));
                    break;
                case "jf":
                    //Join game request failed, room is full or not found
                    controller.handleJoinFailed(gson.fromJson(p, JoinFailed.class));
                    break;
                case "gs":
                    //Game start
                    controller.handleGameStart(gson.fromJson(p, GameStart.class));
                    break;
                case "sp":
                    //Opponent move
                    controller.handleOpponentMove(gson.fromJson(p, StonePut.class));
                    break;
                case "ge":
                    //Game end
                    controller.handleGameEnd(gson.fromJson(p, GameEnd.class));
                    break;
                case "ol":
                    //Opponent left
                    controller.handleOpponentLeft(gson.fromJson(p, OpponentLeft.class));
                    break;
                case "od":
                    //Offer draw - the receiving end
                    controller.handleDrawOffer(gson.fromJson(p, OfferDraw.class));
                    break;
                case "dr":
                    //Draw response - the receiving end
                    controller.handleDrawResponse(gson.fromJson(p, DrawResponse.class));
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
