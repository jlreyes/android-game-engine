package com.jlreyes.libraries.android_game_engine.threading;

import com.jlreyes.libraries.android_game_engine.utils.ThreadUtils;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;

public class TempThread extends Thread {
    private Scheduler mScheduler;
    /**
     * Returns true when the thread is done
     */
    private Function0<Boolean> mThreadChecker;
    /**
     * Kills the thread when the above thread returns true
     */
    private Function0<Void> mThreadKiller;
    private boolean mActivityPaused = false;

    public TempThread(final Scheduler scheduler) {
        this.mScheduler = scheduler;
        this.mThreadChecker = new Function0<Boolean>() {
            public Boolean run() {
                if (TempThread.this.getState() == Thread.State.TERMINATED)
                    return Boolean.TRUE;
                return Boolean.FALSE;
            }
        };
        this.mThreadKiller = new Function0<Void>() {
            public Void run() {
                ThreadUtils.KillThread(TempThread.this);
                return null;
            }
        };

    }

    public void start() {
        super.start();
        mScheduler.registerListener(mThreadChecker, mThreadKiller);
    }

    public void onPause() {
        this.mActivityPaused = true;
    }

    /**
     * Called when the thread is finished executing.
     */
    public void onDone() {
        ThreadUtils.KillThread(this);
    }

    public boolean activityIsPaused() {
        return this.mActivityPaused;
    }
}
