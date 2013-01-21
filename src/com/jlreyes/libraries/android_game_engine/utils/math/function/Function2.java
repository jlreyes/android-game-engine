package com.jlreyes.libraries.android_game_engine.utils.math.function;

/**
 * @param <A1> Argument 1's type
 * @param <A2> Argument 2's type
 * @param <R> Return type
 */
public interface Function2<A1, A2, R> {
    public R run(A1 a1, A2 a2);
}