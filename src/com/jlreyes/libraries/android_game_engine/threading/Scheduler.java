package com.jlreyes.libraries.android_game_engine.threading;

import android.content.Context;
import android.util.Log;
import com.jlreyes.libraries.android_game_engine.datastructures.MinAllocHashSet;
import com.jlreyes.libraries.android_game_engine.io.GameView;
import com.jlreyes.libraries.android_game_engine.rendering.GameRenderer;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Animator;
import com.jlreyes.libraries.android_game_engine.threading.logic.LogicManager;
import com.jlreyes.libraries.android_game_engine.utils.ThreadUtils;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class that manages the game's threads and communication between them.
 *
 * @author jlreyes
 */
public class Scheduler extends GameThread {
    public static final String TAG = "Scheduler";

    private Context mContext;
    private boolean mInitialized;
    private GameView mGameView;
    private GameRenderer mGameRenderer;
    private LogicManager mLogicManager;
    //private MusicManager mMusicManager;
    private Animator mAnimator;

    private ConcurrentLinkedQueue<TempThread> mTempThreads;
    private MinAllocHashSet<TempThread> mRunningTempThreads;
    private Iterator<TempThread> mRunningTempThreadIter;
    private ArrayList<Function0<Boolean>> mListeners;
    private HashMap<Function0<Boolean>, Function0<Void>> mListenerMap;
    private MyLock mListenerLock;

    public Scheduler(String name, Context context) {
        super(name);
        this.mContext = context;
        this.mInitialized = false;        /* Initialize threads */
        this.mGameView = new GameView(context);
        this.mGameRenderer = mGameView.getGameRenderer();
        this.mLogicManager = new LogicManager("Logic Manager", this);
        //this.mMusicManager = new MusicManager(this, "Music Manager");
        this.mAnimator = new Animator("Animator");

        this.mTempThreads = new ConcurrentLinkedQueue<TempThread>();
        this.mRunningTempThreads = new MinAllocHashSet<TempThread>();
        this.mRunningTempThreadIter = this.mRunningTempThreads.iterator();
        this.mListeners = new ArrayList<Function0<Boolean>>();
        this.mListenerMap = new HashMap<Function0<Boolean>, Function0<Void>>();
        this.mListenerLock = new MyLock();
    }

    /**
     * Finish setting up the game.
     */
    public void initialize(Context context) {
        Log.i(TAG, "Initializing.");
        Log.i(TAG, "Waiting for the game renderer to be ready.");        /* Wait until the renderer is read */
        while (mGameRenderer.isReady() == false)
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        Log.i(TAG, "Starting other threads.");
        mLogicManager.start(GameThread.LoopState.RUNNING);
        //mMusicManager.start(LoopState.RUNNING);
        mAnimator.start(GameThread.LoopState.RUNNING);
        mGameRenderer.setState(GameRenderer.State.RENDERING);
    }

    protected void onRunning() {
	    /* If everything isn't initialized, initialize it */
        if (mInitialized != true) {
            initialize(mContext);
            mInitialized = true;
        }
        /* Remove any finished temp threads from the list */
        while (this.mRunningTempThreadIter.hasNext()) {
            TempThread tempThread = this.mRunningTempThreadIter.next();
            if (tempThread.isAlive() == false)
                this.mRunningTempThreadIter.remove();
        }
        ((MinAllocHashSet.MinAllocHashSetIterator) this.mRunningTempThreadIter).reset();

		/* Execute any threads in our queue */
        while (mTempThreads.isEmpty() == false) {
            TempThread thread = mTempThreads.remove();
            thread.start();
            this.mRunningTempThreads.add(thread);
        }
		/* Check listeners */
        synchronized (mListenerLock) {
            int length = mListeners.size();
            for (int i = 0; i < length; i++) {
                Function0<Boolean> listener = mListeners.get(i);
                if (listener.run() == Boolean.TRUE) {
                    mListenerMap.get(listener).run();
                }
            }
        }
    }

    /**
     * Called when the activity is about to be paused
     */
    public void onPause() {
        super.onPause();
        mGameView.onPause();
        mLogicManager.onPause();
        //mMusicManager.onPause();
        mAnimator.onPause();
        Iterator<TempThread> iter = this.mRunningTempThreads.iterator();
        while (iter.hasNext())
            iter.next().onPause();
    }

    /**
     * Called when the activity is about to be resumed
     */
    public void onResume() {
        super.onResume();
        mGameView.onResume();
        mLogicManager.onResume();
        //mMusicManager.onResume();
        mAnimator.onPause();
    }

    /**
     * Called when the activity is about to be killed
     */
    public void onDestroy() {
        super.onDestroy();
		
		/* Kill all Threads/Views */
		/* TODO: Kill Temp Threads and Running Ones */
        mGameView.onDestroy();
        ThreadUtils.KillGameThread(mLogicManager);
        ThreadUtils.KillGameThread(mAnimator);
        //ThreadUtils.KillGameThread(mMusicManager);
    }

    public synchronized void addThread(TempThread thread) {
        mTempThreads.add(thread);
    }

    /**
     * Registers a listener that calls the callback function when function f
     * returns true.
     *
     * @param f        the function we are waiting on to return true. Should be
     *                 thread safe.
     */
    public synchronized void registerListener(Function0<Boolean> f,
                                              Function0<Void> callback) {
        synchronized (mListenerLock) {
            mListeners.add(f);
            mListenerMap.put(f, callback);
        }
    }

    /*
     * Getters and Setters
     */
    public Context getContext() {
        return mContext;
    }

    public GameView getGameView() {
        return mGameView;
    }

    public GameRenderer getGameRenderer() {
        return mGameRenderer;
    }

    public LogicManager getLogicManager() {
        return mLogicManager;
    }

    /*public MusicManager getMusicManager() {
        return mMusicManager;
    } */

    public Animator getAnimator() {
        return mAnimator;
    }
}
