package com.jlreyes.libraries.android_game_engine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.jlreyes.libraries.android_game_engine.scenes.SceneController;
import com.jlreyes.libraries.android_game_engine.texinit.TextureInitActivity;
import com.jlreyes.libraries.android_game_engine.threading.GameThread;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.utils.ThreadUtils;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function2;

/**
 * Author: jlreyes
 * Date: 1/8/13
 * Time: 5:15 PM
 */
public abstract class AGEActivity extends Activity {
    public static final String TAG = "AGEActivity";

    private Scheduler mScheduler;
    private static boolean onCreateCalled = false;

    /* Callback that is executed whenever there is an exception in any
     * thread */
    public static Function2<Context, Throwable, Void> ON_EXCEPTION;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        /* Exception handling TODO */
        //setExceptionHandler();

        /* We skip texture initialization iff the texInitSkip boolean is true
         * in the intent that created this acticity */
        Intent intent = this.getIntent();
        boolean texInitSkip = intent.getBooleanExtra("texInitSkip", false);
        if (texInitSkip == false) {
            Intent texInitIntent = new Intent(this, getTexInitActivityClass());
            texInitIntent.putExtra("returnClass", this.getClass());
            this.startActivity(texInitIntent);
            this.finish();
            return;
        }

        this.onCreateCalled = true;


        /* Set up window */
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                  WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /* Let the client set up */
        this.init();
        SceneController.START_SCENE = this.getStartSceneInfo();
        SceneController.LOAD_SCENE = this.getLoadSceneInfo();
        SceneController.InitLoadSceneConstructor();

        /* Start the scheduler */
        this.mScheduler = new Scheduler("Scheduler", this);
        this.setContentView(this.mScheduler.getGameView());
        this.mScheduler.start(GameThread.LoopState.RUNNING);
    }

    /**
     * Returns the Texture Initialization Activity class.
     */
    protected abstract Class<? extends TextureInitActivity> getTexInitActivityClass();

    /**
     * Returns the Starting Scene's Info
     */
    protected abstract SceneController.SceneInfo getStartSceneInfo();

    /**
     * Returns load scene info
     */
    protected abstract SceneController.SceneInfo getLoadSceneInfo();

    /**
     * Called as soon as the activity is initialized. Do any initialization
     * needed here.
     */
    protected abstract void init();

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        mScheduler.onPause();

    	/* onCreate could be called, we need to monitor if it is */
        this.onCreateCalled = false;
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

    	/* We don't want onResume to be called the first time we load. The
         * Activity lifecycle dictates that it will be. */
        if (this.onCreateCalled == false) mScheduler.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        if (this.mScheduler != null)
            ThreadUtils.KillGameThread(mScheduler);
    }
}