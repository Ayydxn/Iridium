package me.ayydan.iridium.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface WindowResizeEvent
{
    Event<WindowResizeEvent> EVENT = EventFactory.createLoop();

    void onWindowResize(int newWindowWidth, int newWindowHeight);
}
