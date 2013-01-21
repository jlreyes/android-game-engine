package com.jlreyes.libraries.android_game_engine.utils.math;

import android.util.FloatMath;

public class FloatMathUtils {
    public static final float FLOAT_PI = (float) Math.PI;
    /**
     * Float epsilon to use if none is provided
     */
    public static final float FLOAT_EPSILON = 0.001f;

    public static float PythagRFromXY(float x, float y) {
        return FloatMath.sqrt(x * x + y * y);
    }

    public static float AngleFromCircleArcLength(float distance,
                                                 float radius) {
        return (float) ((Math.PI / 2.0) - (180.0 * distance) / (Math.PI * radius));
    }

    /**
     * @param degrees Degree in degrees.
     * @return The equivalent degree in radians
     */
    public static float DegreesToRadians(float degrees) {
        return degrees / 180.0f * FLOAT_PI;
    }

    public static float RadiansToDegrees(float radians) {
        return (radians * 180f) / FLOAT_PI;
    }

    /**
     * @return true iff |f1 - f2| <= epsilon
     */
    public static boolean FloatsEqual(float epsilon, float f1, float f2) {
        return (Math.abs(f1 - f2) <= epsilon);
    }
}
