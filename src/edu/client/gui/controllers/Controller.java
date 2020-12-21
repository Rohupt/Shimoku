package edu.client.gui.controllers;
/**
 * Created by Doston Hamrakulov
 */

import edu.client.EventListener;

public abstract class Controller {
    private EventListener listener;

    public EventListener getListener() {
        return listener;
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    void initialise(EventListener listener) { };
}
