package com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene;

import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.scenes.Scene;

/**
 * User: James
 * Date: 1/18/13
 * Time: 12:54 PM
 */
public abstract class LoadLayer extends Layer {
    public LoadLayer(LoadScene scene) {
        super("LoadLayer", scene);
    }

    @Override
    public void updateOnState(long deltaTime, Scene.SceneState state) {
        this.onUpdate(((LoadScene.LoadState) state).getPercentDone());
    }

    public abstract void onUpdate(int percent);
}
