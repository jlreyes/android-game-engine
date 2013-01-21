package com.jlreyes.libraries.android_game_engine.sprites.sprites;

import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.sprites.Sprite;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;

/**
 * User: James
 * Date: 1/15/13
 * Time: 8:18 PM
 */
public abstract class GameCamera extends Sprite {
    public GameCamera(String name, Layer layer, Renderable renderable) {
        super(name, layer, renderable, TexController.NO_TEX, true);
    }

    public GameCamera(String name, Layer layer, Renderable renderable, float startX, float startY, float startScale, float startAngle) {
        super(name, layer, renderable, TexController.NO_TEX, true, startX,
              startY, startScale, startAngle);
    }

    public abstract LayerCamera getInstance();
}
