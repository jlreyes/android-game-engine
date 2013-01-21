package com.jlreyes.libraries.android_game_engine.utils.exceptions;

public class FileCorruptedException extends Exception {
    private static final long serialVersionUID = -6594702433969655464L;

    public FileCorruptedException() {
        super();
    }

    public FileCorruptedException(String message) {
        super(message);
    }
}
