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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

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
    private BorderPane boardContainer;
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
    private boolean interrupted = false;
    private long time;
    private StonePut tempSP;
    private Future<StonePut> futureSP;
    private Thread moveThread;
    private TimerTask timeUpdateSender;
    private final Timer timer = new Timer();
    private final long[] times = new long[2];
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
        
        gameTimeComboBox.getItems().addAll(IntStream.rangeClosed(2, 12).map(x -> x * 5).boxed().toArray(Integer[]::new));
        moveTimeComboBox.getItems().addAll(IntStream.rangeClosed(2, 12).map(x -> x * 5).boxed().toArray(Integer[]::new));
        sizeComboBox.getItems().addAll(IntStream.rangeClosed(9, 25).boxed().toArray(Integer[]::new));
        
        addHandlers();
    }
    
    private void addHandlers() {
        nameField.textProperty().addListener((v, o, n) -> fieldChanged());
        codeField.textProperty().addListener((v, o, n) -> fieldChanged());
        codeLabel.textProperty().addListener((v, o, n) -> copyBtn.setDisable(codeLabel.getText().isBlank()));
        gameTimingCheckBox.selectedProperty().addListener((v, o, n) -> gameTimingEnabled());
        moveTimingCheckBox.selectedProperty().addListener((v, o, n) -> moveTimingEnabled());
        parent.widthProperty().addListener((v, o, n) -> {
            boardContainer.setMinWidth(n.intValue() - 200);
            boardContainer.setMaxWidth(n.intValue() - 200);
        });
        parent.heightProperty().addListener((v, o, n) -> {
            boardContainer.setMinHeight(n.intValue() - 70);
            boardContainer.setMaxHeight(n.intValue() - 70);
        });
        boardContainer.widthProperty().addListener((v, o, n) -> resizeCanvas());
        boardContainer.heightProperty().addListener((v, o, n) -> resizeCanvas());
        
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
    
    private void resizeCanvas() {
        int canvasSize = (int) Math.min(boardContainer.getWidth(), boardContainer.getHeight()) - 12;
        if (boardContainer.getCenter() instanceof BoardPane) {
            ((BoardPane) boardContainer.getCenter()).setMinSize(canvasSize, canvasSize);
            ((BoardPane) boardContainer.getCenter()).setMaxSize(canvasSize, canvasSize);
        }
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
                hostMoveTimeLabel.setText("");
                guestMoveTimeLabel.setText("");
                break;
            case 1:
                if (clearBoard) {
                    ClientMain.getRoom().setGuest(null);
                    guestNameLabel.setText("");
                }
                loadTimeLabels();
                startBtn.setDisable(ClientMain.getRoom().getGuest() == null);
                break;
            case 2:
                startBtn.setDisable(true);
                loadTimeLabels();
                break;
        }
        gamePane.setDisable(p != 0);
        gameBtnPane.setDisable(p == 0);
        settingsPane.setDisable(p != 1);
        if (clearBoard) {
            board = null;
            boardContainer.setCenter(board);
        }
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
    
    private void switchStartBtn(boolean reset) {
        if (reset) {
            startBtn.setText("Reset Board");
            startBtn.setOnAction(e -> {
                updateSettings();
                switchStartBtn(false);
            });
        } else {
            startBtn.setText("Start Game");
            startBtn.setOnAction(e -> startGame());
        }
    }
    
    private Alert alert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(this.getClass().getClassLoader().getResource("edu/client/gui/resources/AppIcon.png").toExternalForm()));
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
        loadTimeLabels();
    }
    
    private void resetBoard() {
        board = new BoardPane(getSettings().getSize());
        boardContainer.setCenter(board);
        resizeCanvas();
        switchStartBtn(false);
    }
    
    private void loadTimeLabels() {
        if(getSettings().moveTimingEnabled()) {
            hostMoveTimeLabel.setText(getTimeString(getSettings().getMoveTimeMillis()));
            if (ClientMain.getRoom().getGuest() != null)
                guestMoveTimeLabel.setText(getTimeString(getSettings(). getMoveTimeMillis()));
            else guestMoveTimeLabel.setText("");
        } else {
            hostMoveTimeLabel.setText("∞");
            if (ClientMain.getRoom().getGuest() != null)
                guestMoveTimeLabel.setText("∞");
            else guestMoveTimeLabel.setText("");
        }
        if(getSettings().gameTimingEnabled()) {
            hostGameTimeLabel.setText(getTimeString(getSettings().getGameTimeMillis()));
            if (ClientMain.getRoom().getGuest() != null)
                guestGameTimeLabel.setText(getTimeString(getSettings().getGameTimeMillis()));
            else guestGameTimeLabel.setText("");
        } else {
            hostGameTimeLabel.setText("∞");
            if (ClientMain.getRoom().getGuest() != null)
                guestGameTimeLabel.setText("∞");
            else guestGameTimeLabel.setText("");
        }
    }
    
    private void resetMoveTimeLabels() {
        hostMoveTimeLabel.setText(getTimeString(getSettings().getMoveTimeMillis()));
        guestMoveTimeLabel.setText(getTimeString(getSettings(). getMoveTimeMillis()));
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
    
    private void moveTimeChanged(boolean host, long timeMillis) {
        if (host)
            hostMoveTimeLabel.setText(getTimeString(timeMillis));
        else
            guestMoveTimeLabel.setText(getTimeString(timeMillis));
    }

    private void gameTimeChanged(boolean host, long timeMillis) {
        if (host)
            hostGameTimeLabel.setText(getTimeString(timeMillis));
        else
            guestGameTimeLabel.setText(getTimeString(timeMillis));
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
        sendObject(new TurnStart());
        sendTimeUpdates(position == 1);
        int player = isBlack ? 1 : 2;
        board.enableStonePicker(player);
        turnLabel.setText(String.format("%s", position == 1 ? "\u25c0" : "\u25b6"));
        turnLabel.setTextFill(isBlack ? Color.WHITE : Color.BLACK);
        turnLabel.setBackground(new Background(new BackgroundFill(isBlack ? Color.BLACK : Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        
        moveThread = new Thread(getRunnable());
        moveThread.start();
    }
    
    private void offerDraw() {
        Alert alert = alert(AlertType.CONFIRMATION, "Confirm", "Do you really want to offer a draw?");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        ButtonType button = alert.showAndWait().get();
        if (button == ButtonType.YES) {
            sendObject(new OfferDraw());
            notifBoard.appendText("OFFER SENT: You sent an offer for a draw, but the match WILL CONTINUE until the opponent answers or being concluded in another way.\n");
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
    
    private void sendTimeUpdates(boolean host) {
        this.timeUpdateSender = new TimerTask() {
            long startTime = System.currentTimeMillis();
            long moveTime = getSettings().getMoveTimeMillis();
            long gameTime = times[host ? 0 : 1];
            @Override
            public void run() {
                Platform.runLater(() -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    gameTime -= elapsed;
                    moveTime -= elapsed;
                    if(getSettings().gameTimingEnabled()) {
                        gameTimeChanged(host, gameTime);
                    }
                    if(getSettings().moveTimingEnabled()) {
                        moveTimeChanged(host, moveTime);
                    }
                    startTime = System.currentTimeMillis();
                });
            }
        };
        timer.scheduleAtFixedRate(timeUpdateSender, 0, 100);
    }
    
    private void stopTimeUpdates() {
        timeUpdateSender.cancel();
    }
    
    private Runnable getRunnable() {
        return () -> {
            tempSP = null;
            interrupted = false;
            if (ClientMain.getRoom().getSettings().gameTimingEnabled() || ClientMain.getRoom().getSettings().moveTimingEnabled())
                time = System.currentTimeMillis();

            board.setOnMouseClicked((event) -> {
                if (ClientMain.getRoom().getSettings().gameTimingEnabled() || ClientMain.getRoom().getSettings().moveTimingEnabled())
                    time = System.currentTimeMillis() - time;
                int row = board.getClosestRow(event.getY());
                int col = board.getClosestCol(event.getX());
                if (!board.hasStoneAt(row, col)) {
                    if (ClientMain.getRoom().getSettings().gameTimingEnabled() || ClientMain.getRoom().getSettings().moveTimingEnabled())
                        tempSP = new StonePut(row, col, time);
                    else tempSP = new StonePut(row, col);
                    synchronized (getMoveThread()) {
                        getMoveThread().notify();
                    }
                }
            });

            futureSP = executor.submit(() -> getTempSP());
                
            try {    
                if (timeoutMillis() > 0)
                    sendObject(futureSP.get(timeoutMillis(), TimeUnit.MILLISECONDS));
                else
                    sendObject(futureSP.get());
                board.addStone(isBlack ? 1 : 2, tempSP.getX(), tempSP.getY(), false);
                notifBoard.appendText(String.format("Your move: %s\n", BoardPane.convertMoveAlgebraic(tempSP.getX(), tempSP.getY(), getSettings().getSize())));
            } catch (InterruptedException | ExecutionException | CancellationException ex) {
                interrupted = true;
            } catch (TimeoutException ex) {
                sendObject(new StonePut(-1, -1));
            } finally {
                board.setOnMouseClicked(null);
                //TODO: Stop the clock
                Platform.runLater(() -> {
                    stopTimeUpdates();
                    board.disableStonePicker();                    
                });
            }
            Platform.runLater(() -> {
                if (!interrupted) {
                    if (getSettings().gameTimingEnabled()) {
                        times[position - 1] -= time;
                        gameTimeChanged(position == 1, times[position - 1]);
                    }
                    if (getSettings().moveTimingEnabled())
                        resetMoveTimeLabels();
                    turnLabel.setText(String.format("%s", position == 1 ? "\u25b6" : "\u25c0"));
                    turnLabel.setTextFill(isBlack ? Color.BLACK : Color.WHITE);
                    turnLabel.setBackground(new Background(new BackgroundFill(isBlack ? Color.WHITE : Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
                    sendTimeUpdates(position == 2);
                }
                if ("Agree to Offer".equals(drawBtn.getText())) {
                    drawBtn.setText("Offer Draw");
                    drawBtn.setOnAction(e -> offerDraw());
                }
            });
            Thread.currentThread().interrupt();
        };
    }
    
    private Thread getMoveThread() {
        return moveThread;
    }
    
    private StonePut getTempSP() {
        synchronized (getMoveThread()) {
            try {
                getMoveThread().wait();
            } catch (InterruptedException ex) {
                return null;
            }
        }
        return tempSP;
    }
    
    private long timeoutMillis() {
        if(getSettings().moveTimingEnabled() && getSettings().gameTimingEnabled()) {
            return Math.min(getSettings().getMoveTimeMillis(), times[position - 1]);
        } else if(getSettings().gameTimingEnabled()) {
            return times[position - 1];
        } else if(getSettings().moveTimingEnabled()) {
            return getSettings().getMoveTimeMillis();
        } else {
            return 0;
        }
    }
    
    private String getTimeString(long millis) {
        return String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }
    
    private boolean settingIdentical() {
        return getSettings().getMoveTimeMillis() == TimeUnit.MILLISECONDS.convert(moveTimeComboBox.getValue(), TimeUnit.SECONDS)
            && getSettings().getGameTimeMillis() == TimeUnit.MILLISECONDS.convert(gameTimeComboBox.getValue(), TimeUnit.MINUTES)
            && getSettings().gameTimingEnabled() == gameTimingCheckBox.isSelected()
            && getSettings().moveTimingEnabled() == moveTimingCheckBox.isSelected()
            && getSettings().getSize() == sizeComboBox.getValue();
    }

    public void handleIDPacket(GameID idPacket) {
        Player host = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setRoomID(idPacket.getRoomID());
        room.setSettings(idPacket.getRuleSet().toGameSettings());
        ClientMain.setRoom(room);
        if (position == 2)
            notifBoard.appendText("Opponent has left. You are the host of this room now. The game code has changed.\n");
        setPosition((byte) 1, true);
        hostNameLabel.setText(host.getUsername());
        guestNameLabel.setText("");
        codeLabel.setText(room.getRoomID());
        loadSettings();
    }

    public void handleRuleChanges(RuleSet rsPacket) {
        if (!rsPacket.toGameSettings().equals(getSettings())) {
            ClientMain.getRoom().setSettings(rsPacket.toGameSettings());
            loadSettings();
            notifBoard.appendText("WARNING: Game rules changed.\n");
        } else
            notifBoard.appendText("Board reset.\n");
        resetBoard();
    }

    public void handleRuleConfirmed(ConfirmRules cfPacket) {
        if (cfPacket.isSuccessful()) {
            if (!settingIdentical()) {
                getSettings().setMoveTimeMillis(TimeUnit.MILLISECONDS.convert(moveTimeComboBox.getValue(), TimeUnit.SECONDS));
                getSettings().setMoveTimingEnabled(moveTimingCheckBox.isSelected());
                getSettings().setGameTimeMillis(TimeUnit.MILLISECONDS.convert(gameTimeComboBox.getValue(), TimeUnit.MINUTES));
                getSettings().setGameTimingEnabled(gameTimingCheckBox.isSelected());
                getSettings().setSize(sizeComboBox.getValue());
                loadTimeLabels();
                notifBoard.appendText("SUCCESS: Game rules changed successfully.\n");
            } else {
                notifBoard.appendText("Board reset.\n");
            }
            if (ClientMain.getRoom().getGuest() != null)
                resetBoard();
        } else {
            loadSettings();
            notifBoard.appendText("FAILED: Game rules failed to change.\n");
        }
        settingsPane.setDisable(false);
    }

    public void handleGuestFound(GuestFound gfPacket) {
        Player guest = new Player(gfPacket.getUsername(), null);
        ClientMain.getRoom().setGuest(guest);
        guestNameLabel.setText(guest.getUsername());
        loadTimeLabels();
        startBtn.setDisable(false);
        notifBoard.appendText("GUEST JOINED: User \"" + guest.getUsername() + "\" has joined.\n");
        resetBoard();
    }

    public void handleGameInfo(GameInfo giPacket) {
        Player host = new Player(giPacket.getHostUsername(), null);
        Player guest = new Player(nameField.getText(), null);
        Room room = new Room();
        room.setHost(host);
        room.setGuest(guest);
        room.setRoomID(codeField.getText());
        room.setSettings(giPacket.getRuleSet().toGameSettings());
        ClientMain.setRoom(room);
        setPosition((byte) 2, true);
        loadSettings();
        resetBoard();
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
        isBlack = gsPacket.isHostMoveFirst() ? position == 1 : position == 2;
        setInGame(true);
        notifBoard.appendText(String.format("GAME STARTED: %s will move first.\n", isBlack ? "you" : "opponent"));
        if (getSettings().gameTimingEnabled()) {
            times[0] = getSettings().getGameTimeMillis();
            times[1] = getSettings().getGameTimeMillis();
        }
        loadTimeLabels();
        drawBtn.setOnAction(e -> offerDraw());
        if (isBlack)
            requestMove();
        else {
            turnLabel.setText(String.format("%s", position == 1 ? "\u25b6" : "\u25c0"));
            turnLabel.setTextFill(Color.WHITE);
            turnLabel.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
            sendTimeUpdates(position == 2);
        }
    }

    public void handleOpponentMove(StonePut spPacket) {
        stopTimeUpdates();
        if (getSettings().gameTimingEnabled()) {
            times[2 - position] -= spPacket.getTime();
            gameTimeChanged(position == 2, times[2 - position]);
        }
        if (getSettings().moveTimingEnabled())
            resetMoveTimeLabels();
        if (drawOfferSent) {
            notifBoard.appendText("REJECTED: Opponent rejected your draw offer. Game continues.\n");
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
        stopTimeUpdates();
        if (futureSP != null)
            if (!futureSP.isDone())
                futureSP.cancel(true);
        if (moveThread != null)
            if (moveThread.isAlive()) {
                moveThread.interrupt();
                try {
                    moveThread.join();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        resetGame();
        String result = gePacket.getEndingType() == GameEnd.EndingType.DRAW
                ? "IT'S A DRAW!" : (gePacket.getEndingType() == GameEnd.EndingType.HOST_WON && position == 1) || (gePacket.getEndingType() == GameEnd.EndingType.GUEST_WON && position == 2)
                ? "YOU WON!" : "YOU LOST!";
        String reason = "";
        switch (gePacket.getReason()) {
            case BY_BOARD_FULL: reason = "There's no space left to move."; break;
            case BY_AGREEMENT: reason = "You both realized no one could win."; break;
            case BY_OPPONENT_LEFT: reason = result.equals("YOU WON!") ? "The opponent cowardly ran away." : "But forfeiting when needed is a great strategem."; break;
            case BY_OPPONENT_SURRENDER: reason = result.equals("YOU WON!") ? "The opponent kneeled under your might." : "No point continuing a losing game."; break;
            case BY_TIMEOUT: reason = result.equals("YOU WON!") ? "Slow deers get captured." : "Maybe you should think a bit faster?"; break;
            case BY_WINNING_MOVE: reason = result.equals("YOU WON!") ? "Oh, that was merciless." : "Next time mind your back a little, won't you?"; break;
        }
        notifBoard.appendText(String.format("%s %s\n", result, reason));
        setPosition(position, gePacket.getReason() == GameEnd.ReasonType.BY_OPPONENT_LEFT);
        if (position == 1)
            switchStartBtn(true);
    }

    public void handleDrawOffer() {
        notifBoard.appendText("DRAW OFFERED: Your opponent offered a draw. You can agree to the offer by clicking \"Agree to Offer\", or reject it by continuing making moves.\n");
        drawBtn.setText("Agree to Offer");
        drawBtn.setOnAction(e -> drawAgree());
    }
    
}
