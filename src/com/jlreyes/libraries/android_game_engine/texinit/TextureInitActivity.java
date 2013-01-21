package com.jlreyes.libraries.android_game_engine.texinit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;

/**
 * Activity handling texture initialization.
 * <p/>
 *
 * @author jlreyes
 */
public abstract class TextureInitActivity extends Activity {
    public static final String TAG = "TextureInitActivity";

    private Class<?> mReturnClass;
    private float mScale;
    private ProgressBar mProgressBar;
    private TextView mPercentTextView;
    private TextView mLoadInfoView;

    private int toPx(int dp) {
        return (int) (dp * this.mScale + 0.5f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        this.mScale = this.getResources().getDisplayMetrics().density;

        /* Make sure a return class name was passed.  */
        Intent intent = this.getIntent();
        this.mReturnClass = (Class<?>) intent.getSerializableExtra("returnClass");
        if (this.mReturnClass == null) {
            Log.w(TAG, "No return class passed");
            this.finish();
            return;
        }

        /* Set up the layout */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(this.generateLayout());

        /* Start the texture loading */
        Log.i(TAG, "Starting texture loader");
        TexController.RESOLUTION = this.getResolution();
        TexController.TEXTURES = this.getTextures();
        TexLoader texLoader = new TexLoader(this,
                                            this.getProgressBar(),
                                            this.getPercentTextView(),
                                            this.getLoadInfoView());
        texLoader.start();
    }

    public void startGame() {
        Log.i(TAG, "Starting Game");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, this.mReturnClass);
        intent.putExtra("texInitSkip", true);
        this.startActivity(intent);
        finish();
    }

    /**
     * Returns the resolution of textures we will load */
    public abstract TexController.Resolution getResolution();

    /**
     * Returns an array of all textures used by the application.
     */
    public abstract TexController.TexInfo[] getTextures();

    /**
     * Returns font info
     * TODO
     */
    //public abstract TexController.FontTexInfo[] getFonts();

    /**
     * Returns the linear layout for this activity
     */
    protected LinearLayout generateLayout() {
        /* Create the linear layout */
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(toPx(107), 0, toPx(107), 0);

        /* Create the loading text */
        TextView loadText = new TextView(this);
        loadText.setGravity(Gravity.CENTER_HORIZONTAL);
        loadText.setPadding(0, 0, 0, toPx(20));
        loadText.setText("Loading");
        loadText.setTextAppearance(this, android.R.attr.textAppearanceLarge);
        layout.addView(loadText);

        /* Create the progress container */
        LinearLayout progressContainer = new LinearLayout(this);
        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT);
        progressContainer.setLayoutParams(progressParams);
        layout.addView(progressContainer);

        /* Create the progress bar */
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        ViewGroup.LayoutParams progressBarParams =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                           ViewGroup.LayoutParams.WRAP_CONTENT);
        progressBar.setLayoutParams(progressBarParams);
        progressContainer.addView(progressBar);

        /* Create the percent text */
        TextView percentText = new TextView(this);
        LinearLayout.LayoutParams percentTextParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                              ViewGroup.LayoutParams.WRAP_CONTENT);
        percentTextParams.setMargins(toPx(4), 0, 0, 0);
        percentTextParams.weight = 0;
        percentText.setLayoutParams(percentTextParams);
        percentText.setHint("0");
        progressContainer.addView(percentText);

        /* Create the percent symbol */
        TextView percentSymbol = new TextView(this);
        LinearLayout.LayoutParams percentParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT);
        percentSymbol.setLayoutParams(percentParams);
        percentSymbol.setText("%");
        percentSymbol.setTextAppearance(this, android.R.attr.textAppearanceSmall);
        progressContainer.addView(percentSymbol);

        /* Create the load info text */
        TextView loadInfoText = new TextView(this);
        LinearLayout.LayoutParams loadInfoParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT);
        loadInfoParams.gravity = Gravity.CENTER_HORIZONTAL;
        loadInfoText.setLayoutParams(loadInfoParams);
        loadInfoText.setHint("Please Wait...");
        loadInfoText.setTextAppearance(this, android.R.attr.textAppearanceMedium);
        layout.addView(loadInfoText);

        /* Storing references */
        this.mProgressBar = progressBar;
        this.mPercentTextView = percentText;
        this.mLoadInfoView = loadInfoText;

        return layout;
    }

    /**
     * Returns the ProgressBar displaying how much loading we have done.
     * Return null for no progress bar.
     */
    protected ProgressBar getProgressBar() {
        return this.mProgressBar;
    }

    /**
     * Returns the TextView displaying the current percent that we are at
     * from 0-100
     * Return null for percent text
     */
    protected TextView getPercentTextView() {
        return this.mPercentTextView;
    }

    /**
     * Returns the TextView displaying the current texture file we are loading.
     * Return null for no display info
     */
    protected TextView getLoadInfoView() {
        return this.mLoadInfoView;
    }
}
