package com.jlreyes.tests.test_game;

import com.jlreyes.libraries.android_game_engine.scenes.SceneController.SceneInfo;
import com.jlreyes.tests.test_game.scenes.loadscene.MyLoadScene;
import com.jlreyes.tests.test_game.scenes.startscene.StartScene;

/**
 * User: James
 * Date: 1/14/13
 * Time: 11:23 PM
 *
 */
public class Scenes {
    public static final SceneInfo START_SCENE = new SceneInfo(StartScene.class);
    public static final SceneInfo LOAD_SCENE = new SceneInfo(MyLoadScene.class);
}
