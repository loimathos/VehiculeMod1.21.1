package com.mrcrayfish.controllable;

public class Controllable {
    public static Object getController() { return null; }
    public static boolean isButtonPressed(int button) { return false; }
    public static Input getInput() { return new Input(); }
    public static class Input {
        public boolean isControllerInUse() { return false; }
    }
}
