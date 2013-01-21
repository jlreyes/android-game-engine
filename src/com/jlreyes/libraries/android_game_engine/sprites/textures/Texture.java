package com.jlreyes.libraries.android_game_engine.sprites.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import com.jlreyes.libraries.android_game_engine.io.GameView;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.TexType;
import com.jlreyes.libraries.android_game_engine.utils.Utils;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.StrictGLException;
import com.jlreyes.libraries.android_game_engine.utils.math.MathMatrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;


/**
 * Texture that can be attached to a sprite. Dynamically made out of
 * TextureParts which are individual texture images making up the entire image.
 *
 * @author jlreyes
 */
public class Texture {
    /**
     * Represents a part of a larger texture. Is a row x col matrix
     * where each index is of type Frame.
     *
     * @author jlreyes
     */
    public static class TexturePart {
        private GameView mGameView;
        private MathMatrix<TextureState.Frame> mFrameMatrix;
        private int mRGBHandle;
        private int mAHandle;
        private int mFrameWidth;
        private int mFrameHeight;
        private int mPartWidth;
        private int mPartHeight;

        /**
         * Creates a new texture part
         */
        public TexturePart(MathMatrix<TextureState.Frame> frameMatrix,
                           int frameWidth, int frameHeight) {
            this.mFrameMatrix = frameMatrix;
            this.mFrameWidth = frameWidth;
            this.mFrameHeight = frameHeight;
            this.mPartWidth = mFrameMatrix.getNumCols() * frameWidth;
            this.mPartHeight = mFrameMatrix.getNumRows() * frameHeight;
            this.mRGBHandle = 0;
            this.mAHandle = 0;            /* Making sure each frame in frameMatrix is owned by this part */
            int rows = frameMatrix.getNumRows();
            int cols = frameMatrix.getNumCols();
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++) {
                    TextureState.Frame frame = frameMatrix.get(i, j);
                    if (frame != null) frame.setTexturePart(this);
                }
        }

        /**
         * Registers this texture part with opengl, must be run on the render
         * thread to work.
         */
        public void registerWithOpenGL() throws StrictGLException {
            int[] handleHolder = new int[2];
            GLES20.glGenTextures(2, handleHolder, 0);
            this.mRGBHandle = handleHolder[0];
            this.mAHandle = handleHolder[1];
            if (this.mRGBHandle == 0 || this.mAHandle == 0)
                throw new StrictGLException("Registration failed for texture" +
                                            " part.");
        }

        /**
         * Frees up resources by calling glDeleteTextures on our handles
         */
        public void unregisterWithOpenGL() throws StrictGLException {
            if (mRGBHandle <= 0 || mAHandle <= 0)
                throw new StrictGLException("Attempting to unregister a" +
                                            " texture part that is not been" +
                                            "registered with openGL");
            GLES20.glDeleteTextures(2, new int[]{mRGBHandle, mAHandle}, 0);
        }

        public Bitmap[] generateBitmaps(Context context) {
            Bitmap bitmap = generateARGBBitmap(context);
            Bitmap rgbBitmap = bitmap;
            Bitmap aBitmap = bitmap.extractAlpha();
            Bitmap[] bitmaps = new Bitmap[]{rgbBitmap, aBitmap};
            return bitmaps;

        }

        public Bitmap generateARGBBitmap(Context context) {            /* Generating bitmap */
            Bitmap bitmap = Bitmap.createBitmap(mPartWidth, mPartHeight,
                                                Bitmap.Config.ARGB_8888);
            int numFramesHorz = mPartWidth / mFrameWidth;
            int numFramesVert = mPartHeight / mFrameHeight;
            for (int i = 0; i < numFramesVert; i++) {
                for (int j = 0; j < numFramesHorz; j++) {
                    TextureState.Frame frame = mFrameMatrix.get(i, j);
                    Bitmap frameBitmap = null;
                    if (frame != null) {
                        TextureState state = frame.getState();
                        int frameNum = frame.getStateFrame();
                        frameBitmap = state.generateFrameBitmap(context,
                                                                frameNum);
                    } else {
				        /* This is a blank frame, generate blank bitmap */
                        frameBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight,
                                                          Bitmap.Config.ARGB_8888);
                    }
					/* Copy the pixels from stateBitmap to the correct position
					 * in bitmap */
                    for (int k = 0; k < mFrameHeight; k++) {
                        for (int l = 0; l < mFrameWidth; l++) {
                            int stateColor = frameBitmap.getPixel(l, k);
                            bitmap.setPixel(j * mFrameWidth + l,
                                            i * mFrameHeight + k,
                                            stateColor);
                        }
                    }
                    frameBitmap.recycle();
                }
            }
            return bitmap;
        }

        /**
         * Updates each frame in this TexturePart to render using the given
         * render type and to use the given number of vertices
         */
        public void updateVertices(Renderable.RenderType renderType, int numVertices) {
            switch (renderType) {
                case TRIANGLE_STRIP: {
                    updateVerticesToTriangleStrip(numVertices);
                    break;
                }
                default:
                    throw new RuntimeException("Unsupported render type.");
            }

        }

        private void updateVerticesToTriangleStrip(int numVertices) {
		    /* Iterating through frames */
            int rows = mFrameMatrix.getNumRows();
            int cols = mFrameMatrix.getNumCols();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    TextureState.Frame frame = mFrameMatrix.get(row, col);
                    if (frame != null) {
                        /* Create indices */
                        float[] indices = new float[numVertices * 2]; // (s, t) for each vertex
                        int halfNumVertices = numVertices / 2;
                        float deltaX = (float) mFrameWidth / (float) (halfNumVertices - 1);
                        for (int i = 0; i < halfNumVertices; i++) {
                            float x = (float) col * (float) mFrameWidth + (deltaX * (float) i);
                            float bottomY = ((float) mFrameHeight * (float) (row + 1));
                            float topY = bottomY + (float) mFrameHeight;
                            /* (s, t) ratio coordinates */
                            indices[4 * i] = x / (float) mPartWidth;
                            indices[4 * i + 1] = bottomY / (float) mPartHeight;
                            indices[4 * i + 2] = x / (float) mPartWidth;
                            indices[4 * i + 3] = topY / (float) mPartHeight;
                        }
                        /* Create a float buffer for the indices */
                        ByteBuffer b = ByteBuffer.allocateDirect(indices.length *
                                                                 Utils.FLOAT_BYTES);
                        b.order(ByteOrder.nativeOrder());
                        FloatBuffer f = b.asFloatBuffer();
                        f.put(indices).position(0);
                        frame.setIndices(f);
                    }
                }
            }
        }

        /*
         * Getters and Setters
         */

        public int getNumRows() {
            return mFrameMatrix.getNumRows();
        }

        public int getNumCols() {
            return mFrameMatrix.getNumCols();
        }

        public TextureState.Frame getFrame(int row, int col) {
            return mFrameMatrix.get(row, col);
        }

        public int getRGBHandle() {
            if (mRGBHandle <= 0)
                throw new RuntimeException("This texture part hasn't been " +
                                           "registered with openGL!");
            return mRGBHandle;
        }

        public int getAHandle() {
            if (mAHandle <= 0)
                throw new RuntimeException("This texture part hasn't been " +
                                           "registered with openGL!");
            return mAHandle;
        }

        public int getPartWidth() {
            return mPartWidth;
        }

        public int getPartHeight() {
            return mPartHeight;
        }
    }

    public static final String TAG = "Texture";

    private TexController.TexInfo mTexInfo;
    private TexturePart[] mTextureParts;
    private HashMap<TexController.TexStateInfo, TextureState> mStates;
    private TextureState mActiveState;
    /* Texture information */
    private int mFrameWidth;
    private int mFrameHeight;


    public static final int VERTEX_DIM = 2;

    /**
     * Create a texture.
     */
    public Texture(TexController.TexInfo texInfo,
                   TexturePart[] textureParts,
                   TextureState[] states,
                   TextureState defaultState,
                   int framewidth,
                   int frameHeight) {
        this.mTexInfo = texInfo;
        this.mFrameWidth = framewidth;
        this.mFrameHeight = frameHeight;
        this.mActiveState = defaultState;
        this.mStates = new HashMap<TexController.TexStateInfo, TextureState>();
        this.mTextureParts = textureParts;
		/* Generate states hashmap */
        TexController.TexStateInfo[] statesInfo = texInfo.States;
        int numStates = statesInfo.length;
        for (int i = 0; i < numStates; i++)
            mStates.put(statesInfo[i], states[i]);
    }

    /**
     * Registers the texture on the rendering thread. Does not return until
     * finished registering.
     */
    public void registerWithOpenGL(GameView gameView,
                                   final TexType[] rgbTexs,
                                   final TexType[] aTexs) {
		/* Create runnable to pass to render thread */
        Runnable r = new Runnable() {
            public void run() {
                int length = mTextureParts.length;
                for (int i = 0; i < length; i++) {
                    TexturePart texturePart = mTextureParts[i];
                    TexType rgbTex = rgbTexs[i];
                    TexType aTex = aTexs[i];
					/* Generate texture part handles */
                    try {
                        texturePart.registerWithOpenGL();
                    } catch (StrictGLException e) {
                        throw new RuntimeException("Texture Part " + i +
                                                   "'s registration failed" +
                                                   " for texture " + this);
                    }
					/* Load RGB Texture */
                    int rgbHandle = texturePart.getRGBHandle();
                    rgbTex.register(rgbHandle);
					/* Load alpha Texture */
                    int aHandle = texturePart.getAHandle();
                    aTex.register(aHandle);
                }
            }
        };
		/* Queue Event and wait for it to finish */
        gameView.queueEvent(r);
        while (gameView.eventFinished(r) == false) {
            try {
                Thread.sleep(10l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
	
	/*
	 * Deletion
	 */

    /**
     * Deletes this texture and free up resources. Runs on the renderer thread.
     * Does not return until the texture is unregistered.
     */
    public void unregisterWithOpenGL(GameView gameView) {
        Runnable r = new Runnable() {
            public void run() {
                for (TexturePart texPart : mTextureParts) {
                    try {
                        texPart.unregisterWithOpenGL();
                    } catch (StrictGLException e) {
                        throw new RuntimeException("Texture unregisration " +
                                                   "failed for texture " +
                                                   this);
                    }
                }
            }
        };
		/* Queue event and wait for it to finish. */
        gameView.queueEvent(r);
        while (gameView.eventFinished(r) == false) {
            try {
                Thread.sleep(10l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Static Methods
     */
    public static Bitmap GenerateDefaultAlphaBitmap() {
		/* Bitmap info */
        int width = 4;
        int height = 4;
        Bitmap.Config config = Bitmap.Config.ALPHA_8;
        int numPixels = width * height;
		/* Creating the colors */
        int[] colors = new int[numPixels];
        for (int i = 0; i < numPixels; i++)
            colors[i] = 0xFFFFFFFF;
        Bitmap bitmap = Bitmap.createBitmap(colors, width, height, config);
        return bitmap;
    }
	
	/*
	 * Body
	 */

    /**
     * Updates this texture's frame's indices to match the indices with the given
     * number of vertices.
     *
     * @param renderType TRIANGLE_STRIP ONLY
     */
    public void updateVertices(Renderable.RenderType renderType, int numVertices) {
        if (renderType != Renderable.RenderType.TRIANGLE_STRIP)
            throw new RuntimeException("TRIANGLE_STRIP only supported type");
        for (TexturePart texPart : mTextureParts)
            texPart.updateVertices(renderType, numVertices);
    }

    public void setActiveState(TexController.TexStateInfo stateInfo) {
        synchronized (this) {
            TextureState state = mStates.get(stateInfo);
            if (state == null)
                throw new RuntimeException(stateInfo.Name + " is not a valid " +
                                           " state type for texture " + this);
            this.mActiveState = state;
        }
    }

    public int getActiveRGBHandle() {
        TextureState.Frame activeFrame = mActiveState.getActiveFrame();
        TexturePart activeTexturePart = activeFrame.getTexturePart();
        return activeTexturePart.getRGBHandle();
    }

    public int getActiveAHandle() {
        TextureState.Frame activeFrame = mActiveState.getActiveFrame();
        TexturePart activeTexturePart = activeFrame.getTexturePart();
        return activeTexturePart.getAHandle();
    }

    public TextureState getActiveState() {
        return mActiveState;
    }

    /**
     * Outputs the correct indices in the texture to render.
     */
    public FloatBuffer getActiveIndices() {
        TextureState.Frame activeFrame = mActiveState.getActiveFrame();
        FloatBuffer indices = activeFrame.getIndices();
        if (indices == null) throw new RuntimeException("Indices are null," +
                                                        " but a sprite is " +
                                                        " attached! Wut?!");
        return indices;
    }
	
	/*
	 * Getters and Setters
	 */
    public String toString() {
        return this.mTexInfo.toString();
    }

    public TexController.TexInfo getTexInfo() {
        return mTexInfo;
    }

    public TexturePart[] getTextureParts() {
        return mTextureParts;
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }
}