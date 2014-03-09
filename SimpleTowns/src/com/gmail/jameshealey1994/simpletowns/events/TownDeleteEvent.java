package com.gmail.jameshealey1994.simpletowns.events;

import com.gmail.jameshealey1994.simpletowns.object.Town;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Town Delete Event.
 * Triggered when a town is about to be deleted.
 *
 * @author JamesHealey94 <jameshealey1994.gmail.com>
 */
public class TownDeleteEvent extends Event implements Cancellable {

    /**
     * List of event handlers.
     */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Town about to be deleted.
     */
    private final Town town;

    /**
     * If the event is cancelled.
     */
    private boolean cancelled;

    /**
     * Constructor - Sets the town about to be deleted.
     *
     * @param town  town about to be deleted
     */
    public TownDeleteEvent(Town town) {
        this.town = town;
    }

    /**
     * Returns list of event handlers.
     *
     * @return  list of event handlers
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Returns the town about to be deleted.
     *
     * @return  town about to be deleted
     */
    public Town getTown() {
        return town;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}