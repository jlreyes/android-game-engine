package com.jlreyes.libraries.android_game_engine.io.storage.filetypes;

import android.content.Context;
import com.jlreyes.libraries.android_game_engine.io.storage.GameFile;
import com.jlreyes.libraries.android_game_engine.io.storage.external.ExternalStorageHelper;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.BitmapTexType;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.ETC1TexType;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.TexType;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * File with the following structure:
 * ByteLoc : Description
 * 0 (byte) : 1 if Bitmap Type, 2 if ETC1 Type
 * 1 (?) : The texture image data. See BitmapTexType and ETC1TexType
 * for information on how the data is stored.
 *
 * @author jlreyes
 */
public class TextureImageFile extends GameFile {
    public static final String EXTENSION = "texImg";

    private TexType mTex;

    /*
     * Creation
     */
    public TextureImageFile(String fileName) {
        super(fileName);
    }

    @Override
    protected void loadFileInfo(BufferedInputStream data)
            throws IOException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException {
        /* Get the type of texture image */
        byte type = ExternalStorageHelper.ReadByteFromInputStream(data);
        if (type == 1) this.mTex = new BitmapTexType(data);
        else if (type == 2) this.mTex = new ETC1TexType(data);
        else throw new FileCorruptedException("TexType value given as " + type);
    }

    /**
     * Creates a new TextureImageFile on external storage and loads its info
     * into this instance.
     * @throws IOException
     * @throws MissingFileException
     * @throws DataStreamEndedEarlyException
     * @throws FileCorruptedException
     * @throws InvalidFileSizeException
     * @throws DataExpiredException
     * @throws InvalidTypeException
     */
    public void create(Context context, TexType tex)
            throws IOException,
                   MissingFileException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   InvalidFileSizeException,
                   DataExpiredException,
                   InvalidTypeException {
        /* Determining the textype to store */
        byte texTypeId = 0;
        if (tex instanceof BitmapTexType) texTypeId = 1;
        else if (tex instanceof ETC1TexType) texTypeId = 2;
        else throw new RuntimeException("Wut");
        /* Creating the bytebuffer we will store */
        byte[] texData = tex.toByteArray();
        ByteBuffer texFileData = ByteBuffer.allocateDirect(1 + texData.length);
        /* Storing the information */
        texFileData.put(texTypeId);
        texFileData.put(texData);
        /* Creating  the file */
        super.create(context, texFileData);
    }

    /*
     * Getters and Setters
     */
    public TexType getTexType() {
        return mTex;
    }

    @Override
    public String getExtension() {
        return EXTENSION;
    }

}
