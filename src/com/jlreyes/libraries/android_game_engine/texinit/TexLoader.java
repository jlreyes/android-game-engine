package com.jlreyes.libraries.android_game_engine.texinit;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TextureLoader;
import com.jlreyes.libraries.android_game_engine.threading.ViewUpdater;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;


public class TexLoader extends Thread {
    public static final String TAG = "Tex Load Activity";
    private static final String mLoadInfoStart = "Loading ";

    private TextureInitActivity mActivity;
    private volatile int mPercentDone;
    private volatile String mLoadInfo;

    private ViewUpdater mProgressBarUpdater;
    private ViewUpdater mPercentTextUpdater;
    private ViewUpdater mLoadInfoTextUpdater;

    public TexLoader(TextureInitActivity activity,
                     ProgressBar progressBar,
                     TextView percentText,
                     TextView loadInfoText) {
        this.mActivity = activity;
        this.mPercentDone = 0;
        this.mLoadInfo = "Please wait...";
        this.mProgressBarUpdater = new ViewUpdater(progressBar,
                                                   progressBarFunction());
        this.mPercentTextUpdater = new ViewUpdater(percentText,
                                                   percentTextFunction());
        this.mLoadInfoTextUpdater = new ViewUpdater(loadInfoText,
                                                    loadInfoTextFunction());
    }

    private Function1<View, Void> progressBarFunction() {
        return new Function1<View, Void>() {
            public Void run(View view) {
                ProgressBar progressBar = (ProgressBar) view;
                int percentDone = TexLoader.this.getPercentDone();
                progressBar.setProgress(percentDone);
                return null;
            }
        };
    }

    private Function1<View, Void> percentTextFunction() {
        return new Function1<View, Void>() {
            public Void run(View view) {
                TextView t = (TextView) view;
                String percent = String.valueOf(TexLoader.this.getPercentDone());
                t.setText(percent.toCharArray(), 0, percent.length());
                return null;
            }
        };
    }

    private Function1<View, Void> loadInfoTextFunction() {
        return new Function1<View, Void>() {
            public Void run(View view) {
                TextView t = (TextView) view;
                String loadInfo = TexLoader.this.getLoadInfo();
                t.setText(loadInfo.toCharArray(), 0, loadInfo.length());
                return null;
            }
        };
    }

    public void run() {
        /* Loading each texture */
        TexController.TexInfo[] textures = TexController.TEXTURES;
        int numTextures = textures.length;
        int numLoaded = 0;
        for (TexController.TexInfo texInfo : textures) {
            /* Updating Load info */
            mLoadInfo = texInfo.Name;
            /* Loading */
            TextureLoader.LoadTextureForVerification(texInfo, mActivity);
            numLoaded += 1;
            mPercentDone = (numLoaded * 100) / numTextures;
        }
        mProgressBarUpdater.kill();
        mPercentTextUpdater.kill();
        mLoadInfoTextUpdater.kill();
        mActivity.startGame();
    }

    public int getPercentDone() {
        return mPercentDone;
    }

    public String getLoadInfo() {
        return mLoadInfoStart + mLoadInfo;
    }
}
