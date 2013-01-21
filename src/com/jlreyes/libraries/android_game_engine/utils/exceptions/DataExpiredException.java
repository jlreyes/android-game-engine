package com.jlreyes.libraries.android_game_engine.utils.exceptions;

public class DataExpiredException extends Exception {
    private static final long serialVersionUID = -806749705580668668L;

    public DataExpiredException(String string) {
        super(string);
    }

    public DataExpiredException() {
        super();
    }
}
