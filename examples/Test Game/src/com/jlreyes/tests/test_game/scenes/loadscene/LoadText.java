package com.jlreyes.tests.test_game.scenes.loadscene;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.scenes.Scene;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.RectSprite;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.tests.test_game.Textures;

/**
 * Author: James
 * Date: 1/18/13
 * Time: 4:35 PM
 */
public class LoadText extends RectSprite {
    public LoadText(MyLoadLayer layer) {
        super("LoadSprite", layer, Textures.LOADING, false, 4, 2);
    }

    @Override
    public void update(long deltaTime) {}

    @Override
    public boolean onTouchDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchCancel(MotionEvent e) {
    }

    @Override
    public boolean onTouchUp(MotionEvent e) {
        return false;
    }
}
