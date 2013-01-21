package com.jlreyes.libraries.android_game_engine.sprites.sprites;

import android.opengl.Matrix;
import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.io.GameView;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.LayerCameraRenderable;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.Sprite;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

import java.util.ArrayList;

/**
 * Camera Sprite
 *
 * @author jlreyes
 */
public class LayerCamera extends GameCamera {
    public static enum AnchorPoint {CENTER, BOTTOM_LEFT}

    public static enum AspectType {WIDTH_PRIORITY, HEIGHT_PRIORITY}

    public static final String TAG = "LayerCamera";

    private float mDefaultDim;
    private GameView mGameView;
    private AnchorPoint mAnchorPoint;
    private AspectType mAspectType;
    private ArrayList<Function1<LayerCamera, Void>> mListeners;
    /* Bounding coordinates of the camera */
    private float mWidth;
    private float mHeight;

    public LayerCamera(String name, Layer layer, float defaultDim) {
        this(name, layer, defaultDim, Sprite.DEFAULT_X, Sprite.DEFAULT_Y,
             Sprite.DEFAULT_SCALE, Sprite.DEFAULT_ANGLE);
    }

    public LayerCamera(String name,
                       Layer layer,
                       float defaultDim,
                       float startX,
                       float startY,
                       float startScale,
                       float startAngle) {
        super(name, layer, new LayerCameraRenderable(),
              startX, startY, startScale, startAngle);
        this.mDefaultDim = defaultDim;
        this.mGameView = layer.getParentScene().getScheduler().getGameView();
        this.mListeners = new ArrayList<Function1<LayerCamera, Void>>();
        this.mAnchorPoint = AnchorPoint.CENTER;
        this.mAspectType = AspectType.WIDTH_PRIORITY;
        this.mWidth = 2.0f;
        this.mHeight = 2.0f;
    }

    @Override
    public void update(long deltaTime) {
        setBounds();
        executeListeners();
        checkBounds();
    }

    private void setBounds() {
        float scale = this.getScale();
        float ratio = this.getScreenRatio();
        float width = 0.0f;
        float height = 0.0f;
        if (this.mAspectType == AspectType.WIDTH_PRIORITY) {
            width = this.mDefaultDim;
            height = LayerCamera.getHeightFromRatioWidth(ratio, width);
        } else {
            height = this.mDefaultDim;
            width = LayerCamera.getWidthFromRatioHeight(ratio, height);
        }
        width *= 1.0f / scale;
        height *= 1.0f / scale;
        /* Storing calculated values */
        this.setWidth(width);
        this.setHeight(height);
    }

    public void executeListeners() {
        int length = this.mListeners.size();
        for (int i = 0; i < length; i++)
            this.mListeners.get(i).run(this);
    }

    /**
     * Makes sure we are in the bounds specified by the attached grid if
     * there is one. (TODO)
     */
    private void checkBounds() {            /* TODO: Keep camera within bounds */
			/*if (mTop > mGrid.getTop()) {
				float diff = mTop - mGridTop;
				mTop -= diff;
				mRight -= diff;
			}
			if (mBottom < mGrid.getBottom()) {
				float diff = mGridBottom - mBottom;
				mBottom -= diff;
				mRight -= diff;
			}
			if (mRight > mGridRight) {
				float diff = mRight - mGridRight;
				mRight -= diff;
				mTop -= diff;
			}
			if (mLeft < mGridRight) {
				float diff = mGridRight - mLeft;
				mLeft -= diff;
				mTop -= diff;
			}*/
    }

    @Override
    public LayerCameraRenderable onEndLogicStep(Thread thread) {
        LayerCameraRenderable renderable =
                (LayerCameraRenderable) super.onEndLogicStep(thread);
        renderable.setAnchorPoint(mAnchorPoint);
        renderable.setWidth(mWidth);
        renderable.setHeight(mHeight);
        return renderable;
    }

    /**
     * Returns true iff the given coordinates are visible by this camera.
     */
    public boolean isVisible(float x, float y) {
        float left = Float.NaN;
        float bottom = Float.NaN;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT) {
            left = getXLocation();
            bottom = getYLocation();
        } else if (mAnchorPoint == AnchorPoint.CENTER) {
            left = getXLocation() - mWidth / 2.0f;
            bottom = getYLocation() - mHeight / 2.0f;
        }
        float right = left + mWidth;
        float top = bottom + mHeight;
        if (left <= x && x <= right &&
            bottom <= y && y <= top) return true;
        else return false;
    }

    /**
     * Converts a given screen y pixel coordinate to its corresponding y
     * game coordinate.
     * @return The in-game coordinate.
     */
    public float screenPixelToGameCoordY(float y) {
		/* Fix y inversion */
        y = (float) mGameView.getHeight() - y;
        float screenPercent = y / (float) mGameView.getHeight();
        float gameY = Float.NaN;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT)
            gameY = screenPercent * mHeight + getYLocation();
        else if (mAnchorPoint == AnchorPoint.CENTER)
            gameY = screenPercent * mHeight + (getYLocation() - mHeight / 2.0f);
        return gameY;
    }

    /**
     * Converts a given screen x pixel coordinate to its corresponding x
     * game coordinate.
     * @return The in-game coordinate.
     */
    public float screenPixelToGameCoordX(float x) {
        float screenPercent = x / (float) mGameView.getWidth();
        float gameX = Float.NaN;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT)
            gameX = screenPercent * mWidth + getXLocation();
        else if (mAnchorPoint == AnchorPoint.CENTER)
            gameX = screenPercent * mWidth + (getXLocation() - mWidth / 2.0f);
        return gameX;
    }

    /*
     * Touch handlers. Touching the camera does nothing.
     */
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

    /**
     * Creates the default view matrix.
     */
    public static void SetDefaultViewMatrix(float[] viewMatrix) {
        Matrix.setLookAtM(viewMatrix, 0,
                          0.0f, 0.0f, LayerCameraRenderable.EYE_Z,
                          0.0f, 0.0f, LayerCameraRenderable.CENTER_Z,
                          LayerCameraRenderable.UP_X,
                          LayerCameraRenderable.UP_Y,
                          LayerCameraRenderable.UP_Z);
    }

    /**
     * Returns the screen ratio this camera is rendering at.
     */
    public float getScreenRatio() {
        return ((float) mGameView.getWidth()) / ((float) mGameView.getHeight());
    }

    /**
     * Given an aspect ratio and a width, returns what the height should be.
     *
     * @param ratio (width / height)
     */
    public static float getHeightFromRatioWidth(float ratio, float width) {
        return ((1.0f / ratio) * width);
    }

    /**
     * Given an aspect ratio and a height, returns what the width should be.
     *
     * @param ratio  (width / height)
     */
    public static float getWidthFromRatioHeight(float ratio, float height) {
        return (ratio * height);
    }

    /**
     * Add a function to be executed every update before bounds checking
     *
     * @param listener : LayerCameara -> ()
     */
    public void addUpdateListener(Function1<LayerCamera, Void> listener) {
        this.mListeners.add(listener);
    }

    /*
     * Getters and Setters
     */
    @Override
    public LayerCamera getInstance() {
        return this;
    }

    public float getLeftX() {
        if (mAnchorPoint == AnchorPoint.CENTER)
            return getXLocation() - getWidth() / 2.0f;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT)
            return getXLocation();
        return Float.NaN;
    }

    public float getRightX() {
        if (mAnchorPoint == AnchorPoint.CENTER)
            return getXLocation() + getWidth() / 2.0f;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT)
            return getXLocation() + getWidth();
        return Float.NaN;
    }

    public float getBottomY() {
        if (mAnchorPoint == AnchorPoint.CENTER)
            return getYLocation() - getHeight() / 2.0f;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT)
            return getYLocation();
        return Float.NaN;
    }

    public float getTopY() {
        if (mAnchorPoint == AnchorPoint.CENTER)
            return getYLocation() + getHeight() / 2.0f;
        if (mAnchorPoint == AnchorPoint.BOTTOM_LEFT)
            return getYLocation() + getHeight();
        return Float.NaN;
    }

    public float getScreenWidth() {
        return mGameView.getWidth();
    }

    public float getScreenHeight() {
        return mGameView.getHeight();
    }

    public float getWidth() {
        return mWidth;
    }

    public void setWidth(float width) {
        this.mWidth = width;
    }

    public float getHeight() {
        return mHeight;
    }

    public void setHeight(float height) {
        this.mHeight = height;
    }

    public AnchorPoint getAnchorPoint() {
        return mAnchorPoint;
    }

    public void setAnchorPoint(AnchorPoint anchorPoint) {
        this.mAnchorPoint = anchorPoint;
    }

    public void setAspectType(AspectType aspectType) {
        this.mAspectType = aspectType;
    }

    public AspectType getAspectType() {
        return mAspectType;
    }
}
