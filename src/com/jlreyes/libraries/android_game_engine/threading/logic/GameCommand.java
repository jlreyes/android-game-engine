package com.jlreyes.libraries.android_game_engine.threading.logic;

/**
 * Class where each object represents a command to send to the Logic Manager.
 *
 * @author jlreyes
 */
public class GameCommand {
    public enum Command {
        /** Load a new scene. Pass the scene info as the argument */
        LOAD,
        /** Kill the passed scene */
        KILL,
        /** Kill the passed scene and load the passed scene info's scene.
         * Pass as a tuple in the stated order. */
        LOAD_REPLACE,
        /** Not yet implemented */ // TODO
        SHOW_ERROR,
        /** Not yet implemented */ // TODO
        FATAL_ERROR}

    private Command mCommand;
    private Object mArgs;

    public GameCommand(Command c, Object args) {
        this.mCommand = c;
        this.mArgs = args;
    }

    /*
     * Getters and Setters
     */
    public Command getCommand() {
        return mCommand;
    }

    public Object getArgs() {
        return mArgs;
    }

}
