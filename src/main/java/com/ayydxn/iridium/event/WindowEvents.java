package com.ayydxn.iridium.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class WindowEvents
{
    public static final Event<Resize> RESIZE = EventFactory.createArrayBacked(Resize.class, (listeners) -> (newWidth, newHeight) ->
    {
        for (Resize listener : listeners)
            listener.onWindowResize(newWidth, newHeight);
    });

    @FunctionalInterface
    public interface Resize
    {
        void onWindowResize(int newWidth, int newHeight);
    }
}
