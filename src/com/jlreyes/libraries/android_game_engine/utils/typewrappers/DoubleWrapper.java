package com.jlreyes.libraries.android_game_engine.utils.typewrappers;

/**
 * Wraps a double strictly and without allocation.
 *
 * @author jlreyes
 */
public class DoubleWrapper implements Wrapper {
    private double mD;

    public DoubleWrapper(double d) {
        this.mD = d;
    }

    public void setDouble(double d) {
        this.mD = d;
    }

    public double getDouble() {
        return mD;
    }

    public void increment() {
        mD += 1.0;
    }

    public void decrement() {
        mD -= 1.0;
    }

    public boolean isZero() {
        return mD == 0.0;
    }
}
