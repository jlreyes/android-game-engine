package com.jlreyes.tests.test_game.scenes.loadscene;

import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadLayer;
import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadScene;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;

/**
 * User: James
 * Date: 1/18/13
 * Time: 1:35 PM
 */
public class MyLoadScene extends LoadScene {
    public MyLoadScene(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    protected LoadLayer[] createLoadLayers() {
        return new LoadLayer[] {
            new MyLoadLayer(this)
        };
    }
}
