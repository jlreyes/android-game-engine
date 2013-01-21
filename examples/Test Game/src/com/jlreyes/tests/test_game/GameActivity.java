package com.jlreyes.tests.test_game;

import com.jlreyes.libraries.android_game_engine.AGEActivity;
import com.jlreyes.libraries.android_game_engine.scenes.SceneController;
import com.jlreyes.libraries.android_game_engine.texinit.TextureInitActivity;

public class GameActivity extends AGEActivity {
    public static final String TAG = "GameActivity";

    @Override
    protected void init() {}

    @Override
    protected Class<? extends TextureInitActivity> getTexInitActivityClass() {
        return Textures.class;
    }

    @Override
    protected SceneController.SceneInfo getStartSceneInfo() {
        return Scenes.START_SCENE;
    }

    @Override
    protected SceneController.SceneInfo getLoadSceneInfo() {
        return Scenes.LOAD_SCENE;
    }
}
