package com.jlreyes.libraries.android_game_engine.io.storage.filetypes;

import android.content.Context;
import com.jlreyes.libraries.android_game_engine.io.storage.GameFile;
import com.jlreyes.libraries.android_game_engine.io.storage.external.ExternalStorageHelper;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TextureState;
import com.jlreyes.libraries.android_game_engine.utils.Utils;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.*;
import com.jlreyes.libraries.android_game_engine.utils.math.MathMatrix;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * File with the following structure:
 * 0 (long) : Date I last modified the texture (milliseconds since epoch)
 * 8 (int) : Number of texture parts in this texture.
 * 12 (?) : Texture Parts
 * <p/>
 * Texture Parts have the following structure.
 * 0 (int) : number of rows in this texture part
 * 4 (int) : number of cols in this texture part
 * We now list each rows * cols frame.
 * Frames are stored as followed:
 * (Row 0, Col 0), (Row 0, Col 1), ... (Row 0, Col n),
 * (Row 1, Col 0), ..., (Row n, Col m)
 * Each frame has the following structure.
 * 0 (int) : Frame in the state. -1 if this frame is empty.
 * 4 (String) : Null terminated character array naming the state this points to
 * first char is '\u0000' if this frame is empty.
 *
 * @author jlreyes
 */
public class TextureInfoFile extends GameFile {
    public static final String EXTENSION = "texInfo";
    public static final int TEXINFO_HEADER_SIZE = 12;
    public static final int TEXPART_HEADER_SIZE = 8;
    public static final int TEXFRAME_HEADER_SIZE = 4;

    private Texture.TexturePart[] mTexParts;
    private long mLastModified;
    private TextureState[] mStates;
    private int mFrameWidth;
    private int mFrameHeight;

    /*
     * Creation
     */

    /**
     * Creates a new Texture Info file with the given information.
     */
    public TextureInfoFile(String fileName,
                           long lastModified,
                           TextureState[] states,
                           int frameWidth,
                           int frameHeight) {
        super(fileName);
        this.mLastModified = lastModified;
        this.mStates = states;
        this.mFrameWidth = frameWidth;
        this.mFrameHeight = frameHeight;
    }

    @Override
    protected void loadFileInfo(BufferedInputStream data)
            throws IOException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   DataExpiredException {
        /* Get the data this texture was last modified */
        long fileLastModified = ExternalStorageHelper.ReadLongFromInputStream(data);
        if (mLastModified > fileLastModified) throw new DataExpiredException();
        /* Create texture parts */
        int numTexParts = ExternalStorageHelper.ReadIntFromInputStream(data);
        Texture.TexturePart[] texParts = new Texture.TexturePart[numTexParts];
        for (int i = 0; i < numTexParts; i++) {
            int rows = ExternalStorageHelper.ReadIntFromInputStream(data);
            int cols = ExternalStorageHelper.ReadIntFromInputStream(data);
            /* Create and fill the frame matrix */
            MathMatrix<TextureState.Frame> matrix =
                    new MathMatrix<TextureState.Frame>(rows, cols);
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int frameNum =
                            ExternalStorageHelper.ReadIntFromInputStream(data);
                    String stateName =
                            ExternalStorageHelper.ReadStringFromInputStream(data);
                    if (frameNum != -1) {
                        /* Search our states for this stateName */
                        TextureState result = (TextureState)
                                Utils.LinearSearch(getStateEqualityFunc(stateName),
                                                   mStates);
                        if (result == null) throw new FileCorruptedException();
                        /* Get the frame, if it exists */
                        TextureState.Frame frame = result.getFrame(frameNum);
                        matrix.set(row, col, frame);
                    }
                }
            }
            texParts[i] = new Texture.TexturePart(matrix,
                                                  mFrameWidth,
                                                  mFrameHeight);
        }
        this.mTexParts = texParts;
    }

    /**
     * Returns a function that only returns true if the given state
     * has a name equal to the given stateName
     */
    private Function1<Object, Boolean> getStateEqualityFunc(final String stateName) {
        return new Function1<Object, Boolean>() {
            public Boolean run(Object o) {
                TextureState state = (TextureState) o;
                if (state.getName().equals(stateName)) return true;
                else return false;
            }
        };
    }

    public void create(Context context,
                       long lastModified,
                       Texture.TexturePart[] texParts)
            throws IOException,
                   MissingFileException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   InvalidFileSizeException,
                   DataExpiredException {
        /* Calculating the size of the byte buffer */
        int dataSize = CalculateDataSize(texParts);
        /* Creating the byte buffer we will store */
        ByteBuffer data = ByteBuffer.allocateDirect(dataSize);
        /* Storing data in the byte array */
        data.putLong(lastModified);
        data.putInt(texParts.length);
        for (Texture.TexturePart texPart : texParts) {
            ;
            /* Storing the rows and cols of a texpart */
            int rows = texPart.getNumRows();
            int cols = texPart.getNumCols();
            data.putInt(rows);
            data.putInt(cols);
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    /* Storing the frame index */
                    TextureState.Frame frame = texPart.getFrame(row, col);
                    if (frame == null) data.putInt(-1);
                    else {
                        data.putInt(frame.getStateFrame());
                        /* Calculating and storing the frame state string name */
                        String frameName = frame.getState().getName();
                        int length = frameName.length();
                        for (int i = 0; i < length; i++) {
                            char c = frameName.charAt(i);
                            data.putChar(c);
                        }
                    }
                    data.putChar('\u0000');
                }
            }

        }
        super.create(context, data);
    }

    /**
     * Given an array of textureparts, calculates the size of the data that
     * would be stored (mod the file header).
     */
    public static int CalculateDataSize(Texture.TexturePart[] texParts) {
        int dataSize = TEXINFO_HEADER_SIZE;
        for (Texture.TexturePart texPart : texParts) {
            dataSize += TEXPART_HEADER_SIZE;
            int rows = texPart.getNumRows();
            int cols = texPart.getNumCols();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    dataSize += TEXFRAME_HEADER_SIZE;
                    TextureState.Frame frame = texPart.getFrame(row, col);
                    if (frame != null) {
                        String frameName = frame.getState().getName();
                        int stringSize = 2 * (frameName.length() + 1);
                        dataSize += stringSize;
                    } else {
                        dataSize += 2;
                    }

                }
            }
        }
        return dataSize;
    }

    /*
     * Getters and Setters
     */
    public Texture.TexturePart[] getTexParts() {
        return mTexParts;
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }
}
