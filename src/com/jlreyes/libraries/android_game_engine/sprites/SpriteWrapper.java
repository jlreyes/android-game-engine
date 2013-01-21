package com.jlreyes.libraries.android_game_engine.sprites;

import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.RenderablesWrapper;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;

/**
 * Class wrapping several sprites that are intended to be treated as the same
 * sprite.
 *
 * @author jlreyes
 */
public abstract class SpriteWrapper extends Sprite {
    public Sprite[] mSprites;

    /**
     * Same as SpriteWrapper(name, SpriteWrapper, boolean, {@link Sprite#DEFAULT_X},
     * {@link Sprite#DEFAULT_Y}, {@link Sprite#DEFAULT_SCALE}, {@link Sprite#DEFAULT_ANGLE})
     */
    public SpriteWrapper(String name,
                         SpriteWrapper wrapper,
                         boolean isMovable) {
        this(name,
             wrapper,
             isMovable,
             Sprite.DEFAULT_X,
             Sprite.DEFAULT_Y,
             Sprite.DEFAULT_SCALE,
             Sprite.DEFAULT_ANGLE);
    }

    /**
     * Creates a new SpriteWrapper.
     *
     * @param name       The name of this wrapper.
     * @param wrapper    The wrapper containing this wrapper.
     * @param isMoveable True iff this sprite will ever from its starting
     *                   position, scale, or angle.
     * @param startX     The starting x coordinate of this wrapper.
     * @param startY     The starting y coordinate of this wrapper.
     * @param startScale The starting scale of this wrapper.
     * @param startAngle the starting angle of this wrapper.
     */
    public SpriteWrapper(String name,
                         SpriteWrapper wrapper,
                         boolean isMoveable,
                         float startX,
                         float startY,
                         float startScale,
                         float startAngle) {
        super(name,
              wrapper,
              new RenderablesWrapper(0),
              TexController.NO_TEX,
              isMoveable,
              startX,
              startY,
              startScale,
              startAngle);
        this.mSprites = new Sprite[0];
    }

    /**
     * Same as SpriteWrapper(name, layer, boolean, {@link Sprite#DEFAULT_X}, {@link Sprite#DEFAULT_Y},
     * {@link Sprite#DEFAULT_SCALE}, {@link Sprite#DEFAULT_ANGLE})
     */
    public SpriteWrapper(String name,
                         Layer layer,
                         boolean isMoveable) {
        this(name,
             layer,
             isMoveable,
             Sprite.DEFAULT_X,
             Sprite.DEFAULT_Y,
             Sprite.DEFAULT_SCALE,
             Sprite.DEFAULT_ANGLE);
    }

    /**
     * Creates a new SpriteWrapper.
     *
     * @param name       The name of this wrapper.
     * @param layer      The layer containing this wrapper
     * @param isMoveable True iff this sprite will ever from its starting
     *                   position, scale, or angle.
     * @param startX     The starting x coordinate of this wrapper.
     * @param startY     The starting y coordinate of this wrapper.
     * @param startScale The starting scale of this wrapper.
     * @param startAngle the starting angle of this wrapper.
     */
    public SpriteWrapper(String name,
                         Layer layer,
                         boolean isMoveable,
                         float startX,
                         float startY,
                         float startScale,
                         float startAngle) {
        super(name,
              layer,
              new RenderablesWrapper(0),
              TexController.NO_TEX,
              isMoveable,
              startX,
              startY,
              startScale,
              startAngle);
        this.mSprites = new Sprite[0];
    }

    /**
     * Same as addSprites(new Sprite[] {sprite}).
     */
    public void addSprite(Sprite sprite) {
        addSprites(new Sprite[]{sprite});
    }

    /**
     * Adds the given sprites to this SpriteWrapper to handle. O(n), this should
     * not be called dynamically.
     */
    public void addSprites(Sprite[] sprites) {        /* Creating new array */
        int oldLength = mSprites.length;
        int newLength = oldLength + sprites.length;
        Sprite[] oldSprites = mSprites;
        Sprite[] newSprites = new Sprite[newLength];
        System.arraycopy(oldSprites, 0, newSprites, 0, oldLength);
        System.arraycopy(sprites, 0, newSprites, oldLength, sprites.length);        /* Update renderable */
        changeRenderable(new RenderablesWrapper(newLength));
	    /* Update sprites array */
        mSprites = sprites;
    }

    @Override
    public void onStartLogicStep() {
        super.onStartLogicStep();
        for (Sprite sprite : mSprites) sprite.onStartLogicStep();
    }

    @Override
    public void update(long deltaTime) {
        for (Sprite sprite : mSprites) sprite.update(deltaTime);
    }

    @Override
    public Renderable onEndLogicStep(Thread thread) {
        int numSprites = mSprites.length;
        RenderablesWrapper renderables =
                (RenderablesWrapper) getActiveRenderable();
        for (int i = 0; i < numSprites; i++) {
            Renderable renderable = mSprites[i].onEndLogicStep(thread);
		    /* Make sure the sprite is visible, else just set to null */
            LayerCamera gameCamera = getLayer().getCamera();
            if (renderable.isVisible(gameCamera) == false) renderable = null;
		    /* Set the ith index in the RenderablesWrapper */
            renderables.set(i, renderable);
        }
        return super.onEndLogicStep(thread);
    }

    /**
     * Does the same as {@link Sprite#setLocation(float, float)} except that
     * this also translates every sprite contained by this SpriteWrapper.
     */
    @Override
    public void setLocation(float v1, float v2) {
        float xOffset = v1 - getXLocation();
        float yOffset = v2 - getYLocation();
        super.setLocation(v1, v2);
        for (Sprite sprite : mSprites)
            sprite.move(xOffset, yOffset);
    }

    /**
     * Does the same as {@link Sprite#setScale(float)} except that
     * this also scales every sprite contained by this SpriteWrapper.
     */
    public void setScale(float scale) {
        float scaleOffset = scale - getScale();
        super.setScale(scale);
        for (Sprite sprite : mSprites)
            sprite.scaleBy(scaleOffset);
    }

    /**
     * Does the same as {@link Sprite#setAngle(float)} except that
     * this also rotates every sprite contained by this SpriteWrapper.
     */
    @Override
    public void setAngle(float angle) {
        float angleOffset = angle - getAngle();
        super.setAngle(angle);
        for (Sprite sprite : mSprites) sprite.rotateBy(angleOffset);
    }

    /*
     * Getters and Setters
     */
    public Sprite[] getSprites() {
        return mSprites;
    }
}
