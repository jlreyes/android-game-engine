package com.jlreyes.libraries.android_game_engine.io.storage;

import android.content.Context;
import com.jlreyes.libraries.android_game_engine.io.storage.external.ExternalStorageHelper;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A file that we store somewhere on the device. All game files have the
 * following structure:
 * ByteLoc : Description
 * 0 (int) : Magic Number (69)
 * 4 (int) : The size this file should be.
 * 8 (long)   : Padding
 *
 * @author jlreyes
 */
public abstract class GameFile {
    private boolean mLoaded;
    private String mFileName;

    public static final String TAG = "GameFile";
    public static final int MAGIC_NUMBER = 69;
    public static final int HEADER_SIZE = 16;
    public static final long PADDING = 0l;

    /*
     * Creation
     */
    protected GameFile(String fileName) {
        this.mFileName = fileName;
        this.mLoaded = false;
    }

    /**
     * Loads a the GameFile from external storage.
     * @throws IOException
     * @throws MissingFileException
     * @throws DataStreamEndedEarlyException
     * @throws FileCorruptedException
     * @throws InvalidFileSizeException
     * @throws DataExpiredException
     */
    public void load(Context context)
            throws IOException,
                   MissingFileException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   InvalidFileSizeException,
                   DataExpiredException {

        BufferedInputStream data =
                ExternalStorageHelper.OpenFile(context, getAbsoluteFileName());
        loadHeader(data);
        loadFileInfo(data);
        /* Verify that there is no data left in the stream */
        if (data.read() != -1) {
            int numBytesLeft =
                    1 + ExternalStorageHelper.GetNumberOfBytesLeft(data);
            throw new InvalidFileSizeException("Filesize is " + numBytesLeft +
                                               "bytes too large");
        }
        data.close();
        mLoaded = true;
    }

    /**
     * Loads the header information from the given input stream.
     * @throws DataStreamEndedEarlyException
     * @throws IOException
     * @throws FileCorruptedException
     * @throws InvalidFileSizeException
     * @throws DataExpiredException
     */
    private void loadHeader(BufferedInputStream data)
            throws IOException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   InvalidFileSizeException,
                   DataExpiredException {
        /* Check the magic number */
        int fileMagicNumber = ExternalStorageHelper.ReadIntFromInputStream(data);
        if (fileMagicNumber != MAGIC_NUMBER)
            throw new FileCorruptedException("Magic number given was " + fileMagicNumber);
        ExternalStorageHelper.ReadIntFromInputStream(data);
        /* Move past the padding */
        data.skip(8l);
    }

    protected abstract void loadFileInfo(BufferedInputStream data)
            throws IOException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   DataExpiredException;

    /**
     * Creates the GameFile as a new file on external storage. Loads the file
     * after creation to verify.
     * @throws IOException
     * @throws DataExpiredException
     * @throws InvalidFileSizeException
     * @throws FileCorruptedException
     * @throws DataStreamEndedEarlyException
     * @throws MissingFileException
     */
    protected void create(Context context, ByteBuffer data)
            throws IOException,
                   MissingFileException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   InvalidFileSizeException,
                   DataExpiredException {
        /* Calculate filesize */
        int fileSize = data.capacity() + HEADER_SIZE;
        /* Create file */
        BufferedOutputStream fileBuffer =
                ExternalStorageHelper.WriteFile(context, getAbsoluteFileName());
        /* Write GameFile header */
        ExternalStorageHelper.WriteIntToOutputStream(fileBuffer, MAGIC_NUMBER);
        ExternalStorageHelper.WriteIntToOutputStream(fileBuffer, fileSize);
        ExternalStorageHelper.WriteLongToOutputStream(fileBuffer, PADDING);
        /* Write data */
        data.position(0);
        ExternalStorageHelper.WriteBuffToOutputStream(fileBuffer, data);
        /* Make sure the data is written */
        fileBuffer.flush();
        fileBuffer.close();
        /* Verify creation */
        load(context);
    }
    
    /*
     * Instance methods
     */

    /*
     * Getters and Setters
     */
    public String getFileName() {
        return mFileName;
    }

    public abstract String getExtension();

    public String getAbsoluteFileName() {
        return mFileName + "." + getExtension();
    }

    public boolean isLoaded() {
        return mLoaded;
    }
}
