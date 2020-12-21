package edu.client;

import com.google.gson.Gson;
import edu.client.events.GameListener;
import edu.client.gui.controllers.Controller;
import edu.common.packet.*;
import edu.common.packet.server.*;

import java.util.LinkedList;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EventListener  {
    private final List<GameListener> listeners;
    private final Client client;
    private Controller controller;

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

        Gson gson = new Gson();
        switch (packetID) {
            case "01":
                //Game ID
                GameID idPacket = gson.fromJson(p, GameID.class);
                handleIDPacket(idPacket);
                break;
            case "02":
                //Rule set, only guest clients receive this
                RuleSet rsPacket = gson.fromJson(p, RuleSet.class);
                handleRuleChanges(rsPacket);
                break;
            case "03":
                //Confirm rule changes
                ConfirmRule cfPacket = gson.fromJson(p, ConfirmRule.class);
                handleRuleConfirmed(cfPacket);
                break;
            case "05":
                //Guest found
                GuestFound gfPacket = gson.fromJson(p, GuestFound.class);
                handleGuestFound(gfPacket);
                break;
            case "08":
                //Game start
                GameStart gsPacket = gson.fromJson(p, GameStart.class);
                handleGameStart(gsPacket);
                break;
            case "09":
                //Opponent move
                StonePut spPacket = gson.fromJson(p, StonePut.class);
                handleOpponentMove(spPacket);
                break;
            case "0f":
                //Game end
                GameEnd gePacket = gson.fromJson(p, GameEnd.class);
                handleGameEnd(gePacket);
                break;
            case "0c":
                //Opponent left
                OpponentLeft olPacket = gson.fromJson(p, OpponentLeft.class);
                handleOpponentLeft(olPacket);
                break;
            case "0d":
                //Offer draw - the receiving end
                OfferDraw odPacket = gson.fromJson(p, OfferDraw.class);
                handleDrawOffer(odPacket);
                break;
            case "0e":
                //Draw response - the receiving end
                DrawResponse drPacket = gson.fromJson(p, DrawResponse.class);
                handleDrawResponse(drPacket);
                break;
        }
    }

    private void handleIDPacket(GameID idPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleRuleChanges(RuleSet rsPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleRuleConfirmed(ConfirmRule cfPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleGuestFound(GuestFound gfPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleGameStart(GameStart gsPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleOpponentMove(StonePut spPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleGameEnd(GameEnd gePacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleOpponentLeft(OpponentLeft olPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleDrawResponse(DrawResponse drPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void handleDrawOffer(OfferDraw odPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
