package com.jlreyes.libraries.android_game_engine.threading;

/**
 * Extension of the thread class used in the engine. A GameThread is a thread
 * in an infinite loop until kill() is called. Within each loop the loop state
 * can either be paused or running.
 *
 * @author jlreyes
 */
public abstract class GameThread extends Thread {
    /**
     * Represents the life state of this thread. A thread is ALIVE we want it
     * to be in its run method. It is DEAD otherwise.
     */
    private enum LifeState {
        ALIVE, DEAD
    }

    /**
     * The state of the loop. When the thread is paused, it defaults to sleeping
     * for SLEEP_TIME
     */
    public enum LoopState {
        RUNNING, PAUSED
    }

    private LifeState mLifeState;
    private LoopState mLoopState;

    protected final int SLEEP_TIME = 500;

    public GameThread(String name) {
        super(name);
        this.mLifeState = LifeState.ALIVE;
        this.mLoopState = LoopState.PAUSED;
    }

    /**
     * Starts the thread in the given loopstate.
     *
     * @param loopstate @see GameThread.LoopState
     */
    public void start(LoopState loopstate) {
        super.start();
        setLoopState(loopstate);
    }

    /**
     * Does not terminate until kill() is called.
     */
    public void run() {
        while (weWantAlive() == true) {
            switch (getLoopState()) {
                case PAUSED: {
                    onPaused();
                    break;
                }
                case RUNNING: {
                    onRunning();
                    break;
                }
            }
        }
    }

    /**
     * Called while mLoopState is paused. Defaults to sleeping for
     * SLEEP_TIME ms
     */
    private void onPaused() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called while mLoopState is running
     */
    protected abstract void onRunning();

    /**
     * Terminates the run method whenever possible.
     */
    public void kill() {
        synchronized (mLifeState) {
            this.mLifeState = LifeState.DEAD;
        }
    }

    /**
     * Returns true iff mLifeState is ALIVE
     */
    public boolean weWantAlive() {
        synchronized (mLifeState) {
            if (mLifeState == LifeState.ALIVE) return true;
            else return false;
        }
    }

    /**
     * Called before the activity is paused.
     */
    public void onPause() {
        setLoopState(LoopState.PAUSED);
    }

    /**
     * Called when the activity is resuming from sleep
     */
    public void onResume() {
        setLoopState(LoopState.RUNNING);
    }

    /**
     * Called for cleanup when the activity is going to be destroyed.
     */
    public void onDestroy() {}

	/*
	 * Getters and Setters
	 */

    /**
     * Thread-safe getter for mLoopState
     */
    public LoopState getLoopState() {
        synchronized (mLoopState) {
            return mLoopState;
        }
    }

    /**
     * Thread-safe setter for mLoopState
     */
    public void setLoopState(LoopState loopState) {
        synchronized (loopState) {
            this.mLoopState = loopState;
        }
    }
}
