package com.jlreyes.libraries.android_game_engine.scenes;

import android.util.Log;
import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadScene;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.threading.TempThread;
import com.jlreyes.libraries.android_game_engine.threading.logic.GameCommand;
import com.jlreyes.libraries.android_game_engine.threading.logic.LogicManager;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

public abstract class Loader<T> extends TempThread {
    public static class PercentDone {
        private Integer mPercentDone;

        public PercentDone() {
            this.mPercentDone = 0;
        }

        public synchronized void setPercentDone(int i) {
            synchronized (mPercentDone) {
                this.mPercentDone = i;
            }
        }

        public synchronized int getPercentDone() {
            synchronized (mPercentDone) {
                return mPercentDone;
            }
        }
    }

    private LogicManager mLogicManager;

    private String mPackageLocation;
    private String mName;
    private PercentDone mPercentDone;
    private LoadScene mLoadScene;
    private Function1<T, Void> mCallback;

    public Loader(Scheduler scheduler,
                  String packageLocation,
                  String name,
                  LoadScene loadScene,
                  Function1<T, Void> callback) {
        super(scheduler);
        this.mLogicManager = scheduler.getLogicManager();
        this.mPackageLocation = packageLocation;
        this.mName = name;
        this.mPercentDone = new PercentDone();
        this.mLoadScene = loadScene;
        this.mCallback = callback;

        this.mLoadScene.load(this.mPercentDone);
    }


    public void run() {;
        mLogicManager.addScene(mLoadScene);
        T instance = getLoadedObject();
        Log.i("Loader", "Created instance of " + mName);
        try {
            onLoad(instance);
        } catch(Exception e) {
            if (this.activityIsPaused()) return;
            else throw new RuntimeException("Load error.", e);
        }
        getPercentDone().setPercentDone(100);
        mLogicManager.addGameCommand(new GameCommand(GameCommand.Command.KILL,
                                                     SceneController.LOAD_SCENE));
        mCallback.run(instance);
        Log.i("Loader", "Finished loading " + mName);
        System.gc();
    }

    public abstract void onLoad(T instance);

    public T getLoadedObject() {
        String className = mPackageLocation + "." + mName;
        T instance = null;
        Class classObject = null;
        try {
            classObject = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        instance = (T) constructObject(classObject);
        return instance;
    }

    public abstract T constructObject(Class classObject);

    public PercentDone getPercentDone() {
        return mPercentDone;
    }
}
