package com.jlreyes.tests.test_game.scenes.loadscene;

import com.jlreyes.libraries.android_game_engine.scenes.Loader;
import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadLayer;
import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadScene;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.GameCamera;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.RectSprite;

/**
 * User: James
 * Date: 1/18/13
 * Time: 1:36 PM
 */
public class MyLoadLayer extends LoadLayer {
    public MyLoadLayer(LoadScene scene) {
        super(scene);
    }

    public void load(Loader.PercentDone p) {
        super.load(p);
        /* Load loading sprite */
        this.addSprite(new LoadText(this));
    }

    @Override
    public void onUpdate(int percent) {}

    @Override
    public GameCamera createCamera() {
        LayerCamera c = new LayerCamera("LoadCamera", this, 10, 0, 0, 1, 0);
        c.setAnchorPoint(LayerCamera.AnchorPoint.CENTER);
        return c;
    }
}
