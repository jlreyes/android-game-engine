package com.jlreyes.tests.test_game.scenes.startscene;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.RectSprite;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;
import com.jlreyes.tests.test_game.Textures;

/**
 * Author: James
 * Date: 1/18/13
 * Time: 4:48 PM
 */
public class Button extends RectSprite {
    private Function0<Void> mOnTap;

    public Button(String name,
                  Layer layer,
                  Function0<Void> onTap,
                  float startX, float startY,
                  float startScale, float startAngle,
                  float width, float height) {
        super(name, layer, Textures.NULL_TEXTURE,
              true, startX, startY, startScale, startAngle, width, height);
        this.mOnTap = onTap;
    }

    @Override
    public void update(long deltaTime) {

    }

    @Override
    public boolean onTouchDown(MotionEvent e) {
        this.setScale(2);
        return true;
    }

    public void onTouchCancel(MotionEvent e) {
        this.setScale(1);
    }

    @Override
    public boolean onTouchUp(MotionEvent e) {
        this.setScale(1);
        this.mOnTap.run();
        return true;
    }
}
