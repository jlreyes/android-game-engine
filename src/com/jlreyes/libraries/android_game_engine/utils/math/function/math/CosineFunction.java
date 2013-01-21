package com.jlreyes.libraries.android_game_engine.utils.math.function.math;

import android.util.FloatMath;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;
import com.jlreyes.libraries.android_game_engine.utils.typewrappers.FloatWrapper;

public class CosineFunction implements Function1<FloatWrapper, Void> {
    private float mMagnitude;
    private float mHalfPeriod;
    private float mA;
    private FloatWrapper mFloatWrapper;

    public CosineFunction(float magnitude, float halfPeriod) {
        this.mMagnitude = magnitude;
        this.mHalfPeriod = halfPeriod;
        this.mA = (float) (Math.PI / (double) halfPeriod);
        this.mFloatWrapper = new FloatWrapper(0);
    }

    public Void run(FloatWrapper d) {
        float x = d.getFloat();
        float y = mMagnitude * FloatMath.cos(mA * x);
        d.setFloat(y);
        return null;
    }

    public float eval(float x) {
        mFloatWrapper.setFloat(x);
        run(mFloatWrapper);
        return mFloatWrapper.getFloat();
    }

    public float getHalfPeriod() {
        return mHalfPeriod;
    }
}
