package com.jlreyes.libraries.android_game_engine.sprites.textures.types;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.jlreyes.libraries.android_game_engine.io.storage.StorageHelper;
import com.jlreyes.libraries.android_game_engine.io.storage.external.ExternalStorageHelper;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.DataStreamEndedEarlyException;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.FileCorruptedException;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.InvalidTypeException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Bitmap texture type.
 *
 * Data is stored within a file as follows:
 * ByteOffset : Description
 * 0 (byte) : Bitmap.Config format. 1 => ALPHA_8, 4 => ARGB_888
 * 1 (int) : Bitmap width
 * 5 (int) : Bitmap height
 * 9 (?) : Pixel data. Stored in either ALPHA_8 format or ARGB_8888 format.
 *
 * @author jlreyes
 */
public class BitmapTexType extends TexType {
    public static final String TAG = "Bitmap Tex Type";
    public static final int HEADER_SIZE = 9;

    private Bitmap mBitmap;

    public BitmapTexType(Bitmap bitmap) {
        super();
        this.mBitmap = bitmap;
    }

    /**
     * Creates a new bitmap tex type from the given input stream.
     * @throws DataStreamEndedEarlyException
     * @throws IOException
     * @throws FileCorruptedException
     */
    public BitmapTexType(BufferedInputStream inputStream)
            throws IOException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException {
        super();        /* Get bitmap config */
        Bitmap.Config config = null;
        byte b = ExternalStorageHelper.ReadByteFromInputStream(inputStream);
        if (b == 1) config = Bitmap.Config.ALPHA_8;
        else if (b == 4) config = Bitmap.Config.ARGB_8888;
        else throw new FileCorruptedException();        /* Get width and height */
        int width = ExternalStorageHelper.ReadIntFromInputStream(inputStream);
        int height = ExternalStorageHelper.ReadIntFromInputStream(inputStream);
	    /* Create bitmap */
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        byte[] pixels = new byte[width * height * b];
        inputStream.read(pixels);
        ByteBuffer pixelBuffer = ByteBuffer.wrap(pixels);
        bitmap.copyPixelsFromBuffer(pixelBuffer);
        this.mBitmap = bitmap;
    }

    @Override
    public void texImage2D() {
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
    }

    @Override
    protected byte[] generateByteArray()
            throws IOException,
                   InvalidTypeException {
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        /* Determing the depth */
        byte depth = 0;
        Bitmap.Config config = mBitmap.getConfig();
        if (config == Bitmap.Config.ALPHA_8) depth = 1;
        else if (config == Bitmap.Config.ARGB_8888) depth = 4;
        else throw new InvalidTypeException(config + " not supported");
        /* Get pixels from bitmap */
        byte[] pixels = new byte[width * height * depth];
        ByteBuffer pixelBuffer = ByteBuffer.wrap(pixels);
        mBitmap.copyPixelsToBuffer(pixelBuffer);
        /* Creating the returned array */
        byte[] byteArray = new byte[pixels.length + HEADER_SIZE];
        ByteBuffer b = ByteBuffer.wrap(byteArray).order(StorageHelper.ENDIAN);
        b.put(depth);
        b.putInt(mBitmap.getWidth());
        b.putInt(mBitmap.getHeight());
        b.put(pixels);
        return b.array();
    }

    @Override
    protected void onRecycle() {
        mBitmap.recycle();
        mBitmap = null;
    }

}
