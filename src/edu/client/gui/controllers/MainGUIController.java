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
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javafx.scene.layout.VBox;

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
    private Label player1GameTimeLabel;
    @FXML
    private Label player1MoveTimeLabel;
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
    private Label gameCodeLabel;
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

    public void setListener(EventListener listener) {
        this.listener = listener;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        listener = ClientMain.getClient().getListener();
        listener.setController(this);
        gameBtnPane.setDisable(true);
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
        gameTimingCheckBox.selectedProperty().addListener((b, o, n) -> gameTimingEnabled());
        moveTimingCheckBox.selectedProperty().addListener((b, o, n) -> moveTimingEnabled());
        
        createBtn.setOnAction(e -> createGame());
        joinBtn.setOnAction(e -> joinGame());
        confirmBtn.setOnAction(e -> updateSettings());
        discardBtn.setOnAction(e -> loadSettings());
    }
    
    private void fieldChanged() {
        createBtn.setDisable(nameField.getText().isBlank());
        joinBtn.setDisable(nameField.getText().isBlank() || codeField.getText().isBlank());
    }
    
    private void createGame() {
        CreateGame cgPacket = new CreateGame(nameField.getText());
        ClientMain.getClient().sendObject(cgPacket);
    }
    
    private void joinGame() {
        JoinGame jgPacket = new JoinGame(codeField.getText(), nameField.getText());
        ClientMain.getClient().sendObject(jgPacket);
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
        hostNameLabel.setText(host.getUsername());
        gameCodeLabel.setText(room.getRoomID());
        loadSettings();
        gamePane.setDisable(true);
        settingsPane.setDisable(false);
        gameBtnPane.setDisable(false);
    }

    public void handleRuleChanges(RuleSet rsPacket) {
        ClientMain.getRoom().getSettings().setGameTimingEnabled(rsPacket.getGameTime() != -1);
        ClientMain.getRoom().getSettings().setMoveTimingEnabled(rsPacket.getMoveTime() != -1);
        if (ClientMain.getRoom().getSettings().gameTimingEnabled())
            ClientMain.getRoom().getSettings().setGameTimeMillis(rsPacket.getGameTime());
        if (ClientMain.getRoom().getSettings().moveTimingEnabled())
            ClientMain.getRoom().getSettings().setMoveTimeMillis(rsPacket.getMoveTime());
        ClientMain.getRoom().getSettings().setSize(rsPacket.getSize());
        loadSettings();
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
        Player host = new Player(giPacket.getHostUsername(), null);
        Player guest = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setGuest(guest);
        room.setRoomID(codeField.getText());
        room.setSettings(new GameSettings());
        ClientMain.setRoom(room);
        handleRuleChanges(giPacket.getRuleSet());
        hostNameLabel.setText(host.getUsername());
        guestNameLabel.setText(guest.getUsername());
        gameCodeLabel.setText(room.getRoomID());
        settingsPane.setDisable(true);
        gamePane.setDisable(true);
        startBtn.setDisable(true);
        gameBtnPane.setDisable(false);
    }

    public void handleJoinFailed(JoinFailed jfPacket) {
        String header = jfPacket.isFound()
                ? "Room is full. Request declined."
                : "Room not found";
        alert(AlertType.ERROR, "Error", header).showAndWait();
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

    public void handleOpponentLeft(OpponentLeft olPacket) {
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
