package com.jlreyes.libraries.android_game_engine.utils.exceptions;

public class DataStreamEndedEarlyException extends Exception {
    private static final long serialVersionUID = -4877324016064938344L;

    public DataStreamEndedEarlyException(String string) {
        super(string);
    }

    public DataStreamEndedEarlyException() {
        super();
    }
}
