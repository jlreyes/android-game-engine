package com.jlreyes.libraries.android_game_engine.sprites;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TextureLoader;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.threading.SyncWrapper;

public abstract class Sprite {
    public static final float DEFAULT_X = 0.0f;
    public static final float DEFAULT_Y = 0.0f;
    public static final float DEFAULT_SCALE = 1.0f;
    public static final float DEFAULT_ANGLE = 0.0f;

    private String mName;
    private Texture mTexture;
    private Layer mLayer;
    private boolean mIsMoveable;
    private float mXLoc;
    private float mYLoc;
    private float mScale;
    private float mAngle;

    private SyncWrapper<Renderable> mRenderableWrapper;
    private Renderable mActiveRenderable;

    /**
     * Same as Sprite(String, SpriteWrapper, Renderable, int, boolean, float, float, float, float)
     * except this creates a sprite with default position, scale, and angle.
     */
    public Sprite(String name,
                  SpriteWrapper spriteWrapper,
                  Renderable renderable,
                  TexController.TexInfo texInfo,
                  boolean isMoveable) {
        this(name,
             spriteWrapper,
             renderable,
             texInfo,
             isMoveable,
             DEFAULT_X,
             DEFAULT_Y,
             DEFAULT_SCALE,
             DEFAULT_ANGLE);
    }

    /**
     * Creates a new sprite.
     *
     * @param name       The name of the sprite.
     * @param wrapper    The spritewrapper holding this sprite.
     * @param renderable The renderable to use for this sprite
     * @param texInfo    The texture to load and use for this sprite.
     * @param isMovable True iff this sprite will ever move from its starting
     *                   position, scale, or angle
     * @param startX     The starting x position for this sprite.
     * @param startY     The starting y position for this sprite.
     * @param startScale The starting scale for this sprite
     * @param startAngle The starting angle for this sprite
     */
    public Sprite(String name,
                  SpriteWrapper wrapper,
                  Renderable renderable,
                  TexController.TexInfo texInfo,
                  boolean isMovable,
                  float startX,
                  float startY,
                  float startScale,
                  float startAngle) {
        Scheduler scheduler = wrapper.getLayer().getParentScene().getScheduler();
        Texture texture = TextureLoader.LoadTexture(texInfo, scheduler);
        String formattedName = wrapper.getName() + "." + name;
        CreateSprite(formattedName,
                     wrapper.getLayer(),
                     renderable,
                     texture,
                     isMovable,
                     startX,
                     startY,
                     startScale,
                     startAngle);
    }

    /**
     * Same as Sprite(String, SpriteWrapper, Renderable, Texture, boolean, float, float, float, float)
     * except this creates a sprite with default position, scale, and angle.
     */
    public Sprite(String name,
                  SpriteWrapper spriteWrapper,
                  Renderable renderable,
                  Texture texture,
                  boolean isMoveable) {
        this(name,
             spriteWrapper,
             renderable,
             texture,
             isMoveable,
             DEFAULT_X,
             DEFAULT_Y,
             DEFAULT_SCALE,
             DEFAULT_ANGLE);
    }

    /**
     * Creates a new sprite.
     *
     * @param name       The name of the sprite.
     * @param wrapper    The spritewrapper holding this sprite.
     * @param renderable The renderable to use for this sprite
     * @param texture    The already loaded texture to use for this sprite.
     * @param isMoveable True iff this sprite will ever move from its starting
     *                   position, scale, or angle
     * @param startX     The starting x position for this sprite.
     * @param startY     The starting y position for this sprite.
     * @param startScale The starting scale for this sprite
     * @param startAngle The starting angle for this sprite
     */
    public Sprite(String name,
                  SpriteWrapper wrapper,
                  Renderable renderable,
                  Texture texture,
                  boolean isMoveable,
                  float startX,
                  float startY,
                  float startScale,
                  float startAngle) {
        String formattedName = wrapper.getName() + "." + name;
        CreateSprite(formattedName,
                     wrapper.getLayer(),
                     renderable,
                     texture,
                     isMoveable,
                     startX,
                     startY + wrapper.getYLocation(),
                     startScale,
                     startAngle);
    }

    /**
     * Same as Sprite(String, Layer, Renderable, Texture, boolean, float, float, float, float)
     * except this creates a sprite with default position, scale, and angle.
     */
    public Sprite(String name,
                  Layer layer,
                  Renderable renderable,
                  Texture texture,
                  boolean isMoveable) {
        this(name,
             layer,
             renderable,
             texture,
             isMoveable,
             DEFAULT_X,
             DEFAULT_Y,
             DEFAULT_SCALE,
             DEFAULT_ANGLE);
    }

    /**
     * Creates a new sprite.
     *
     * @param name       The name of the sprite.
     * @param layer      The layer this sprite is attached to.
     * @param renderable The renderable to use for this sprite
     * @param texture    The already loaded texture to use for this sprite.
     * @param isMoveable True iff this sprite will ever move from its starting
     *                   position, scale, or angle
     * @param startX     The starting x position for this sprite.
     * @param startY     The starting y position for this sprite.
     * @param startScale The starting scale for this sprite
     * @param startAngle The starting angle for this sprite
     */
    public Sprite(String name,
                  Layer layer,
                  Renderable renderable,
                  Texture texture,
                  boolean isMoveable,
                  float startX,
                  float startY,
                  float startScale,
                  float startAngle) {
        String formattedName = layer.getName() + "." + name;
        CreateSprite(formattedName,
                     layer,
                     renderable,
                     texture,
                     isMoveable,
                     startX,
                     startY,
                     startScale,
                     startAngle);
    }

    /**
     * Same as Sprite(String, Layer, Renderable, int, boolean, float, float, float, float)
     * except this creates a sprite with default position, scale, and angle.
     */
    public Sprite(String name,
                  Layer layer,
                  Renderable renderable,
                  TexController.TexInfo texInfo,
                  boolean isMoveable) {
        this(name,
             layer,
             renderable,
             texInfo,
             isMoveable,
             DEFAULT_X,
             DEFAULT_Y,
             DEFAULT_SCALE,
             DEFAULT_ANGLE);
    }

    /**
     * Creates a new sprite.
     *
     * @param name       The name of the sprite.
     * @param layer      The layer this sprite is attached to.
     * @param renderable The renderable to use for this sprite
     * @param texInfo    The texture to load and use for this sprite.
     * @param isMoveable True iff this sprite will ever move from its starting
     *                   position, scale, or angle
     * @param startX     The starting x position for this sprite.
     * @param startY     The starting y position for this sprite.
     * @param startScale The starting scale for this sprite
     * @param startAngle The starting angle for this sprite
     */
    public Sprite(String name,
                  Layer layer,
                  Renderable renderable,
                  TexController.TexInfo texInfo,
                  boolean isMoveable,
                  float startX,
                  float startY,
                  float startScale,
                  float startAngle) {
        Scheduler scheduler = layer.getParentScene().getScheduler();
        Texture texture = TextureLoader.LoadTexture(texInfo, scheduler);
        String formattedName = layer.getName() + "." + name;
        CreateSprite(formattedName,
                     layer,
                     renderable,
                     texture,
                     isMoveable,
                     startX,
                     startY,
                     startScale,
                     startAngle);
    }

    private void CreateSprite(String formattedName,
                              Layer layer,
                              Renderable renderable,
                              Texture texture,
                              boolean isMoveable,
                              float startX,
                              float startY,
                              float startScale,
                              float startAngle) {
        this.mName = formattedName;
        this.mTexture = null;
        this.mLayer = layer;
        this.mIsMoveable = isMoveable;
        this.mXLoc = startX;
        this.mYLoc = startY;
        this.mScale = startScale;
        this.mAngle = startAngle;
        Scheduler scheduler = layer.getParentScene().getScheduler();
        /* Attaching to cull grid, if we need to */
        if (isMovable() == false)
            if (layer.hasCullGrid() == true)
                layer.getCullGrid().addSprite(this, renderable);
        /* Attaching renderable */
        this.mRenderableWrapper = new SyncWrapper<Renderable>(scheduler);
        mRenderableWrapper.add(renderable);
        mRenderableWrapper.add(renderable.copy());
        this.mActiveRenderable = null;
        /* Attaching texture */
        this.mTexture = texture;
        if (texture != null)
            mTexture.updateVertices(renderable.getRenderType(),
                                    renderable.getNumVertices());
    }

    /**
     * Grabs a free renderable for the logic step.
     * MUST BE CALLED AT THE BEGINNING OF EVERY LOGIC STEP.
     */
    public void onStartLogicStep() {
        mActiveRenderable = mRenderableWrapper.get();
    }

    /**
     * Step this character by deltaTime.
     */
    public abstract void update(long deltaTime);

    /**
     * MUST BE CALLED AT END OF EVERY LOGIC STEP. Updates the active renderable
     * and then sets it to null.
     * @return The renderable passed.
     */
    public Renderable onEndLogicStep(Thread thread) {
        if (mActiveRenderable == null)
            throw new RuntimeException("No active renderable!");        /* Update the renderable's texture, location, scale, and angle data */
        mActiveRenderable.setTexture(mTexture);
        mActiveRenderable.translate(mXLoc, mYLoc);
        mActiveRenderable.scale(mScale, mScale);
        mActiveRenderable.rotate(mAngle);        /* Send the active renderable to the given thread */
        Renderable sentRenderable = mActiveRenderable;
        mRenderableWrapper.pass(sentRenderable, thread);
		/* Clean up */
        mActiveRenderable = null;
        return sentRenderable;
    }

    /**
     * Returns the active renderable.
     */
    public Renderable getActiveRenderable() {
        return mActiveRenderable;
    }

    /**
     * Changes this sprites renderable to the given renderable.
     */
    public void changeRenderable(Renderable renderable) {
        /* Update the renderable wrapper */
        mRenderableWrapper.clear();
        mRenderableWrapper.add(renderable);
        mRenderableWrapper.add(renderable.copy());
        /* If there was an active renderable, change it */
        if (mActiveRenderable != null) onStartLogicStep();
        /* Update the texture */
        if (mTexture != null)
            mTexture.updateVertices(renderable.getRenderType(),
                                    renderable.getNumVertices());
    }


    public void setTextureState(TexController.TexStateInfo stateInfo) {
        mTexture.setActiveState(stateInfo);
    }

    /**
     * If the given thread holds a renderable for this sprite, calling
     * this releases it
     */
    public void release(Thread thread) {
        mRenderableWrapper.release(thread);
    }

    /**
     * Returns true iff the given x and y touch the sprite.
     */
    public abstract boolean isTouching(float x, float y);

    /**
     * Called when a touch starts on the sprite.
     *
     * @return true if this sprite handled the touch, false otherwise.
     */
    public abstract boolean onTouchDown(MotionEvent e);


    /** Called when a touch is canceled after a onTouchDown was called. */
    public abstract void onTouchCancel(MotionEvent e);

    /**
     * Called when a touch ends on the sprite.
     *
     * @return true if this sprite handled the touch, false otherwise.
     */
    public abstract boolean onTouchUp(MotionEvent e);

    /*
     * Position Methods
     */
    public void setMovable() {
        if (isMovable() == true) return;
        Layer layer = getLayer();
	    /* Get renderable */
        boolean activeRenderableExisted = false;
        Renderable renderable = null;
        if (mActiveRenderable != null) {
            renderable = mActiveRenderable;
            activeRenderableExisted = true;
        } else renderable = mRenderableWrapper.get();
        if (layer.hasCullGrid() == true)
            layer.getCullGrid().addSprite(this, renderable);
        mRenderableWrapper.release(Thread.currentThread());
        if (activeRenderableExisted == false) mActiveRenderable = null;
    }

    public void setImmovable() {
        if (isMovable() == false) return;
        Layer layer = getLayer();
        if (layer.hasCullGrid() == true) layer.getCullGrid().removeSprite(this);
    }

    public boolean isMovable() {
        return mIsMoveable;
    }

    private void checkMoveable() {
        if (mIsMoveable == false)
            throw new RuntimeException("Attempting to a move a sprite declared " +
                                       "to be immovable! The sprite's name is " +
                                       getName() + " and is located at " + toString());
    }

    public float getXLocation() {
        return mXLoc;
    }

    public float getYLocation() {
        return mYLoc;
    }

    /**
     * mTexture
     * Sets the location of this sprite.
     */
    public void setLocation(float v1, float v2) {
        checkMoveable();
        mXLoc = v1;
        mYLoc = v2;
    }

    public void setXLocation(float x) {
        setLocation(x, mYLoc);
    }

    public void setYLocation(float y) {
        setLocation(mXLoc, y);
    }

    /**
     * Translates this sprite by xOffset and yOffset
     */
    public void move(float xOffset, float yOffset) {
        setLocation(mXLoc + xOffset, mYLoc + yOffset);
    }

    public void setScale(float scale) {
        checkMoveable();
        this.mScale = scale;
    }

    /**
     * Increase the scale by delta.
     */
    public void scaleBy(float delta) {
        setScale(mScale + delta);
    }

    public float getScale() {
        return mScale;
    }

    public void setAngle(float angle) {
        checkMoveable();
        this.mAngle = angle;
    }

    public float getAngle() {
        return mAngle;
    }

    /**
     * Increments mAngle by delta.
     */
    public void rotateBy(float delta) {
        setAngle(mAngle + delta);
    }

    /*
     * Getter and Setters
     */
    public String getName() {
        return mName;
    }

    public Layer getLayer() {
        return mLayer;
    }

    public Texture getTexture() {
        return mTexture;
    }
}
