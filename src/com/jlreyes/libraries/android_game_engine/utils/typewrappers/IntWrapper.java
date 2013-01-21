package com.jlreyes.libraries.android_game_engine.utils.typewrappers;

/**
 * Wraps a integer strictly and without allocation.
 *
 * @author jlreyes
 */
public class IntWrapper implements Wrapper {
    private int mI;

    public IntWrapper(int i) {
        this.mI = i;
    }

    public void setInt(int i) {
        this.mI = i;
    }

    public int getInt() {
        return mI;
    }

    public void decrement() {
        mI -= 1;
    }

    public void decrement(int i) {
        mI -= i;
    }

    public void increment() {
        mI += 1;
    }

    public void increment(int i) {
        mI += i;
    }

    public boolean isZero() {
        return (0 == mI);
    }
}
