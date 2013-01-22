package com.jlreyes.libraries.android_game_engine.io;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.rendering.GameRenderer;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * GLSurfaceView handling input and holding the renderer.
 *
 * @author jlreyes
 */
public class GameView extends GLSurfaceView {
    private GameRenderer mGameRenderer;


    /**
     * Set containing all Runnables not yet run on the renderer thread.
     */
    private HashSet<Runnable> mRendererEvents;
    private ArrayList<MotionEvent> mInputEvents;
    private ArrayList<MotionEvent> mInputEventsMedium;

    /**
     * Minimum amount of time to wait between MotionEvents before processing
     * any other MotionEvent. To Prevent Flooding.
     */
    @SuppressWarnings("unused")
    private static final int INPUT_DELAY = 0;

    public GameView(Context context) {
        super(context);
        setEGLContextClientVersion(2); // Set OpenGL version
        setEGLConfigChooser(8, 8, 8, 8, 0, 0); // ARGB_8888
        getHolder().setFormat(PixelFormat.RGBA_8888); // ARGB_8888

        this.mGameRenderer = new GameRenderer();
        this.mRendererEvents = new HashSet<Runnable>();
        this.mInputEvents = new ArrayList<MotionEvent>();
        this.mInputEventsMedium = new ArrayList<MotionEvent>();
        setRenderer(mGameRenderer);
    }

    /**
     * Called when the Activity is about to be destroyed
     */
    public void onDestroy() {}

    /**
     * Called on touch
     */
    public boolean onTouchEvent(final MotionEvent event) {        /* Add event to input queue. */
        synchronized (mInputEvents) {
            mInputEvents.add(event);
        }
        return true;
    }


    @Override
    public void queueEvent(final Runnable r) {
        mRendererEvents.add(r);
        Runnable container = new Runnable() {
            public void run() {
                try {
                    r.run();
                } catch(Exception e) {
                    Log.w("GameView", "Error running runnable passed to the" +
                                      " render thread.", e);
                }
                GameView.this.setEventFinished(r);
            }
        };
        super.queueEvent(container);
    }

    /**
     * Called by each container runnable after it has finished
     * executing the on render thread.
     */
    private void setEventFinished(Runnable r) {
        mRendererEvents.remove(r);
    }

    /**
     * Returns true iff the given runnable is not in our set of events
     * being executed on the renderer thread.
     */
    public boolean eventFinished(Runnable r) {
        return mRendererEvents.contains(r) == false;
    }

    /*
     * Getters and Setters
     */
    public GameRenderer getGameRenderer() {
        return mGameRenderer;
    }

    /**
     * Thread Safe. To prevent allocation, we copy over elements from
     * mInputEvents to mInputEventsMedium and return that.
     */
    public ArrayList<MotionEvent> getInputEvents() {
        synchronized (mInputEvents) {
            mInputEventsMedium.clear();
				/* To prevent toArray */
            int length = mInputEvents.size();
            for (int i = 0; i < length; i++)
                mInputEventsMedium.add(mInputEvents.get(i));
            mInputEvents.clear();
            return mInputEventsMedium;
        }
    }

}
