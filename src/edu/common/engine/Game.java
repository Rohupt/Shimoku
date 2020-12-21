package edu.common.engine;

import edu.common.packet.GameEnd;
import edu.common.packet.StonePut;
import edu.common.packet.server.GameStart;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * Main game loop responsible for running the game from start to finish.
 */
public class Game {

//    private final List<GameListener> listeners;
    private final GameSettings settings;
    private final ExecutorService executor;
    private Player[] players;
    private final long[] times;
    private final Timer timer;
    private Future<Move> futureMove;
    private Thread gameThread;
    private TimerTask timeUpdateSender;
    private GameState state;

    /**
     * Create a new game instance.
     */
    public Game() {
        this(new GameSettings());
    }
    
    public Game(GameSettings gameSettings) {
        this.settings = gameSettings;
        this.times = new long[2];
        this.players = new Player[2];
        this.executor = Executors.newSingleThreadExecutor();
        this.gameThread = new Thread(getRunnable());
        this.timer = new Timer();
        this.state = new GameState(settings.getSize());
    }

    /**
     * Start the game. Reads the game settings and launches a new game thread.
     * Has no effect if the game thread is already running.
     */
    public void start() {
        if(!this.gameThread.isAlive()) {
            this.state = new GameState(settings.getSize());
            times[0] = settings.getGameTimeMillis();
            times[1] = settings.getGameTimeMillis();
            this.gameThread = new Thread(getRunnable());
            this.gameThread.start();
        }
    }

    /**
     * Stop the game. Safely interrupts the thread and cancels any pending
     * moves and calls join() to wait for the thread to resolve. Has no
     * effect if the game thread is not running.
     */
    public void stop() {
        if(this.gameThread.isAlive()) {
            this.gameThread.interrupt();
            try {
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!futureMove.isDone()) {
                futureMove.cancel(true);
            }
            timeUpdateSender.cancel();
        }
    }

    /**
     * Get the game settings.
     * @retun GameSettings instance
     */
    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Register a listener with this game instance.
     * @param listener GameListener to register
     */
//    public void addListener(GameListener listener) {
//        this.listeners.add(listener);
//    }

    /**
     * Request a move from a player.
     * @return Players move
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    private Move requestMove(int playerIndex, Move lastMove) throws
            InterruptedException, ExecutionException, TimeoutException {
        Player player = players[playerIndex - 1];
        long timeout = calculateTimeoutMillis(playerIndex);
        this.futureMove = executor.submit(() -> player.getMove(state));

//      Send an object to Client
        StonePut movePacket = new StonePut(lastMove.row,lastMove.col);
        player.getConnection().sendObject(movePacket);

        if (timeout > 0) {
            try {
                return futureMove.get(timeout, TimeUnit.MILLISECONDS);
            } catch(TimeoutException ex) {
                futureMove.cancel(true);
                throw(ex);
            }
        } else {
            return futureMove.get();
        }


    }

    /**
     * Called by the GUI to set a user's move for the game.
     * @param move Move from the user
     * @return True if the move was accepted
     */
    public boolean setUserMove(Move move) {
        Player currentPlayer = players[state.getCurrentIndex() - 1];
        if(!state.getMoves().contains(move)) {
            synchronized(currentPlayer) {
                currentPlayer.setMove(move);
                players[state.getCurrentIndex() - 1].notify();
            }
            return true;
        }
        return false;
    }

    /**
     * Calculate the timeout value for a player or return 0 if timing is not
     * enabled for this game.
     * @param player Player index
     * @return Timeout value in milliseconds
     */
    private long calculateTimeoutMillis(int player) {
        if(settings.moveTimingEnabled() && settings.gameTimingEnabled()) {
            // Both move timing and game timing are enabled
            return Math.min(settings.getMoveTimeMillis(), times[player - 1]);
        } else if(settings.gameTimingEnabled()) {
            // Only game timing is enabled
            return times[player - 1];
        } else if(settings.moveTimingEnabled()) {
            // Only move timing is enabled
            return settings.getMoveTimeMillis();
        } else {
            // No timing is enabled
            return 0;
        }
    }

    /**
     * Get the runnable game loop for this game.
     */
    private Runnable getRunnable() {
        return () -> {
            if(state.getMoves().isEmpty()) {
                GameStart startPacket = new GameStart();
                this.players[0].getConnection().sendObject(startPacket);
                this.players[1].getConnection().sendObject(startPacket);
            }

            boolean timeout = false;

            while(state.terminal() == 0) {
                try {
                    long startTime = System.currentTimeMillis();

                    //Request move from current player
                    Move move;
                    if(state.getMoves().isEmpty()){
                        Move initMove = new Move(-1,-1);
                        move = requestMove(state.getCurrentIndex(),initMove);
                    }else{
                        move = requestMove(state.getCurrentIndex(),state.getLastMove());
                    }

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    //decrease time game of current player
                    times[state.getCurrentIndex() - 1] -= elapsedTime;

                    state.makeMove(move);

                } catch (InterruptedException ex) {
//                    stopTimeUpdates();
                    break;
                } catch (ExecutionException ex) {
//                    stopTimeUpdates();
                    ex.printStackTrace();
                    break;
                } catch (TimeoutException ex) {
//                    stopTimeUpdates();
//                    LOGGER.log(Level.INFO, timeout(state.getCurrentIndex()));
//                    Xu ly truong hop timeout cho tung buoc di o day
                    timeout = true;
                    break;
                }
            }

            // Game end and send message to 2 client
            GameEnd gameEnd = new GameEnd();
            if(state.terminal() == 1) {
                gameEnd.setEndingType(GameEnd.EndingType.HOST_WON);
                gameEnd.setReason(GameEnd.ReasonType.BY_WINNING_MOVE);
            }else if(state.terminal() == 2){
                gameEnd.setEndingType(GameEnd.EndingType.GUEST_WON);
                gameEnd.setReason(GameEnd.ReasonType.BY_WINNING_MOVE);
            }else if(state.terminal() == 3){
                gameEnd.setEndingType(GameEnd.EndingType.DRAW);
                gameEnd.setReason(GameEnd.ReasonType.BY_BOARD_FULL);
            }else if(timeout){
                if(state.getCurrentIndex() == 1)
                    gameEnd.setEndingType(GameEnd.EndingType.GUEST_WON);
                else
                    gameEnd.setEndingType(GameEnd.EndingType.HOST_WON);
                gameEnd.setReason(GameEnd.ReasonType.BY_TIMEOUT);
            }
            players[0].getConnection().sendObject(gameEnd);
            players[1].getConnection().sendObject(gameEnd);
        };
    }

    /**
     * Start sending time events to listeners.
     * @param playerIndex Player index to send times for
     */
    private void sendTimeUpdates(int playerIndex) {
        this.timeUpdateSender = new TimerTask() {
            long startTime = System.currentTimeMillis();
            long moveTime = settings.getMoveTimeMillis();
            long gameTime = times[playerIndex - 1];
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                gameTime -= elapsed;
                moveTime -= elapsed;
                // Broadcast the elapsed times since the last TimerTask
                if(settings.gameTimingEnabled()) {
//                    listeners.forEach(listener -> listener.gameTimeChanged
//                            (playerIndex, gameTime));V
                }
                if(settings.moveTimingEnabled()) {
//                    listeners.forEach(listener -> listener.moveTimeChanged
//                            (playerIndex, moveTime));
                }
                startTime = System.currentTimeMillis();
            }
        };
        timer.scheduleAtFixedRate(timeUpdateSender, 0, 100);
    }

    /**
     * Stop sending time updates.
     */
    private void stopTimeUpdates() {
        timeUpdateSender.cancel();
    }

    private static String gameOver(int index) {
        return String.format("Game over, winner: Player %d.", index);
    }

    private static String timeout(int index) {
        return String.format("Player %d ran out of time.", index);
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }
    
    public void setPlayer1(Player player) {
        this.players[0] = player;
    }
    
    public void setPlayer2(Player player) {
        this.players[1] = player;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public boolean checkAlive(){
        return this.gameThread.isAlive();
    }
}
