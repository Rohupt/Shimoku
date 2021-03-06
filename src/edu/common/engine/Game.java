package edu.common.engine;

import edu.common.packet.server.GameEnd;
import edu.common.packet.StonePut;
import edu.common.packet.server.GameStart;

import java.util.concurrent.*;

public class Game {

    private final boolean hostMoveFirst;
    private final GameSettings settings;
    private final ExecutorService executor;
    private Player[] players;
    private final long[] times;
    private Future<StonePut> futureSpPacket;
    private final Thread gameThread;
    private GameState state;
    private final Room room;
    private boolean ready;

    public Game(GameSettings gameSettings, Room room) {
        this.settings = gameSettings;
        this.room = room;
        this.times = new long[2];
        this.players = new Player[2];
        this.executor = Executors.newSingleThreadExecutor();
        this.gameThread = new Thread(getRunnable());
        this.state = new GameState(settings.getSize());
        this.hostMoveFirst = ThreadLocalRandom.current().nextBoolean();
    }

    public void start() {
        if(!this.gameThread.isAlive()) {
            this.state = new GameState(settings.getSize());
            times[0] = settings.getGameTimeMillis();
            times[1] = settings.getGameTimeMillis();
            System.out.println("\t\tGame thread started: " + room.getRoomID());
            ready = false;
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
            if (futureSpPacket != null)
                if(!futureSpPacket.isDone()) {
                    futureSpPacket.cancel(true);
                }
            System.out.println("\t\tGame thread stopped: ");
        }
    }

    public GameSettings getSettings() {
        return settings;
    }

    private StonePut requestSpPacket(int playerIndex) throws InterruptedException, ExecutionException, TimeoutException {
        Player player = players[playerIndex - 1];
        long timeout = calculateTimeoutMillis(playerIndex);
        this.futureSpPacket = executor.submit(() -> player.getSpPacket()); System.out.println("\t\tWaiting for player move");

        if (timeout > 0) {
            try {
                return futureSpPacket.get(timeout, TimeUnit.MILLISECONDS);
            } catch(TimeoutException ex) {
                futureSpPacket.cancel(true);
                throw(ex);
            }
        } else {
            return futureSpPacket.get();
        }
    }
    
    public boolean setUserSpPacket(StonePut spPacket) {
        Player currentPlayer = players[state.getCurrentIndex() - 1];
        if(!state.getMoves().contains(new Move(spPacket.getX(), spPacket.getY()))) {
            synchronized(currentPlayer) {
                currentPlayer.setSpPacket(spPacket);
                players[state.getCurrentIndex() - 1].notify();
            }
            return true;
        }
        return false;
    }

    private long calculateTimeoutMillis(int player) {
        if(settings.moveTimingEnabled() && settings.gameTimingEnabled()) {
            return Math.min(settings.getMoveTimeMillis(), times[player - 1]);
        } else if(settings.gameTimingEnabled()) {
            return times[player - 1];
        } else if(settings.moveTimingEnabled()) {
            return settings.getMoveTimeMillis();
        } else {
            return 0;
        }
    }

    private Runnable getRunnable() {
        return () -> {
            if(state.getMoves().isEmpty()) {
                GameStart startPacket = new GameStart(hostMoveFirst);
                this.players[0].getConnection().sendMessage(startPacket);
                this.players[1].getConnection().sendMessage(startPacket);
            }

            boolean timeout = false;

            while(state.terminal() == 0) {
                try {
                    if (!timeout)
                        synchronized (Thread.currentThread()) {
                            ready = true;
                            System.out.println("\t\tWaiting for turn start signal");
                            synchronized (this.players[state.getCurrentIndex() - 1].getConnection().getListener()) {
                                this.players[state.getCurrentIndex() - 1].getConnection().getListener().notify();
                            }
                            Thread.currentThread().wait();
                        }
                    System.out.println("\t\tContinuing.");
                    long startTime = System.currentTimeMillis();
                    StonePut spPacket = requestSpPacket(state.getCurrentIndex());
                    System.out.println("\t\tMove packet received.");
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (spPacket.timeOut() || (timeout && Math.abs(elapsedTime + calculateTimeoutMillis(state.getCurrentIndex()) - spPacket.getTime()) >= 500)) {
                        timeout = true;
                        break;
                    } else {
                        if (timeout || Math.abs(elapsedTime - spPacket.getTime()) >= 100)
                            elapsedTime = spPacket.getTime();
                        times[state.getCurrentIndex() - 1] -= elapsedTime;
                        state.makeMove(new Move(spPacket.getX(), spPacket.getY()));
                        players[state.getCurrentIndex() - 1].getConnection().sendMessage(spPacket);
                    }
                    ready = false;
                } catch (InterruptedException ex) {
                    return;
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                    break;
                } catch (TimeoutException ex) {
                    timeout = true;
                }
            }
            if (players[0].getConnection() != null && players[1].getConnection() != null) {
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
                players[0].getConnection().sendMessage(gameEnd);
                players[1].getConnection().sendMessage(gameEnd);
            }
            room.removeGame();
        };
    }
    
    public void resumeTurn() {
        synchronized (this.gameThread) {
            this.gameThread.notify();
        }
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

    public boolean isReady() {
        return ready;
    }
}
