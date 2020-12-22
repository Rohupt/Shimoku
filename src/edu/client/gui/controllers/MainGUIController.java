package edu.client.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import edu.client.ClientMain;
import edu.client.EventListener;
import edu.client.gui.views.BoardPane;
import edu.common.engine.*;
import edu.common.packet.*;
import edu.common.packet.server.*;
import edu.common.packet.client.*;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author honor
 */
public class MainGUIController implements Initializable {
    //<editor-fold defaultstate="collapsed" desc="Element declarations">
    @FXML
    private GridPane hostPane;
    @FXML
    private Label hostNameLabel;
    @FXML
    private Label hostGameTimeLabel;
    @FXML
    private Label hostMoveTimeLabel;
    @FXML
    private GridPane guestPane;
    @FXML
    private Label guestNameLabel;
    @FXML
    private Label turnLabel;
    @FXML
    private GridPane gamePane;
    @FXML
    private TextField nameField;
    @FXML
    private TextField codeField;
    @FXML
    private Button createBtn;
    @FXML
    private Button joinBtn;
    @FXML
    private CheckBox gameTimingCheckBox;
    @FXML
    private ComboBox<Integer> gameTimeComboBox;
    @FXML
    private CheckBox moveTimingCheckBox;
    @FXML
    private ComboBox<Integer> moveTimeComboBox;
    @FXML
    private ComboBox<Integer> sizeComboBox;
    @FXML
    private Button confirmBtn;
    @FXML
    private Button discardBtn;
    @FXML
    private Label codeLabel;
    @FXML
    private Button copyBtn;
    @FXML
    private Button leaveBtn;
    @FXML
    private Button surrenderBtn;
    @FXML
    private Button drawBtn;
    @FXML
    private Label guestGameTimeLabel;
    @FXML
    private Label guestMoveTimeLabel;
    @FXML
    private Button startBtn;
    @FXML
    private VBox gameBtnPane;
    @FXML
    private GridPane settingsPane;
    @FXML
    private BoardPane board;
    //</editor-fold>
    
    private EventListener listener;
    private boolean isInGame;
    private byte position; // 1 is host, 2 is guest, 0 is not set

    public void setListener(EventListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isInGame = false;
        setPosition((byte) 0);
        listener = ClientMain.getClient().getListener();
        listener.setController(this);
        createBtn.setDisable(true);
        joinBtn.setDisable(true);
        nameField.requestFocus();
        
        gameTimeComboBox.getItems().addAll(IntStream.rangeClosed(1, 12).map(x -> x * 5).boxed().toArray(Integer[]::new));
        moveTimeComboBox.getItems().addAll(IntStream.rangeClosed(1, 12).map(x -> x * 5).boxed().toArray(Integer[]::new));
        sizeComboBox.getItems().addAll(IntStream.rangeClosed(9, 25).boxed().toArray(Integer[]::new));
        
        addHandlers();
    }
    
    private void addHandlers() {
        nameField.textProperty().addListener((b, o, n) -> fieldChanged());
        codeField.textProperty().addListener((b, o, n) -> fieldChanged());
        codeLabel.textProperty().addListener((b, o, n) -> copyBtn.setDisable(codeLabel.getText().isBlank()));
        gameTimingCheckBox.selectedProperty().addListener((b, o, n) -> gameTimingEnabled());
        moveTimingCheckBox.selectedProperty().addListener((b, o, n) -> moveTimingEnabled());
        
        createBtn.setOnAction(e -> createGame());
        joinBtn.setOnAction(e -> joinGame());
        copyBtn.setOnAction(e -> copyCode());
        confirmBtn.setOnAction(e -> updateSettings());
        discardBtn.setOnAction(e -> loadSettings());
        leaveBtn.setOnAction(e -> leaveRoom());
    }
    
    private void fieldChanged() {
        createBtn.setDisable(nameField.getText().isBlank());
        joinBtn.setDisable(nameField.getText().isBlank() || codeField.getText().isBlank());
    }
    
    private void copyCode() {
        final ClipboardContent content = new ClipboardContent();
        content.put(DataFormat.PLAIN_TEXT, codeLabel.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }
    
    private void createGame() {
        ClientMain.getClient().sendObject(new CreateGame(nameField.getText()));
    }
    
    private void joinGame() {
        ClientMain.getClient().sendObject(new JoinGame(codeField.getText(), nameField.getText()));
    }
    
    private void leaveRoom() {
        exitGame();
        position = 0;
        hostNameLabel.setText("");
        guestNameLabel.setText("");
        ClientMain.setRoom(null);
        gamePane.setDisable(false);
        gameBtnPane.setDisable(true);
        settingsPane.setDisable(true);
        ClientMain.getClient().sendObject(new LeaveGame());
    }
    
    private void setPosition(byte p) {
        position = p;
        if (p == 0) {
            codeLabel.setText("");
            hostNameLabel.setText("");
            guestNameLabel.setText("");
            hostGameTimeLabel.setText("");
            hostMoveTimeLabel.setText("");
            guestGameTimeLabel.setText("");
            guestMoveTimeLabel.setText("");
        }
        if (p == 1) {
            guestNameLabel.setText("");
            ClientMain.getRoom().setGuest(null);
            startBtn.setDisable(true);
        }
        gamePane.setDisable(p != 0);
        gameBtnPane.setDisable(p == 0);
        settingsPane.setDisable(p != 1);
    }
    
    private void exitGame() {
        if (isInGame) {
            isInGame = false;
        }
        hostGameTimeLabel.setText("");
        hostMoveTimeLabel.setText("");
        guestGameTimeLabel.setText("");
        guestMoveTimeLabel.setText("");
    }
    
    private void loadSettings() {
        GameSettings settings = ClientMain.getRoom().getSettings();
        sizeComboBox.setValue(settings.getSize());
        // Avoid showing millisecond values to the user
        gameTimeComboBox.setValue((int) TimeUnit.MINUTES.convert
                (settings.getGameTimeMillis(), TimeUnit.MILLISECONDS));
        moveTimeComboBox.setValue((int) TimeUnit.SECONDS.convert
                (settings.getMoveTimeMillis(), TimeUnit.MILLISECONDS));
        gameTimingCheckBox.setSelected(settings.gameTimingEnabled());
        moveTimingCheckBox.setSelected(settings.moveTimingEnabled());
    }

    private void gameTimingEnabled() {
        gameTimeComboBox.setDisable(!gameTimingCheckBox.isSelected());
        if (gameTimingCheckBox.isSelected())
            gameTimeComboBox.setValue((int) TimeUnit.MINUTES.convert
                (ClientMain.getRoom().getSettings().getGameTimeMillis(), TimeUnit.MILLISECONDS));
    }

    private void moveTimingEnabled() {
        moveTimeComboBox.setDisable(!moveTimingCheckBox.isSelected());
        if (moveTimingCheckBox.isSelected())
            moveTimeComboBox.setValue((int) TimeUnit.SECONDS.convert
                (ClientMain.getRoom().getSettings().getMoveTimeMillis(), TimeUnit.MILLISECONDS));
    }

    private void updateSettings() {
        var settings = new GameSettings();
        settings.setMoveTimeMillis(TimeUnit.MILLISECONDS
                .convert(moveTimeComboBox.getValue(), TimeUnit.SECONDS));
        settings.setMoveTimingEnabled(moveTimingCheckBox.isSelected());
        settings.setGameTimeMillis(TimeUnit.MILLISECONDS.convert
                (gameTimeComboBox.getValue(), TimeUnit.MINUTES));
        settings.setGameTimingEnabled(gameTimingCheckBox.isSelected());
        settings.setSize(sizeComboBox.getValue());
        
        RuleSet rsPacket = new RuleSet(settings.getSize(),
                settings.gameTimingEnabled() ? settings.getGameTimeMillis() : -1,
                settings.moveTimingEnabled() ? settings.getMoveTimeMillis() : -1);
        ClientMain.getClient().sendObject(rsPacket);
        settingsPane.setDisable(true);
    }

    public void handleIDPacket(GameID idPacket) {
        Player host = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setRoomID(idPacket.getRoomID());
        room.setSettings(new GameSettings());
        ClientMain.setRoom(room);
        setPosition((byte) 1);
        hostNameLabel.setText(host.getUsername());
        guestNameLabel.setText("");
        codeLabel.setText(room.getRoomID());
        loadSettings();
    }

    public void handleRuleChanges(RuleSet rsPacket, boolean alerted) {
        ClientMain.getRoom().getSettings().setGameTimingEnabled(rsPacket.getGameTime() != -1);
        ClientMain.getRoom().getSettings().setMoveTimingEnabled(rsPacket.getMoveTime() != -1);
        if (ClientMain.getRoom().getSettings().gameTimingEnabled())
            ClientMain.getRoom().getSettings().setGameTimeMillis(rsPacket.getGameTime());
        if (ClientMain.getRoom().getSettings().moveTimingEnabled())
            ClientMain.getRoom().getSettings().setMoveTimeMillis(rsPacket.getMoveTime());
        ClientMain.getRoom().getSettings().setSize(rsPacket.getSize());
        loadSettings();
        if (alerted)
            alert(AlertType.WARNING, "Warning", "Game rules changed").show();
    }

    public void handleRuleConfirmed(ConfirmRule cfPacket) {
        if (cfPacket.isStatus()) {
            var settings = ClientMain.getRoom().getSettings();
            settings.setMoveTimeMillis(TimeUnit.MILLISECONDS
                .convert(moveTimeComboBox.getValue(), TimeUnit.SECONDS));
            settings.setMoveTimingEnabled(moveTimingCheckBox.isSelected());
            settings.setGameTimeMillis(TimeUnit.MILLISECONDS.convert
                (gameTimeComboBox.getValue(), TimeUnit.MINUTES));
            settings.setGameTimingEnabled(gameTimingCheckBox.isSelected());
            settings.setSize(sizeComboBox.getValue());
            settingsPane.setDisable(false);
            alert(AlertType.INFORMATION, "Confirmation", "Rules changed successfully").showAndWait();
        } else {
            loadSettings();
            alert(AlertType.ERROR, "Confirmation", "Failed to change rules").showAndWait();
        }
    }

    public void handleGuestFound(GuestFound gfPacket) {
        Player guest = new Player(gfPacket.getUsername(), null);
        ClientMain.getRoom().setGuest(guest);
        guestNameLabel.setText(guest.getUsername());
        startBtn.setDisable(false);
    }

    public void handleGameInfo(GameInfo giPacket) {
        setPosition((byte) 2);
        Player host = new Player(giPacket.getHostUsername(), null);
        Player guest = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setGuest(guest);
        room.setRoomID(codeField.getText());
        room.setSettings(new GameSettings());
        ClientMain.setRoom(room);
        handleRuleChanges(giPacket.getRuleSet(), false);
        hostNameLabel.setText(host.getUsername());
        guestNameLabel.setText(guest.getUsername());
        codeLabel.setText(room.getRoomID());
        startBtn.setDisable(true);
    }

    public void handleJoinFailed(JoinFailed jfPacket) {
        String header = jfPacket.isFound()
                ? "Room is full. Request declined."
                : "Room not found";
        alert(AlertType.ERROR, "Error", header).showAndWait();
    }

    public void handleOpponentLeft(OpponentLeft olPacket) {
        String mesg = position == 2 ? "Opponent has left. You are the host of this room now." : "Opponent has left.";
        Alert alert = isInGame ? alert(AlertType.INFORMATION, "Game ended", "Opponent has left. You won!")
                : alert(AlertType.INFORMATION, "Notification", mesg);
        exitGame();
        setPosition((byte) 1);
        alert.showAndWait();
    }

    public void handleGameStart(GameStart gsPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void handleOpponentMove(StonePut spPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void handleGameEnd(GameEnd gePacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void handleDrawOffer(OfferDraw odPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void handleDrawResponse(DrawResponse drPacket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Alert alert(AlertType type, String title, String header) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(null);
        return alert;
    }
}
