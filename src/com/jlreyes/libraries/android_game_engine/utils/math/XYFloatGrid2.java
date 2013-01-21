package com.jlreyes.libraries.android_game_engine.utils.math;

/**
 * Represents a 2d Cartesian plane of type float
 *
 * @author jlreyes
 */
public class XYFloatGrid2 {
    private float mLeft;
    private float mRight;
    private float mBottom;
    private float mTop;

    /**
     * Creates a new float grid.
     */
    public XYFloatGrid2(float left, float right, float bottom, float top) {
        this.mLeft = left;
        this.mRight = right;
        this.mBottom = bottom;
        this.mTop = top;
    }

    public boolean isIn(float x, float y) {
        return (mLeft <= x && x <= mRight) && (mBottom <= y && y <= mTop);
    }

	/*
     * Getters and Setters
	 */

    public float getLeft() {
        return mLeft;
    }

    public void setLeft(float mLeft) {
        this.mLeft = mLeft;
    }

    public float getRight() {
        return mRight;
    }

    public float getBottom() {
        return mBottom;
    }

    public void setBottom(float mBottom) {
        this.mBottom = mBottom;
    }

    public void setRight(float mRight) {
        this.mRight = mRight;
    }

    public float getTop() {
        return mTop;
    }

    public void setTop(float mTop) {
        this.mTop = mTop;
    }
}
