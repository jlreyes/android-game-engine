package com.jlreyes.tests.test_game.scenes.startscene;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.RectSprite;
import com.jlreyes.tests.test_game.Textures;

/**
 * User: James
 * Date: 1/15/13
 * Time: 9:24 PM
 */
public class MainCharacter extends RectSprite {
    public MainCharacter(MainLayer layer) {
        super("MainCharacter", layer, Textures.MAIN_CHARACTER, true, 20, 20);
    }

    @Override
    public void update(long deltaTime) {
    }

    @Override
    public boolean onTouchDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onTouchCancel(MotionEvent e) {
    }

    @Override
    public boolean onTouchUp(MotionEvent e) {
        ((MainLayer) this.getLayer()).onMainCharacterTap();
        return true;
    }
}
