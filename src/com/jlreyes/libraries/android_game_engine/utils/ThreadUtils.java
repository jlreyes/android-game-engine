package com.jlreyes.libraries.android_game_engine.utils;

import com.jlreyes.libraries.android_game_engine.threading.GameThread;

/**
 * Various utility functions for GameThreads
 *
 * @author jlreyes
 */
public class ThreadUtils {
    /**
     * Takes a Thread and returns when it has been killed
     */
    public static void KillThread(Thread thread) {
        boolean threadAlive = true;
        while (threadAlive) {
            try {
                thread.join();
                threadAlive = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Takes a GameThread and returns when it has been successfully killed
     */
    public static void KillGameThread(GameThread thread) {
        thread.onDestroy();
        thread.kill();
        KillThread(thread);
    }
}
