package com.jlreyes.libraries.android_game_engine.rendering.renderable;

import android.opengl.Matrix;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera.AnchorPoint;

/**
 * A Renderable that updates the model, view, and projection matrices.
 *
 * @author jlreyes
 */
public class LayerCameraRenderable extends Renderable {
    private AnchorPoint mAnchorPoint;
    private float mWidth;
    private float mHeight;

    /* z position of the eye */
    public static final float EYE_Z = -1.0f;
    /* z position we are looking at */
    public static final float CENTER_Z = 0.0f;
    /* Up Vector */
    public static final float UP_X = 0.0f;
    public static final float UP_Y = 1.0f;
    public static final float UP_Z = 0.0f;
    /* Z Near and Far */
    public static final float NEAR = 1.0f;
    public static final float FAR = -1.0f;

    public LayerCameraRenderable() {
        super();
        this.mAnchorPoint = AnchorPoint.CENTER;
        this.mWidth = 2.0f;
        this.mHeight = 2.0f;
    }

    @Override
    public boolean isVisible(LayerCamera gameCamera) {
        return true;
    }

    @Override
    public void draw(int positionhandle,
                     int mvpMatrixHandle,
                     int texCoordHandle,
                     int texRGBHandle,
                     int texHandle,
                     float ratio,
                     float[] mvpMatrix,
                     float[] viewMatrix,
                     float[] modelMatrix,
                     float[] projectionMatrix) {
        float x = Float.NaN;
        float y = Float.NaN;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT) {
            x = getXPos();
            y = getYPos();
        } else if (mAnchorPoint == AnchorPoint.CENTER) {
            x = getXPos() - (mWidth / 2.0f);
            y = getYPos() - (mHeight / 2.0f);
        }
        Matrix.orthoM(projectionMatrix, 0,
                      0, mWidth,
                      0, mHeight,
                      NEAR, FAR);
        Matrix.setLookAtM(viewMatrix, 0,
                          -x, y, EYE_Z,
                          -x, y, CENTER_Z,
                          UP_X, UP_Y, UP_Z);
    }

    @Override
    public Renderable copy() {
        return new LayerCameraRenderable();
    }

    /*
     * Getters and Setters
     */
    public void setWidth(float width) {
        mWidth = width;
    }

    public void setHeight(float height) {
        mHeight = height;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setAnchorPoint(AnchorPoint anchorPoint) {
        mAnchorPoint = anchorPoint;
    }
}
