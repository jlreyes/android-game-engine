package com.jlreyes.libraries.android_game_engine.sprites.sprites;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;

/**
 * User: James
 * Date: 1/15/13
 * Time: 8:16 PM
 */
public class IndirectCamera extends GameCamera {
    private Layer mWatchLayer;

    /**
     * Create a camera for dstLayer that mimics srcLayer.
     */
    public IndirectCamera(String name, Layer dstLayer, Layer srcLayer) {
        super(name, dstLayer, null);
        this.mWatchLayer = srcLayer;
    }

    public LayerCamera getInstance() {
        return mWatchLayer.getCamera();
    }

    @Override
    public void update(long deltaTime) {
        throw new IllegalStateException();
    }

    @Override
    public boolean isTouching(float x, float y) {
        return false;
    }

    @Override
    public boolean onTouchDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchCancel(MotionEvent e) {}

    @Override
    public boolean onTouchUp(MotionEvent e) {
        return false;
    }
}
