package com.jlreyes.libraries.android_game_engine.scenes;

import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadScene;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

/**
 * Author: James
 * Date: 1/14/13
 * Time: 11:06 PM
 */
public class SceneController {
    public static class SceneInfo {
        public Class<? extends Scene> ClassName;

        public SceneInfo(Class<? extends Scene> className) {
            this.ClassName = className;
        }
    }

    public static SceneInfo START_SCENE;
    public static SceneInfo LOAD_SCENE;
    public static Function1<Scheduler, LoadScene> LOAD_SCENE_CONSTRUCTOR;

    public static void InitLoadSceneConstructor() {
        LOAD_SCENE_CONSTRUCTOR = new Function1<Scheduler, LoadScene>() {
            @Override
            public LoadScene run(Scheduler scheduler) {
                return (LoadScene) SceneLoader.ConstructObject(scheduler, LOAD_SCENE.ClassName);
            }
        };
    }
}
