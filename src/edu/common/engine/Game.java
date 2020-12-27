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
    private final boolean hostMoveFirst;
    private final GameSettings settings;
    private final ExecutorService executor;
    private Player[] players;
    private final long[] times;
    private final Timer timer;
    private Future<Move> futureMove;
    private Thread gameThread;
    private TimerTask timeUpdateSender;
    private GameState state;
    private final Room room;

    public Game(GameSettings gameSettings, Room room) {
        this.settings = gameSettings;
        this.room = room;
        this.times = new long[2];
        this.players = new Player[2];
        this.executor = Executors.newSingleThreadExecutor();
        this.gameThread = new Thread(getRunnable());
        this.timer = new Timer();
        this.state = new GameState(settings.getSize());
        this.hostMoveFirst = ThreadLocalRandom.current().nextBoolean();
    }

    public void start() {
        if(!this.gameThread.isAlive()) {
            this.state = new GameState(settings.getSize());
            times[0] = settings.getGameTimeMillis();
            times[1] = settings.getGameTimeMillis();
            this.gameThread = new Thread(getRunnable());
            this.gameThread.start();
        }
    }

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
            //Turn this on when setting clock
            //timeUpdateSender.cancel();
        }
    }

    public GameSettings getSettings() {
        return settings;
    }

//    public void addListener(GameListener listener) {
//        this.listeners.add(listener);
//    }

    private Move requestMove(int playerIndex, Move lastMove) throws
            InterruptedException, ExecutionException, TimeoutException {
        Player player = players[playerIndex - 1];
        long timeout = calculateTimeoutMillis(playerIndex);
        this.futureMove = executor.submit(() -> player.getMove(state));
        if (lastMove != null)
            player.getConnection().sendObject(new StonePut(lastMove.row,lastMove.col));

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

    private Runnable getRunnable() {
        return () -> {
            if(state.getMoves().isEmpty()) {
                GameStart startPacket = new GameStart(hostMoveFirst);
                this.players[0].getConnection().sendObject(startPacket);
                this.players[1].getConnection().sendObject(startPacket);
            }

            boolean timeout = false;

            while(state.terminal() == 0) {
                try {
                    long startTime = System.currentTimeMillis();

                    //Request move from current player
                    Move move;
                    if(state.getMoves().isEmpty())
                        move = requestMove(state.getCurrentIndex(), null);
                    else
                        move = requestMove(state.getCurrentIndex(), state.getLastMove());
                    
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    //decrease time game of current player
                    times[state.getCurrentIndex() - 1] -= elapsedTime;
                    state.makeMove(move);

                } catch (InterruptedException ex) {
//                    stopTimeUpdates();
                    return;
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
            if (players[0].getConnection() == null || players[1].getConnection() == null)
                return;
            players[state.getCurrentIndex() - 1].getConnection().sendObject(new StonePut(state.getLastMove().row, state.getLastMove().col));
            // Game end and send message to 2 client
            GameEnd gameEnd = new GameEnd();
            if (state.terminal() == 1 || state.terminal() == 2) {
                gameEnd.setEndingType((hostMoveFirst ? state.terminal() == 1 : state.terminal() == 2)
                        ? GameEnd.EndingType.HOST_WON : GameEnd.EndingType.GUEST_WON);
                gameEnd.setReason(GameEnd.ReasonType.BY_WINNING_MOVE);
            }else if (state.terminal() == 3){
                gameEnd.setEndingType(GameEnd.EndingType.DRAW);
                gameEnd.setReason(GameEnd.ReasonType.BY_BOARD_FULL);
            }else if (timeout) {
                gameEnd.setEndingType((hostMoveFirst ? state.getCurrentIndex() == 1 : state.getCurrentIndex() == 2)
                        ? GameEnd.EndingType.GUEST_WON : GameEnd.EndingType.HOST_WON);
                gameEnd.setReason(GameEnd.ReasonType.BY_TIMEOUT);
            }
            players[0].getConnection().sendObject(gameEnd);
            players[1].getConnection().sendObject(gameEnd);
            room.removeGame();
        };
    }

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

    public boolean isHostMoveFirst() {
        return hostMoveFirst;
    }
}
