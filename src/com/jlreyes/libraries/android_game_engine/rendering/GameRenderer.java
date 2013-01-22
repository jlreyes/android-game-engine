package com.jlreyes.libraries.android_game_engine.rendering;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;

/**
 * Renderer for the GameView.
 *
 * @author jlreyes
 */
public class GameRenderer implements Renderer {
    public static enum State {RENDERING, NOT_RENDERING}

    /**
     * How long this thread will sleep when it needs to sleep
     */
    public static final int SLEEP_INTERVAL = 500;

    private volatile boolean mDrawing;
    private State mState;
    private RenderInfo mRenderInfo;
    private RenderInfo mRenderInfoBuffer;
    /**
     * Dummy thread for indexing in SyncWrapper
     */
    private Thread mRendererThread;

    /* Aspect Ratio */
    private float mRatio;
    private int mProgramHandle;
    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mRGBTexHandle;
    private int mATexHandle;
    private float[] mMVPMatrix;
    private float[] mModelMatrix;
    private float[] mViewMatrix;
    private float[] mProjectionMatrix;

    public GameRenderer() {
        this.mDrawing = false;
        this.mState = State.NOT_RENDERING;
        this.mRenderInfo = new RenderInfo();
        this.mRenderInfoBuffer = mRenderInfo;
        this.mRendererThread = new Thread("Renderer");

        this.mProgramHandle = 0;
        this.mMVPMatrixHandle = 0;
        this.mPositionHandle = 0;
        this.mTexCoordHandle = 0;
        this.mRGBTexHandle = 0;
        this.mATexHandle = 0;
        this.mRatio = 0.0f;
        this.mMVPMatrix = new float[16];
        this.mModelMatrix = new float[16];
        this.mViewMatrix = new float[16];
        this.mProjectionMatrix = new float[16];
    }

    /**
     * Returns true iff the renderer has reached the onDrawFrame.
     */
    public boolean isReady() {
        return mDrawing;
    }

    /**
     * Renders whatever information the renderer has.
     */
    public void onDrawFrame(GL10 gl) {
        mDrawing = true;
        switch (getState()) {
            case NOT_RENDERING: {
                onNotRendering();
                break;
            }
            case RENDERING: {
                onRendering(gl);
                break;
            }
        }
    }

    /**
     * Called whenever we are not rendering
     */
    private void onNotRendering() {
        try {
            Thread.sleep(SLEEP_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called whenever we need to render a frame.
     */
    @SuppressWarnings("UnusedParameters")
    private void onRendering(GL10 _) {        /* Get the latest render info */
        synchronized (mRenderInfoBuffer) {
            mRenderInfo = mRenderInfoBuffer;
        }        /* Render the Scene */
        synchronized (mRenderInfo) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            ArrayList<Renderable> renderables = mRenderInfo.getRenderables();
            int length = renderables.size();
            for (int i = 0; i < length; i++) {
                Renderable renderable = renderables.get(i);
                renderable.draw(mPositionHandle,
                                mMVPMatrixHandle,
                                mTexCoordHandle,
                                mRGBTexHandle,
                                mATexHandle,
                                mRatio,
                                mMVPMatrix,
                                mViewMatrix,
                                mModelMatrix,
                                mProjectionMatrix);
            }
        }
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        LayerCamera.SetDefaultViewMatrix(mViewMatrix);
        setUpTweaks();
        setUpShaders();
        getHandles();
        GLES20.glUseProgram(mProgramHandle);
    }

    /**
     * Updates the viewport and the stored ratio.
     */
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    private void setUpTweaks() {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		/* Blending and anti-aliasing */
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void setUpShaders() {
        String vertexShader = RenderUtils.VERTEX_SHADER;
        String fragmentShader = RenderUtils.FRAGMENT_SHADER;
        String[] attributes = new String[]{"a_Position", "a_TexCoordinate"};
        int vertexHandle =
                RenderUtils.compileShader(GLES20.GL_VERTEX_SHADER,
                                          vertexShader);
        int fragmentHandle =
                RenderUtils.compileShader(GLES20.GL_FRAGMENT_SHADER,
                                          fragmentShader);
        mProgramHandle =
                RenderUtils.createAndLinkProgram(vertexHandle,
                                                 fragmentHandle,
                                                 attributes);
    }

    public void getHandles() {
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle,
                                                       "u_MVPMatrix");
        mRGBTexHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_RGBTexture");
        mATexHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_ATexture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle,
                                                     "a_Position");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgramHandle,
                                                     "a_TexCoordinate");
	    /* Verify we got the location */
        if (mMVPMatrixHandle == -1)
            throw new RuntimeException("Could not get MVP matrix handle.");
        if (mRGBTexHandle == -1)
            throw new RuntimeException("Could not get RGB Tex handle.");
        if (mATexHandle == -1)
            throw new RuntimeException("Could not get A Tex handle.");
        if (mPositionHandle == -1)
            throw new RuntimeException("Could not get position handle.");
        if (mTexCoordHandle == -1)
            throw new RuntimeException("Could not get Tex Coord handle.");
    }


    /**
     * Updates the camera and renderables safely.
     */
    public void update(RenderInfo renderInfo) {
        synchronized (mRenderInfoBuffer) {
            this.mRenderInfoBuffer = renderInfo;
        }
    }
	
	/*
	 * Getters and Setters
	 */

    /**
     * Thread Safe
     */
    public State getState() {
        synchronized (mState) {
            return mState;
        }
    }

    /**
     * Thread Safe
     */
    public void setState(State state) {
        synchronized (mState) {
            this.mState = state;
        }
    }

    public synchronized Thread getThread() {
        return mRendererThread;
    }
}
