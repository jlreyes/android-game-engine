package com.jlreyes.libraries.android_game_engine.scenes;

import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadScene;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * TempThread handling scene loading. Exists only as long as loading still needs
 * to be done.
 *
 * @author jlreyes
 */
public class SceneLoader extends Loader<Scene> {
    private Scheduler mScheduler;

    public SceneLoader(Scheduler scheduler,
                       Class sceneClass,
                       LoadScene loadScene,
                       Function1<Scene, Void> callback) {
        super(scheduler,
              sceneClass.getPackage().getName(),
              sceneClass.getSimpleName(),
              loadScene,
              callback);

        this.mScheduler = scheduler;
    }

    @Override
    public void onLoad(Scene instance) {
        instance.load(getPercentDone());
    }

    public Scene constructObject(Class classObject) {
        return SceneLoader.ConstructObject(mScheduler, classObject);
    }

    public static Scene ConstructObject(Scheduler scheduler, Class classObject) {
        Constructor tConstructor = null;
        try {
            tConstructor = classObject.getConstructor(Scheduler.class);
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        }
        Scene scene = null;
        try {
            scene = (Scene) tConstructor.newInstance(scheduler);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return scene;
    }
}
