package com.mrcrayfish.controllable.event;

import net.minecraftforge.eventbus.api.Event;

public class ControllerEvent extends Event {
    public static class Update extends ControllerEvent {}
    public static class Button extends ControllerEvent {
        public int getButton() { return 0; }
        public boolean isPressed() { return false; }
    }
}
