package com.jlreyes.libraries.android_game_engine.sprites.textures.types;

import android.graphics.Bitmap;
import android.opengl.ETC1Util;
import android.opengl.GLES20;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ETC1TexType extends TexType {
    private ETC1Util.ETC1Texture mTex;

    public ETC1TexType(Bitmap bitmap) {
        super();        /* Figure out the number of bytes per pixel. */
        int pixelBytes = 0;
        Bitmap.Config config = bitmap.getConfig();
        switch (config) {
            case ARGB_8888: {
                this.mTex = FromARGBBitmap(bitmap);
                bitmap.recycle();
                return;
            }
            case RGB_565: {
                pixelBytes = 3;
                break;
            }
            case ALPHA_8: {
                throw new RuntimeException("Cannot use ETC1 to compress ALPHA_8");
            }
        }        /* Bitmap info */
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
		/* Creating a buffer */
        ByteBuffer pixels =
                ByteBuffer.allocateDirect(width * height * pixelBytes)
                          .order(ByteOrder.nativeOrder());
        bitmap.copyPixelsToBuffer(pixels);
        pixels.position(0);
		/* Create the texture */
        mTex = ETC1Util.compressTexture(pixels,
                                        width,  // width in pixels
                                        height, // height in pixels
                                        pixelBytes, // pixel size
                                        width * pixelBytes); // stride
        bitmap.recycle();
    }

    /**
     * Create a ETC1 texture from a correctly formatted input stream.
     * @throws IOException
     */
    public ETC1TexType(InputStream input) throws IOException {
        super();
        this.mTex = ETC1Util.createTexture(input);
    }

    /**
     * Create a new ETC1Texture from the given ARGB bitmap. We ignore
     * the alpha channel.
     */
    public static ETC1Util.ETC1Texture FromARGBBitmap(Bitmap bitmap) {
        int pixelBytes = 3;
		/* Bitmap info */
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
		/* Creating a buffer */
        ByteBuffer pixels =
                ByteBuffer.allocateDirect(width * height * pixelBytes)
                          .order(ByteOrder.nativeOrder());
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int color = bitmap.getPixel(j, i);
                for (int k = 1; k <= pixelBytes; k++) {
                    byte channel = (byte) ((color >> (8 * (pixelBytes - k))) & 0xFF);
                    pixels.put(channel);
                }
            }
        }
        pixels.position(0);
        return ETC1Util.compressTexture(pixels,
                                        width,
                                        height,
                                        pixelBytes,
                                        width * pixelBytes);
    }

    @Override
    protected byte[] generateByteArray()
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ETC1Util.writeTexture(mTex, bytes);
        return bytes.toByteArray();
    }

    @Override
    public void texImage2D() {
        ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D,
                             0, // Tex level
                             0, // Tex border size
                             GLES20.GL_RGB, // Fallback format
                             GLES20.GL_UNSIGNED_BYTE, // 24 bit fallback
                             mTex);
    }

    public void onRecycle() {
        this.mTex = null;
    }

}
