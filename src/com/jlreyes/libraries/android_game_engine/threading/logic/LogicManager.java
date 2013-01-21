package com.jlreyes.libraries.android_game_engine.threading.logic;

import android.util.Log;
import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.io.GameView;
import com.jlreyes.libraries.android_game_engine.rendering.GameRenderer;
import com.jlreyes.libraries.android_game_engine.rendering.RenderInfo;
import com.jlreyes.libraries.android_game_engine.scenes.Scene;
import com.jlreyes.libraries.android_game_engine.scenes.SceneController;
import com.jlreyes.libraries.android_game_engine.scenes.SceneLoader;
import com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene.LoadScene;
import com.jlreyes.libraries.android_game_engine.threading.GameThread;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.threading.SyncWrapper;
import com.jlreyes.libraries.android_game_engine.utils.math.Tuple;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Logic game loop and scene managing.
 *
 * @author jlreyes
 */
public class LogicManager extends GameThread {
    public static final String TAG = "Logic Manager";

    private Scheduler mScheduler;
    private GameView mGameView;
    private GameRenderer mGameRenderer;

    private Scene[] mScenes;
    private ArrayList<GameCommand> mCommands;
    private ArrayList<GameCommand> mCommandQueue;
    private ArrayList<MotionEvent> mInputEvents;
    private SyncWrapper<RenderInfo> mAllRenderInfo;

    private Queue<Function0<Void>> mResumeQueue;
    private volatile Boolean mLoading;
    private SceneLoader mLoader;

    private long currentTime;
    private long lastTime;
    private long deltaTime;

    /**
     * The minimum time we must wait between logic updates in ms
     */
    private static final long TIMESTEP = 10l;
    /**
     * Function called after a scene has been loaded
     */
    private final Function1<Scene, Void> LOAD_CALLBACK = new Function1<Scene, Void>() {
        public Void run(Scene scene) {
            if (getLoopState() == LoopState.PAUSED) {
                Log.w("LOAD_CALLBACK", "Load completed, but we have paused!");
                return null;
            }
            LogicManager.this.addScene(scene);
            synchronized (mLoading) {
                mLoading = false;
                mLoader = null;
            }
            return null;
        }
    };

    public LogicManager(String name, Scheduler scheduler) {
        super(name);
        this.mScheduler = scheduler;
        this.mGameView = scheduler.getGameView();
        this.mGameRenderer = scheduler.getGameRenderer();
        this.mScenes = new Scene[0];
        this.mCommands = new ArrayList<GameCommand>();
        this.mCommands.add(
                new GameCommand(GameCommand.Command.LOAD, SceneController.START_SCENE));
        this.mCommandQueue = new ArrayList<GameCommand>();
        this.mInputEvents = new ArrayList<MotionEvent>();
        this.mAllRenderInfo = new SyncWrapper<RenderInfo>(mScheduler);
        this.mAllRenderInfo.add(new RenderInfo());
        this.mAllRenderInfo.add(new RenderInfo());
        this.mResumeQueue = new LinkedList<Function0<Void>>();
        this.mLoading = false;
        this.mLoader = null;
        this.currentTime = 0l;
        this.lastTime = 0l;
        this.deltaTime = 0l;

        if (SceneController.START_SCENE == null)
            throw new IllegalStateException("SceneController.START_SCENE is null");
    }

    public void onPause() {
        super.onPause();
        removeScene(SceneController.LOAD_SCENE);
        synchronized (mLoading) {
            if (mLoading == true) {
                Log.w(TAG, "Pause during loading. Adding loading to resume " +
                           "queue.");
                this.mResumeQueue.add(new Function0<Void>() {
                    @Override
                    public Void run() {
                        LoadScene loadScene = SceneController.LOAD_SCENE_CONSTRUCTOR
                                                             .run(mScheduler);
                        mLoader = new SceneLoader(mScheduler,
                                                  mLoader.getSceneClass(),
                                                  loadScene,
                                                  mLoader.getCallback());
                        mScheduler.addThread(mLoader);
                        return null;
                    }
                });
            }
        }
        for (Scene scene : mScenes) scene.onPause();
    }

    public void onResume() {
        /* We need to execute functions we stored on pause */
        synchronized (mLoading) {
            this.mLoading = false;
            while (!this.mResumeQueue.isEmpty())
                this.mResumeQueue.remove().run();
        }
        /* We need to reload all scenes. We do this on another thread to
         * prevent ui lockup */
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Scene scene : mScenes) scene.onResume();
                LogicManager.super.onResume();
            }
        }).start();
    }

    @Override
    protected void onRunning() {
        processTime();
        processCommands();
        processInput();
        processScenes();
    }

    /**
     * Process time and delay/skip logic updates if we need to.
     */
    private void processTime() {
        this.currentTime = System.currentTimeMillis();
        this.deltaTime = currentTime - lastTime;
        this.lastTime = currentTime;
        //Log.i("FPS", "" + ((float) deltaTime / (1000f / 60f)) * 60.0f); // TODO
        if (deltaTime <= TIMESTEP) {
            try {
                Thread.sleep(TIMESTEP - deltaTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.deltaTime = TIMESTEP;
        }
    }

    /**
     * Copies the MotionEvents from the given linked list to mInputEvents
     */
    private void processInput() {
        mInputEvents = mGameView.getInputEvents();
    }

    /**
     * Processes all commands in mCommands
     */
    private void processCommands() {
        synchronized (mCommands) {
            int numQueued = mCommandQueue.size();
            for (int i = 0; i < numQueued; i++)
                mCommands.add(mCommandQueue.get(i));
            mCommandQueue.clear();

            int length = mCommands.size();
            for (int i = 0; i < length; i++) {
                GameCommand gameCommand = mCommands.get(i);
                switch (gameCommand.getCommand()) {
                    case LOAD: {
                        synchronized (mLoading) {
                            if (this.mLoading == true) {
                                this.mCommandQueue.add(gameCommand);
                                break;
                            }
                            this.mLoading = true;
                            LoadScene loadScene = SceneController.LOAD_SCENE_CONSTRUCTOR
                                                                 .run(mScheduler);
                            SceneController.SceneInfo sceneInfo =
                                    (SceneController.SceneInfo) gameCommand.getArgs();
                            mLoader = new SceneLoader(mScheduler,
                                                      sceneInfo.ClassName,
                                                      loadScene,
                                                      LOAD_CALLBACK);
                            mScheduler.addThread(mLoader);
                        }
                        break;
                    }
                    case KILL: {
                        SceneController.SceneInfo sceneInfo =
                                (SceneController.SceneInfo) gameCommand.getArgs();
                        removeScene(sceneInfo);
                        break;
                    }
                    case LOAD_REPLACE: {
                        synchronized (mLoading) {
                            if (this.mLoading == true) {
                                this.mCommandQueue.add(gameCommand);
                                break;
                            }
                            this.mLoading = true;
                            Tuple args = (Tuple) gameCommand.getArgs();
                            final SceneController.SceneInfo thisScene =
                                    (SceneController.SceneInfo) args.get(0);
                            final SceneController.SceneInfo nextScene =
                                    (SceneController.SceneInfo) args.get(1);
                            Function1<Scene, Void> callback = new Function1<Scene, Void>() {
                                @Override
                                public Void run(Scene scene) {
                                    if (getLoopState() == LoopState.PAUSED)
                                        return null;
                                    removeScene(thisScene);
                                    LOAD_CALLBACK.run(scene);
                                    return null;
                                }
                            };
                            LoadScene loadScene = SceneController.LOAD_SCENE_CONSTRUCTOR
                                                                 .run(mScheduler);
                            mLoader = new SceneLoader(mScheduler,
                                                      nextScene.ClassName,
                                                      loadScene,
                                                      callback);
                            mScheduler.addThread(mLoader);
                            break;
                        }
                    }
                    case SHOW_ERROR: {
                        /* TODO: Throw error */
                        break;
                    }
                    case FATAL_ERROR: {
                        // TODO: Deal with error.
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            mCommands.clear();
        }
    }

    /**
     * Iterate through all the scenes, updating each, ultimately updating
     * mCommands and mAllRenderInfo and passing the data to the render thread.
     */
    private void processScenes() {
        RenderInfo renderInfo = null;
        renderInfo = (RenderInfo) mAllRenderInfo.get();
        renderInfo.clear();        /* Update all scenes. */
        synchronized (mScenes) {
            for (Scene scene : mScenes) {
                scene.step(deltaTime, mInputEvents);
                renderInfo.append(scene.getRenderInfo());
            }
            mGameRenderer.update(renderInfo);
            mAllRenderInfo.pass(renderInfo, mGameRenderer.getThread());
        }
    }

    public void addGameCommand(GameCommand gameCommand) {
        synchronized (mCommands) {
            mCommands.add(gameCommand);
        }
    }

    /**
     * Adds a new scene.
     * O(n) where n is the number of scenes including the added one.
     */
    public void addScene(Scene scene) {
        synchronized (mScenes) {
            int newSize = mScenes.length + 1;
            Scene[] newScenes = new Scene[newSize];
            System.arraycopy(mScenes, 0, newScenes, 0, mScenes.length);
            newScenes[mScenes.length] = scene;
            mScenes = newScenes;
        }
    }

    /**
     * Removes a scene.
     * O(n) where n is the number of scenes held.
     */
    public void removeScene(SceneController.SceneInfo sceneInfo) {
        synchronized (mScenes) {
			/* Finding the index of this scene */
            int sceneIndex = -1;
            int length = mScenes.length;
            for (int i = 0; i < length; i++) {
                if (mScenes[i].getClass().equals(sceneInfo.ClassName)) {
                    sceneIndex = i;
                    break;
                }
            }
            if (sceneIndex == -1) return;
			/* Removing the scene */
            int newSize = mScenes.length - 1;
            Scene[] newScenes = new Scene[newSize];
            if (newSize != 0) {
                System.arraycopy(mScenes, 0, newScenes, 0, sceneIndex);
                System.arraycopy(mScenes, sceneIndex + 1,
                                 newScenes, sceneIndex,
                                 newSize - sceneIndex);
            }
            mScenes = newScenes;
        }
        /* Removing other instances of this scene */
        removeScene(sceneInfo);
    }
}
