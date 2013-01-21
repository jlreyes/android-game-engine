package com.jlreyes.libraries.android_game_engine.utils.typewrappers;

/**
 * Wraps a float strictly and without allocation.
 *
 * @author jlreyes
 */
public class FloatWrapper implements Wrapper {
    private float mF;

    public FloatWrapper(float f) {
        this.mF = f;
    }

    public void setFloat(float f) {
        this.mF = f;
    }

    public float getFloat() {
        return mF;
    }

    public void increment() {
        mF += 1.0f;
    }

    public void decrement() {
        mF -= 1.0f;
    }

    public boolean isZero() {
        return mF == 0.0f;
    }
}
