package com.jlreyes.libraries.android_game_engine.rendering.renderable;

public class Rectangle extends Polygon {
    private float mWidth;
    private float mHeight;

    public Rectangle(float width, float height) {
        super(new float[]{
                -width / 2.0f, -height / 2.0f,
                -width / 2.0f, height / 2.0f,
                width / 2.0f, -height / 2.0f,
                width / 2.0f, height / 2.0f
        });
        this.mWidth = width;
        this.mHeight = height;
    }
}
