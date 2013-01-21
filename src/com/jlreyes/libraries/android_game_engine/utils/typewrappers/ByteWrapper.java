package com.jlreyes.libraries.android_game_engine.utils.typewrappers;

/**
 * Wraps a byte strictly and without allocation.
 *
 * @author jlreyes
 */
public class ByteWrapper implements Wrapper {
    private byte mB;

    public ByteWrapper(byte b) {
        this.mB = b;
    }

    public void setByte(byte b) {
        this.mB = b;
    }

    public byte getByte() {
        return mB;
    }

    public void decrement() {
        mB -= 1;
    }

    public void decrement(byte b) {
        mB -= b;
    }

    public void increment() {
        mB += 1;
    }

    public void increment(byte b) {
        mB += b;
    }

    public boolean isZero() {
        return (0 == mB);
    }
}
