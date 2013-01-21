package com.jlreyes.libraries.android_game_engine.utils.exceptions;

public class MissingFileException extends Exception {
    private static final long serialVersionUID = 4323654931284220168L;

    public MissingFileException() {
        super();
    }

    public MissingFileException(String string) {
        super(string);
    }
}
