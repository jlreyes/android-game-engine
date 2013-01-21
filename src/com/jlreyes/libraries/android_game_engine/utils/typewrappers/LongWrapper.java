package com.jlreyes.libraries.android_game_engine.utils.typewrappers;

/**
 * Wraps a long strictly and without allocation.
 *
 * @author jlreyes
 */
public class LongWrapper implements Wrapper {
    private long mL;

    public LongWrapper(long l) {
        this.mL = l;
    }

    public void setLong(long l) {
        this.mL = l;
    }

    public long getLong() {
        return mL;
    }

    public void decrement() {
        mL -= 1;
    }

    public void decrement(long l) {
        mL -= l;
    }

    public void increment() {
        mL += 1;
    }

    public void increment(long l) {
        mL += l;
    }

    public boolean isZero() {
        return (0 == mL);
    }
}
