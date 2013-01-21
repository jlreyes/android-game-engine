package com.jlreyes.libraries.android_game_engine.threading;

import android.view.View;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

public class ViewUpdater extends Thread {
    /**
     * Default time to wait between each loop
     */
    public static final long DEFAULT_POLL_INTERVAL = 100;

    private View mView;
    private Runnable mUpdateRunnable;
    private long mPollInterval;

    private boolean mKilled;

    /**
     * Same as ViewUpdater(View, Function1<View, Void>, {@link ViewUpdater#DEFAULT_POLL_INTERVAL})
     */
    public ViewUpdater(View view, Function1<View, Void> updateFunction) {
        this(view, updateFunction, DEFAULT_POLL_INTERVAL);
    }

    /**
     * Thread that updates the given view every pollInterval milliseconds
     * with the give updateFunction that takes the view as its argument.
     */
    public ViewUpdater(View view,
                       final Function1<View, Void> updateFunction,
                       long pollInterval) {
        this.mView = view;
        this.mPollInterval = pollInterval;
        this.mKilled = false;
        this.mUpdateRunnable = new Runnable() {
            public void run() {
                updateFunction.run(mView);
            }
        };
        start();
    }

    public void run() {
        while (mKilled == false) {
            if (this.mView == null) return;
            mView.post(mUpdateRunnable);
            try {
                Thread.sleep(mPollInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void kill() {
        this.mKilled = true;
    }
}
