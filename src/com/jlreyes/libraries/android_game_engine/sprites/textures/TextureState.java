package com.jlreyes.libraries.android_game_engine.sprites.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture.TexturePart;

import java.nio.FloatBuffer;

/**
 * Represents a state that the texture can be in. Each state is broken
 * into frames, each frame contains a reference to the texturePart and
 * the partIndex that
 *
 * @author jlreyes
 */
public class TextureState {
    /**
     * Frame in a state.
     *
     * @author jlreyes
     */
    public static class Frame {
        /* For quick access */
        private TextureState mState;
        private long mTimeLength;
        private int mFrame;
        private TexturePart mTexturePart;
        private FloatBuffer mIndices;

        public Frame(TextureState state, int frame, long timeLength) {
            this.mState = state;
            this.mFrame = frame;
            this.mTimeLength = timeLength;
        }

        /*
         * Getters and Setters
         */
        public void setState(TextureState state, int frame) {
            this.mState = state;
        }

        public long getTimeLength() {
            return mTimeLength;
        }

        public TextureState getState() {
            return mState;
        }

        public int getStateFrame() {
            return mFrame;
        }

        public void setTexturePart(TexturePart texturePart) {
            this.mTexturePart = texturePart;
        }

        public TexturePart getTexturePart() {
            return mTexturePart;
        }

        public void setIndices(FloatBuffer f) {
            this.mIndices = f;
        }

        public FloatBuffer getIndices() {
            return mIndices;
        }
    }

    private String mName;
    private int mResourceId;
    private TexturePart mTexturePart;
    private long mLastTimeStepped;
    private Frame[] mFrames;
    private int mFrameWidth;
    private int mFrameHeight;
    private int mActiveFrame;

    public TextureState(TexController.TexStateInfo stateInfo,
                        int frameWidth, int frameHeight) {
        this(stateInfo.Name,
             stateInfo.ResourceId(),
             stateInfo.FrameTimes,
             frameWidth,
             frameHeight);
    }

    public TextureState(String name,
                        int resourceId,
                        int[] timings,
                        int frameWidth,
                        int frameHeight) {
        this.mName = name;
        this.mResourceId = resourceId;
        this.mLastTimeStepped = System.currentTimeMillis();
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
        this.mActiveFrame = 0;        /* Creating frames */
        int numFrames = timings.length;
        this.mFrames = new Frame[numFrames];
        for (int i = 0; i < numFrames; i++)
            mFrames[i] = new Frame(this,
                                   i,
                                   timings[i]);
    }

    /**
     * Step a frame in the state.
     */
    public void step() {
        mActiveFrame += 1;
        if (mActiveFrame >= mFrames.length) mActiveFrame = 0;
        mLastTimeStepped = System.currentTimeMillis();
    }

    public Bitmap generateBitmap(Context context) {        /* Options for generating the bitmaps */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                                                     mResourceId, options);
        return bitmap;
    }

    public Bitmap generateFrameBitmap(Context context, int frame) {
        Bitmap stateBitmap = generateBitmap(context);
        int xOffset = mFrameWidth * frame;
		/* Copying the necessary pixels from stateBitmap to a new bitmap */
        Bitmap bitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight,
                                            stateBitmap.getConfig());
        for (int i = 0; i < mFrameHeight; i++) {
            for (int j = 0; j < mFrameWidth; j++) {
                int color = stateBitmap.getPixel(xOffset + j, i);
                bitmap.setPixel(j, i, color);
            }
        }
        stateBitmap.recycle();
        return bitmap;
    }

    /*
     * Getters and Setters
     */
    public void setTexturePart(TexturePart texturePart) {
        this.mTexturePart = texturePart;
    }

    public String getName() {
        return mName;
    }

    public long getLastTimeStepped() {
        return mLastTimeStepped;
    }

    public Frame getActiveFrame() {
        return mFrames[mActiveFrame];
    }

    public Frame getFrame(int index) {
        return mFrames[index];
    }

    public int getNumFrames() {
        return mFrames.length;
    }
}
