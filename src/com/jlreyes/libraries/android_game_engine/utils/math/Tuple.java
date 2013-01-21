package com.jlreyes.libraries.android_game_engine.utils.math;

public class Tuple {
    private Object[] mElems;

    public Tuple(int capacity) {
        this.mElems = new Object[capacity];
    }

    public Tuple(Object... elems) {
        this.mElems = elems;
    }

    public void set(int i, Object o) {
        mElems[i] = o;
    }

    public Object get(int i) {
        return mElems[i];
    }

    public Object[] getElements() {
        return mElems;
    }
}
