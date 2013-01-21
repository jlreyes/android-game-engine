package com.jlreyes.libraries.android_game_engine.sprites.textures.types;

import android.opengl.GLES20;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.InvalidTypeException;

import java.io.IOException;

public abstract class TexType {
    public static enum Type {ETC1, BITMAP}

    private boolean mIsRecycled;

    public TexType() {
        this.mIsRecycled = false;
    }

    public void register(int handle) {
        checkRecycled();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                               GLES20.GL_TEXTURE_MIN_FILTER,
                               GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                               GLES20.GL_TEXTURE_MAG_FILTER,
                               GLES20.GL_LINEAR);
        texImage2D();
    }

    public abstract void texImage2D();

    public byte[] toByteArray()
            throws IOException,
                   InvalidTypeException {
        checkRecycled();
        return generateByteArray();
    }

    protected abstract byte[] generateByteArray()
            throws IOException,
                   InvalidTypeException;

    public boolean isRecycled() {
        return mIsRecycled;
    }

    public void recycle() {
        onRecycle();
        this.mIsRecycled = true;
    }

    protected abstract void onRecycle();

    protected void checkRecycled() {
        if (mIsRecycled == true)
            throw new RuntimeException(this + " is recycled.");
    }
}
