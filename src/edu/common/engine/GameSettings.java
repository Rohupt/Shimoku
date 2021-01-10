package edu.common.engine;

public class GameSettings {

    private int size;
    private boolean gameTimingEnabled;
    private boolean moveTimingEnabled;
    private long gameTimeMillis;
    private long moveTimeMillis;

    public GameSettings() {
        this.gameTimingEnabled = false;
        this.moveTimingEnabled = false;
        this.gameTimeMillis = 1200000;
        this.moveTimeMillis = 15000;
        this.size = 19;
    }
    
    public GameSettings(GameSettings origin) {
        this.gameTimingEnabled = origin.gameTimingEnabled();
        this.moveTimingEnabled = origin.moveTimingEnabled();
        this.gameTimeMillis = origin.getGameTimeMillis();
        this.moveTimeMillis = origin.getMoveTimeMillis();
        this.size = origin.getSize();
    }
    
    public int getSize() {
        return this.size;
    }
    
    public void setSize(int size) {
        this.size = size;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    public boolean gameTimingEnabled() {
        return this.gameTimingEnabled;
    }

    public boolean moveTimingEnabled() {
        return this.moveTimingEnabled;
    }

    public void setGameTimingEnabled(boolean enabled) {
        this.gameTimingEnabled = enabled;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    public void setMoveTimingEnabled(boolean enabled) {
        this.moveTimingEnabled = enabled;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    public long getGameTimeMillis() {
        return this.gameTimeMillis;
    }

    public long getMoveTimeMillis() {
        return this.moveTimeMillis;
    }

    public void setGameTimeMillis(long millis) {
        this.gameTimeMillis = millis;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    public void setMoveTimeMillis(long millis) {
        this.moveTimeMillis = millis;
//        listeners.forEach(listener -> listener.settingsChanged());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final GameSettings other = (GameSettings) obj;
        if (this.size != other.size)
            return false;
        if (this.gameTimingEnabled != other.gameTimingEnabled)
            return false;
        if (this.moveTimingEnabled != other.moveTimingEnabled)
            return false;
        if (this.gameTimeMillis != other.gameTimeMillis)
            return false;
        return this.moveTimeMillis == other.moveTimeMillis;
    }

}
