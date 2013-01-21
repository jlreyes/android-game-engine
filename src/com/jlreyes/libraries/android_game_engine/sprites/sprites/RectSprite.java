package com.jlreyes.libraries.android_game_engine.sprites.sprites;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Rectangle;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.Sprite;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;

/**
 * A standard rectangle sprite.
 *
 * @author jlreyes
 */
public abstract class RectSprite extends Sprite {
    private float mWidth;
    private float mHeight;

    public RectSprite(String name,
                      Layer layer,
                      Texture texture,
                      boolean isMoveable,
                      float width,
                      float height) {
        this(name,
             layer,
             texture,
             isMoveable,
             Sprite.DEFAULT_X,
             Sprite.DEFAULT_Y,
             Sprite.DEFAULT_SCALE,
             Sprite.DEFAULT_ANGLE,
             width,
             height);
    }

    public RectSprite(String name,
                      Layer layer,
                      Texture texture,
                      boolean isMoveable,
                      float startX,
                      float startY,
                      float startScale,
                      float startAngle,
                      float width,
                      float height) {
        super(name,
              layer,
              new Rectangle(width, height),
              texture,
              isMoveable,
              startX,
              startY,
              startScale,
              startAngle);
        this.mWidth = width;
        this.mHeight = height;
    }

    public RectSprite(String name,
                      Layer layer,
                      TexController.TexInfo texInfo,
                      boolean isMoveable,
                      float width,
                      float height) {
        this(name,
             layer,
             texInfo,
             isMoveable,
             Sprite.DEFAULT_X,
             Sprite.DEFAULT_Y,
             Sprite.DEFAULT_SCALE,
             Sprite.DEFAULT_ANGLE,
             width,
             height);
    }

    public RectSprite(String name,
                      Layer layer,
                      TexController.TexInfo texInfo,
                      boolean isMoveable,
                      float startX,
                      float startY,
                      float startScale,
                      float startAngle,
                      float width,
                      float height) {
        super(name,
              layer,
              new Rectangle(width, height),
              texInfo,
              isMoveable,
              startX,
              startY,
              startScale,
              startAngle);
        this.mWidth = width;
        this.mHeight = height;
    }

    @Override
    public abstract void onTouchCancel(MotionEvent e);

    @Override
    public boolean isTouching(float x, float y) {
        float spriteLeft = getXLocation() - mWidth / 2.0f;
        float spriteRight = getXLocation() + mWidth / 2.0f;
        float spriteBottom = getYLocation() - mHeight / 2.0f;
        float spriteTop = getYLocation() + mHeight / 2.0f;
        if (spriteLeft <= x && x <= spriteRight &&
            spriteBottom <= y && y <= spriteTop)
            return true;
        else return false;
    }
}
