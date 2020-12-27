package edu.client.gui.controllers;

import edu.client.ClientMain;
import edu.client.EventListener;
import edu.client.gui.views.BoardPane;
import edu.common.engine.*;
import edu.common.packet.*;
import edu.common.packet.server.*;
import edu.common.packet.client.*;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author honor
 */
public class MainGUIController implements Initializable {
    //<editor-fold defaultstate="collapsed" desc="Element declarations">
    @FXML
    private BorderPane parent;
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
    private TextArea notifBoard;
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
    private byte position; // 1 is host, 2 is guest, 0 is not set
    private boolean isBlack;
    private boolean drawOfferSent;

    public void setListener(EventListener listener) {
        this.listener = listener;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setPosition((byte) 0, true);
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
        startBtn.setOnAction(e -> startGame());
        surrenderBtn.setOnAction(e -> surrender());
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
    
    private void setPosition(byte p, boolean clearBoard) {
        position = p;
        switch (p) {
            case 0:
                resetGame();
                codeLabel.setText("");
                hostNameLabel.setText("");
                guestNameLabel.setText("");
                break;
            case 1:
                guestNameLabel.setText("");
                ClientMain.getRoom().setGuest(null);
                startBtn.setDisable(ClientMain.getRoom().getGuest() != null);
                break;
            case 2:
                startBtn.setDisable(true);
                break;
        }
        gamePane.setDisable(p != 0);
        gameBtnPane.setDisable(p == 0);
        settingsPane.setDisable(p != 1);
        if (clearBoard)
            parent.setCenter(null);
    }

    private void setInGame(boolean inGame) {
        startBtn.setDisable(inGame);
        drawBtn.setDisable(!inGame);
        surrenderBtn.setDisable(!inGame);
        if (inGame)
            settingsPane.setDisable(true);
    }
    
    private void sendObject(Object o) {
        ClientMain.getClient().sendObject(o);
    }
    
    private Alert alert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        return alert;
    }
    
    private GameSettings getSettings() {
        return ClientMain.getRoom().getSettings();
    }
    
    private void createGame() {
        sendObject(new CreateGame(nameField.getText()));
    }
    
    private void joinGame() {
        sendObject(new JoinGame(codeField.getText(), nameField.getText()));
    }
    
    private void startGame() {
        sendObject(new StartRequest());
    }
    
    private void leaveRoom() {
        Alert alert = alert(AlertType.CONFIRMATION, "Confirm", "Do you really want to leave the room?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        ButtonType button = alert.showAndWait().get();
        if (button == ButtonType.YES) {
            notifBoard.appendText("You left the room.\n");
            setPosition((byte) 0, true);
            ClientMain.setRoom(null);
            sendObject(new LeaveGame());
        }
    }
    
    private void resetGame() {
        setInGame(false);
        drawOfferSent = false;
        hostGameTimeLabel.setText("");
        hostMoveTimeLabel.setText("");
        guestGameTimeLabel.setText("");
        guestMoveTimeLabel.setText("");
        turnLabel.setText("");
        turnLabel.setBackground(new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        drawBtn.setText("Offer Draw");
        drawBtn.setOnAction(null);
    }
    
    private void loadSettings() {
        GameSettings settings = getSettings();
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
                (getSettings().getGameTimeMillis(), TimeUnit.MILLISECONDS));
    }

    private void moveTimingEnabled() {
        moveTimeComboBox.setDisable(!moveTimingCheckBox.isSelected());
        if (moveTimingCheckBox.isSelected())
            moveTimeComboBox.setValue((int) TimeUnit.SECONDS.convert
                (getSettings().getMoveTimeMillis(), TimeUnit.MILLISECONDS));
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
        sendObject(rsPacket);
        settingsPane.setDisable(true);
    }
    
    private void requestMove() {
        int player = isBlack ? 1 : 2;
        board.enableStonePicker(player);
        turnLabel.setText(String.format("%s", position == 1 ? "\u25c0" : "\u25b6"));
        turnLabel.setTextFill(isBlack ? Color.WHITE : Color.BLACK);
        turnLabel.setBackground(new Background(new BackgroundFill(isBlack ? Color.BLACK : Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        board.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                int row = board.getClosestRow(event.getY());
                int col = board.getClosestCol(event.getX());
                board.addStone(player, row, col, false);
                sendObject(new StonePut(row, col));
                notifBoard.appendText(String.format("Your move: %s\n", BoardPane.convertMoveAlgebraic(row, col, getSettings().getSize())));
                //TODO: Stop the clock
                board.disableStonePicker();
                board.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                turnLabel.setText(String.format("%s", position == 1 ? "\u25b6" : "\u25c0"));
                turnLabel.setTextFill(isBlack ? Color.BLACK : Color.WHITE);
                turnLabel.setBackground(new Background(new BackgroundFill(isBlack ? Color.WHITE : Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
                if ("Agree to Offer".equals(drawBtn.getText())) {
                    drawBtn.setText("Offer Draw");
                    drawBtn.setOnAction(e -> offerDraw());
                }
            }
        });
    }
    
    private void offerDraw() {
        Alert alert = alert(AlertType.CONFIRMATION, "Confirm", "Do you really want to offer a draw?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        notifBoard.appendText("OFFER SENT: You sent an offer for a draw, but the match WILL CONTINUE until the opponent answers or being concluded in another way.\n");
        ButtonType button = alert.showAndWait().get();
        if (button == ButtonType.YES) {
            sendObject(new OfferDraw());
            drawBtn.setDisable(true);
            drawOfferSent = true;
        }
    }
    
    private void drawAgree() {
        Alert alert = alert(AlertType.CONFIRMATION, "Confirm", "Do you really want to accept the offer?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        ButtonType button = alert.showAndWait().get();
        if (button == ButtonType.YES)
            sendObject(new DrawAgree());
    }
    
    private void surrender() {
        Alert alert = alert(AlertType.CONFIRMATION, "Confirm", "Do you really want to surrender?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        ButtonType button = alert.showAndWait().get();
        if (button == ButtonType.YES)
            sendObject(new Surrender());
    }

    public void handleIDPacket(GameID idPacket) {
        Player host = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setRoomID(idPacket.getRoomID());
        room.setSettings(idPacket.getRuleSet().toGameSettings());
        ClientMain.setRoom(room);
        if (position == 2)
            notifBoard.appendText("You are the host of this room now. The game code has changed.\n");
        setPosition((byte) 1, true);
        hostNameLabel.setText(host.getUsername());
        guestNameLabel.setText("");
        codeLabel.setText(room.getRoomID());
        loadSettings();
    }

    public void handleRuleChanges(RuleSet rsPacket) {
        ClientMain.getRoom().setSettings(rsPacket.toGameSettings());
        loadSettings();
        notifBoard.appendText("WARNING: Game rules changed.\n");
    }

    public void handleRuleConfirmed(ConfirmRules cfPacket) {
        if (cfPacket.isSuccessful()) {
            getSettings().setMoveTimeMillis(TimeUnit.MILLISECONDS
                .convert(moveTimeComboBox.getValue(), TimeUnit.SECONDS));
            getSettings().setMoveTimingEnabled(moveTimingCheckBox.isSelected());
            getSettings().setGameTimeMillis(TimeUnit.MILLISECONDS.convert
                (gameTimeComboBox.getValue(), TimeUnit.MINUTES));
            getSettings().setGameTimingEnabled(gameTimingCheckBox.isSelected());
            getSettings().setSize(sizeComboBox.getValue());
            settingsPane.setDisable(false);
            notifBoard.appendText("SUCCESS: Game rules changed successfully.\n");
        } else {
            loadSettings();
            notifBoard.appendText("FAILED: Game rules failed to change.\n");
        }
    }

    public void handleGuestFound(GuestFound gfPacket) {
        Player guest = new Player(gfPacket.getUsername(), null);
        ClientMain.getRoom().setGuest(guest);
        guestNameLabel.setText(guest.getUsername());
        startBtn.setDisable(false);
        notifBoard.appendText("GUEST JOINED: User \"" + guest.getUsername() + "\" has joined.\n");
    }

    public void handleGameInfo(GameInfo giPacket) {
        setPosition((byte) 2, true);
        Player host = new Player(giPacket.getHostUsername(), null);
        Player guest = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setGuest(guest);
        room.setRoomID(codeField.getText());
        room.setSettings(giPacket.getRuleSet().toGameSettings());
        ClientMain.setRoom(room);
        hostNameLabel.setText(host.getUsername());
        guestNameLabel.setText(guest.getUsername());
        codeLabel.setText(room.getRoomID());
        startBtn.setDisable(true);
        notifBoard.appendText("ACCEPTED: Your request is accepted.\n");
    }

    public void handleJoinFailed(JoinFailed jfPacket) {
        if (jfPacket.isFound())
            notifBoard.appendText("DECLINED: Room is full, request declined.\n");
        else
            notifBoard.appendText("NOT FOUND: code invalid, room not found.\n");
    }

    public void handleOpponentLeft() {
        notifBoard.appendText("Opponent has left.\n");
        resetGame();
        setPosition((byte) 1, true);
    }

    public void handleGameStart(GameStart gsPacket) {
        board = new BoardPane(getSettings().getSize());
        parent.setCenter(board);
        drawBtn.setOnAction(e -> offerDraw());
        setInGame(true);
        isBlack = gsPacket.isHostMoveFirst() ? position == 1 : position == 2;
        notifBoard.appendText(String.format("GAME STARTED: %s will move first.\n", isBlack ? "you" : "opponent"));
        if (isBlack)
            requestMove();
        else {
            turnLabel.setText(String.format("%s", position == 1 ? "\u25b6" : "\u25c0"));
            turnLabel.setTextFill(Color.WHITE);
            turnLabel.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        }
    }

    public void handleOpponentMove(StonePut spPacket) {
        if (drawOfferSent) {
            notifBoard.appendText("REJECTED: Opponent rejected your draw offer. Game continues.");
            drawOfferSent = false;
            drawBtn.setDisable(false);
        }
        board.addStone(isBlack ? 2 : 1, spPacket.getX(), spPacket.getY(), false);
        notifBoard.appendText(String.format("Opponent move: %s\n",
                BoardPane.convertMoveAlgebraic(spPacket.getX(), spPacket.getY(), getSettings().getSize())));
        requestMove();
    }

    public void handleGameEnd(GameEnd gePacket) {
        board.setOnMouseClicked(null);
        board.disableStonePicker();
        String result = gePacket.getEndingType() == GameEnd.EndingType.DRAW
                ? "IT'S A DRAW!" : (gePacket.getEndingType() == GameEnd.EndingType.HOST_WON && position == 1) || (gePacket.getEndingType() == GameEnd.EndingType.GUEST_WON && position == 2)
                ? "YOU WON!" : "YOU LOST!";
        String reason = "";
        switch (gePacket.getReason()) {
            case BY_BOARD_FULL: reason = "There's no space left to move."; break;
            case BY_AGREEMENT: reason = "You both realized no one could win."; break;
            case BY_BOTH_DISCONNECTION: reason = "You both disconnected."; break;
            case BY_OPPONENT_LEFT: reason = result.equals("YOU WON!") ? "The opponent cowardly ran away." : "But forfeiting when needed is a great strategem."; break;
            case BY_OPPONENT_SURRENDER: reason = result.equals("YOU WON!") ? "The opponent kneeled under your might." : "No point continuing a losing game."; break;
            case BY_TIMEOUT: reason = result.equals("YOU WON!") ? "Slow deers get captured." : "Maybe you should think a bit faster?"; break;
            case BY_WINNING_MOVE: reason = result.equals("YOU WON!") ? "Oh, that was merciless." : "Next time mind your back a little, won't you?"; break;
        }
        notifBoard.appendText(String.format("%s %s\n", result, reason));
        resetGame();
        setPosition(position, gePacket.getReason() == GameEnd.ReasonType.BY_OPPONENT_LEFT);
    }

    public void handleDrawOffer() {
        notifBoard.appendText("DRAW OFFERED: Your opponent offered a draw. You can agree to the offer by clicking \"Agree to Offer\", or reject it by continuing making moves.\n");
        drawBtn.setText("Agree to Offer");
        drawBtn.setOnAction(e -> drawAgree());
    }
    
}
