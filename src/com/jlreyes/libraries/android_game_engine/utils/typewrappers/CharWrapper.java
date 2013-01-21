package com.jlreyes.libraries.android_game_engine.utils.typewrappers;

/**
 * Wraps a char strictly and without allocation.
 *
 * @author jlreyes
 */
public class CharWrapper implements Wrapper {
    private char mC;

    public CharWrapper(char c) {
        this.mC = c;
    }

    public void setChar(char c) {
        this.mC = c;
    }

    public char getChar() {
        return mC;
    }

    public void decrement() {
        mC -= 1;
    }

    public void decrement(char c) {
        mC -= c;
    }

    public void increment() {
        mC += 1;
    }

    public void increment(char c) {
        mC += c;
    }

    public boolean isZero() {
        return (0 == mC);
    }
}
