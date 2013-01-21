/**
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit 
 * http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */
package com.jlreyes.libraries.android_game_engine.sprites.textures;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;
import com.jlreyes.libraries.android_game_engine.io.GameView;
import com.jlreyes.libraries.android_game_engine.io.storage.external.ExternalStorageHelper;
import com.jlreyes.libraries.android_game_engine.io.storage.filetypes.TextureImageFile;
import com.jlreyes.libraries.android_game_engine.io.storage.filetypes.TextureInfoFile;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture.TexturePart;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.BitmapTexType;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.ETC1TexType;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.TexType;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.utils.Utils.Direction;
import com.jlreyes.libraries.android_game_engine.utils.exceptions.*;
import com.jlreyes.libraries.android_game_engine.utils.math.MathMatrixConstructor;
import com.jlreyes.libraries.android_game_engine.utils.math.Tuple;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;
import com.jlreyes.libraries.android_game_engine.utils.typewrappers.IntWrapper;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Class holding texture loading methods.
 *
 * @author jlreyes
 */
public class TextureLoader {
    public static final String TAG = "Texture Loader";
    public static long MEMORY_USED = 0l;

    /**
     * TexInfo is a public, purely-organizational, class used by the Texture
     * Loader to transfer texture info around methods. It holds a given
     * texture's Texture Parts, RGB and Alpha TexTypes, its default TextureState,
     * an array of its TextureStates, its width and height, and a boolean
     * indicating if the texture uses its alpha channel.
     *
     * @author jlreyes
     */
    public static class TexInfo extends Tuple {
        protected TexInfo(Texture.TexturePart[] texParts,
                          TexType[] rgbTexs,
                          TexType[] aTexs,
                          TextureState defaultState,
                          TextureState[] states,
                          int frameWidth,
                          int frameHeight) {
            super(texParts,
                  rgbTexs,
                  aTexs,
                  defaultState,
                  states,
                  frameWidth,
                  frameHeight);
        }

        public Texture.TexturePart[] getTexParts() {
            return (TexturePart[]) super.get(0);
        }

        public TexType[] getRGBTexs() {
            return (TexType[]) super.get(1);
        }

        public TexType[] getATexs() {
            return (TexType[]) super.get(2);
        }

        public TextureState getDefaultState() {
            return (TextureState) super.get(3);
        }

        public TextureState[] getStates() {
            return (TextureState[]) super.get(4);
        }

        public int getFrameWidth() {
            return (Integer) super.get(5);
        }

        public int getFrameHeight() {
            return (Integer) super.get(6);
        }
    }

    /**
     * Load a texture without a scheduler. That is, load a texture
     * outside of a game context. The only purpose this serves is to verify
     * that a valid texture file exists on the device. If one does not,
     * calling this method will have the side affect of creating it.
     *
     * @param texControllerInfo The texture to load.
     * @param context           An application context to load resources from.
     * @return The loaded texture.
     */
    public static Texture LoadTextureForVerification(TexController.TexInfo texControllerInfo,
                                                     Context context) {
        return LoadTexture(texControllerInfo, context, null, null);
    }

    /**
     * Load a texture using the scheduler's context, gameview, and animator.
     * Calling this method will have the side effect of creating a texture
     * file on the device if one does not exist.
     *
     * @param texControllerInfo The texture to load.
     * @param scheduler         The scheduler for the game.
     * @return The loaded texture.
     */
    public static Texture LoadTexture(TexController.TexInfo texControllerInfo,
                                      Scheduler scheduler) {
        return LoadTexture(texControllerInfo,
                           scheduler.getContext(),
                           scheduler.getGameView(),
                           scheduler.getAnimator());
    }

    /**
     * Loads the texture with the given textureId. Has various effects depending
     * on the arguments supplied. Thus, this method is private.
     *
     * @param texControllerInfo Required. The texture to load.
     * @param context           The Android context to grab resources from.
     * @param gameView          Optional. If supplied, the texture will be registered with
     *                          openGL ES.
     * @param animator          Optional. If supplied the texture will be registered with
     *                          the given animator. That is, the state of the texture will be updated
     *                          according the its animation properties.
     * @return A fully loaded texture.
     */
    private static Texture LoadTexture(TexController.TexInfo texControllerInfo,
                                       Context context,
                                       GameView gameView,
                                       Animator animator) {
        if (texControllerInfo == TexController.NO_TEX) return null;

        /** Tuple containing all important information about the texture */
        TexInfo texInfo = GetTexInfo(texControllerInfo, context);
        Texture.TexturePart[] textureParts = texInfo.getTexParts();
        TexType[] rgbTexs = texInfo.getRGBTexs();
        TexType[] aTexs = texInfo.getATexs();
        TextureState[] states = texInfo.getStates();
        TextureState defaultState = texInfo.getDefaultState();
        int frameWidth = texInfo.getFrameWidth();
        int frameHeight = texInfo.getFrameHeight();
    	/* Verifying */
        if ((textureParts.length == rgbTexs.length &&
             textureParts.length == aTexs.length) == false)
            throw new RuntimeException("length of textureParts, rgbTexs, and" +
                                       "aTexs must the same.");
    	/* Loading the texture */
        Texture texture = new Texture(texControllerInfo,
                                      textureParts,
                                      states,
                                      defaultState,
                                      frameWidth,
                                      frameHeight);
    	/* Registering with opengl if we need to */
        if (gameView != null) texture.registerWithOpenGL(gameView,
                                                         rgbTexs, aTexs);
		/* Register with animator if we need */
        if (animator != null) {
		    /* If each state only has one frame, there is no need to animate. */
            int numFrames = texControllerInfo.NumFrames;
            if (states.length != numFrames) animator.addTexture(texture);
        }
		/* Recycling the textures */
        int numTexs = textureParts.length;
        for (int i = 0; i < numTexs; i++) {
            rgbTexs[i].recycle();
            rgbTexs[i] = null;
            aTexs[i].recycle();
            aTexs[i] = null;
        }
        rgbTexs = null;
        aTexs = null;
        System.gc();
        return texture;
    }

    /**
     * Given a textureId, generates and returns its tex info.
     *
     * @return An instance of {@link com.jlreyes.libraries.android_game_engine.sprites.textures.TextureLoader.TexInfo}
     */
    private static TexInfo GetTexInfo(TexController.TexInfo texControllerInfo,
                                      Context context) {
        TexInfo texInfo = null;
        try {
            texInfo = TexFromStorage(texControllerInfo, context);
        } catch (MissingFileException e) {
            /* There was a file missing in this texture, recreate */
            texInfo = CreateTex(texControllerInfo, context);
            SaveTexInStorage(texControllerInfo, texInfo, context);
        } catch (DataStreamEndedEarlyException e) {
            /* For some reason, the file was too small */
            texInfo = CreateTex(texControllerInfo, context);
            SaveTexInStorage(texControllerInfo, texInfo, context);
        } catch (FileCorruptedException e) {
            /* Something misc was wrong with the file */
            texInfo = CreateTex(texControllerInfo, context);
            SaveTexInStorage(texControllerInfo, texInfo, context);
        } catch (InvalidFileSizeException e) {
            /* The stored filesize didnt match the actual filesize */
            texInfo = CreateTex(texControllerInfo, context);
            SaveTexInStorage(texControllerInfo, texInfo, context);
        } catch (DataExpiredException e) {
            /* The data was old.  */
            texInfo = CreateTex(texControllerInfo, context);
            SaveTexInStorage(texControllerInfo, texInfo, context);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return texInfo;
    }
	
	/*
	 * SAVING A TEXTURE
	 */

    private static void SaveTexInStorage(TexController.TexInfo texControllerInfo,
                                         TexInfo texInfo,
                                         Context context) {
        String fileName = texControllerInfo.Name + "_" + TexController.RESOLUTION;
        long lastModified = texControllerInfo.Version;
        try {
	        /* Create texture info file */
            TextureInfoFile texInfoFile =
                    new TextureInfoFile(fileName,
                                        lastModified,
                                        texInfo.getStates(),
                                        texInfo.getFrameWidth(),
                                        texInfo.getFrameHeight());
            texInfoFile.create(context,
                               lastModified,
                               texInfo.getTexParts());
	        /* Creating texture image files */
            TexType[][] texImages = new TexType[][]{
                    texInfo.getRGBTexs(), texInfo.getATexs()
            };
            int numTexParts = texInfo.getTexParts().length;
            for (int i = 0; i < numTexParts; i++) {
                TextureImageFile rgbFile = new TextureImageFile(fileName + "_rgb_" + i);
                rgbFile.create(context, texImages[0][i]);
                TextureImageFile aFile = new TextureImageFile(fileName + "_a_" + i);
                aFile.create(context, texImages[1][i]);
            }
        } catch (IOException e) {
	        /* This is serious, force close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        } catch (MissingFileException e) {
            /* A file wasn't actually created for some reason. Force Close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        } catch (DataStreamEndedEarlyException e) {
            /* This shouldn't happen on correct creation. Force close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        } catch (FileCorruptedException e) {
            /* This shouldn't happen on correct creation. Force close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        } catch (InvalidFileSizeException e) {
            /* This shouldn't happen on correct creation. Force close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        } catch (DataExpiredException e) {
            /* This shouldn't happen on correct creation. Force close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        } catch (InvalidTypeException e) {
            /* This shouldn't happen on correct creation. Force close. */
            e.printStackTrace();
            throw new RuntimeException("Fatal error.");
        }
	    /* TODO: Cleanup of created files on force close */
    }
	
	/*
	 * LOADING A TEXTURE
	 */

    /**
     * Loads the information from storage needed to create a texture.
     * @throws IOException
     * @throws MissingFileException
     * @throws DataExpiredException
     * @throws InvalidFileSizeException
     * @throws FileCorruptedException
     * @throws DataStreamEndedEarlyException
     */
    public static TexInfo TexFromStorage(TexController.TexInfo texControllerInfo,
                                         Context context)
            throws IOException,
                   MissingFileException,
                   DataStreamEndedEarlyException,
                   FileCorruptedException,
                   InvalidFileSizeException,
                   DataExpiredException {
		/* Getting attributes */
        final String fileName = texControllerInfo.Name + "_" + TexController.RESOLUTION;
		/* Test if the texInfo file is there. Prevents doing unnecessary calculation 
		 * if its not */
        boolean fileExists =
                ExternalStorageHelper.FileExists(context, fileName + ".texInfo");
        if (fileExists == false) throw new MissingFileException();
		/* Continue to get attributes */
        long lastModified = texControllerInfo.Version;
        int frameWidth = texControllerInfo.FrameWidth();
        int frameHeight = texControllerInfo.FrameHeight();
        TextureState[] states = CreateStates(texControllerInfo,
                                             frameWidth,
                                             frameHeight);
        TextureState defaultState = GetDefaultState(texControllerInfo, states);
        /* Attempt to open texture info file */
        TextureInfoFile texInfo = new TextureInfoFile(fileName,
                                                      lastModified,
                                                      states,
                                                      frameWidth,
                                                      frameHeight);
        texInfo.load(context);
        TexturePart[] texParts = texInfo.getTexParts();
        /* Attempt to open our texture image files */
        int numTexParts = texParts.length;
        TexType[] rgbTexs = new TexType[numTexParts];
        TexType[] aTexs = new TexType[numTexParts];
        for (int i = 0; i < numTexParts; i++) {
            /* RGB Image */
            String rgbFileName = fileName + "_rgb_" + i;
            TextureImageFile rgbFile = new TextureImageFile(rgbFileName);
            rgbFile.load(context);
            rgbTexs[i] = rgbFile.getTexType();
            /* Alpha Image */
            String aFileName = fileName + "_a_" + i;
            TextureImageFile aFile = new TextureImageFile(aFileName);
            aFile.load(context);
            aTexs[i] = aFile.getTexType();
        }
        return new TexInfo(texParts,
                           rgbTexs,
                           aTexs,
                           defaultState,
                           states,
                           frameWidth,
                           frameHeight);
    }
	
	/*
	 * CREATING A TEXTURE 
	 */

    /**
     * From scratch, loads and creates all the information needed for a texture.
     */
    private static TexInfo CreateTex(TexController.TexInfo texControllerInfo, Context context) {
        int numFrames = texControllerInfo.NumFrames;
        int frameWidth = texControllerInfo.FrameWidth();
        int frameHeight = texControllerInfo.FrameHeight();
        Log.i(TAG, "Creating states");
        TextureState[] states = CreateStates(texControllerInfo, frameWidth, frameHeight);
        Log.i(TAG, "GETTING DEFAULT STATE");
        TextureState defaultState = GetDefaultState(texControllerInfo, states);
        Log.i(TAG, "CREATING TEXTURE PARTS");
        TexturePart[] textureParts = CreateTextureParts(texControllerInfo,
                                                        states,
                                                        numFrames,
                                                        frameWidth,
                                                        frameHeight);
		/* Creating bitmaps */
        Log.i(TAG, "CREATING BITMAPS");
        TexType[][] texs = GetTexTypes(textureParts, context);
		/* Return info */
        Log.i(TAG, "DONE");
        return new TexInfo(textureParts,
                           texs[0],
                           texs[1],
                           defaultState,
                           states,
                           frameWidth,
                           frameHeight);
    }

    /**
     * Returns an array of TexTypes where the first index represents the
     * rgb textype and the second index represents the alpha textype.
     */
    private static TexType[][] GetTexTypes(TexturePart[] textureParts,
                                           Context context) {
        int numParts = textureParts.length;
        TexType[] rgbTexs = new TexType[numParts];
        TexType[] aTexs = new TexType[numParts];
        for (int i = 0; i < numParts; i++) {
            Bitmap[] bitmaps = textureParts[i].generateBitmaps(context);
            rgbTexs[i] = new ETC1TexType(bitmaps[0]);
            aTexs[i] = new BitmapTexType(bitmaps[1]);
        }
        return new TexType[][]{rgbTexs, aTexs};
    }

    /**
     * Given a texture id, returns the states associated with it.
     */
    private static TextureState[] CreateStates(TexController.TexInfo texControllerInfo,
                                               int frameWidth,
                                               int frameHeight) {
        TexController.TexStateInfo[] stateInfo = texControllerInfo.States;
        int numStates = stateInfo.length;
        TextureState[] states = new TextureState[numStates];
        for (int i = 0; i < numStates; i++)
            states[i] = new TextureState(stateInfo[i], frameWidth, frameHeight);
        return states;
    }

    private static TextureState GetDefaultState(TexController.TexInfo texInfo,
                                                TextureState[] states) {
	    /* Get default state name */
        TexController.TexStateInfo defaultStateInfo = texInfo.DefaultState;
        String defaultStateName = defaultStateInfo.Name;
        for (TextureState state : states)
            if (state.getName().equals(defaultStateName)) return state;
        throw new RuntimeException("No default state for texture" + texInfo);
    }

    /**
     * Creates texture parts from scratch with the given information.
     */
    private static Texture.TexturePart[] CreateTextureParts(TexController.TexInfo texControllerInfos,
                                                            TextureState[] states,
                                                            int numFrames,
                                                            int frameWidth,
                                                            int frameHeight) {
		/* Getting the maximum texture size */
        int maxTexSize = GLES20.GL_MAX_TEXTURE_SIZE;
        if (maxTexSize == 0)
            throw new RuntimeException("Attempting to create texture parts " +
                                       " without a maximum texture size.");
        IntWrapper numFramesLeft = new IntWrapper(numFrames);
        IntWrapper currentState = new IntWrapper(0);
        IntWrapper stateFrame = new IntWrapper(0);
        ArrayList<Texture.TexturePart> textureParts =
                new ArrayList<Texture.TexturePart>();
        while (numFramesLeft.getInt() > 0) {
			/* Construction of the texturepart matrix */
            MathMatrixConstructor<TextureState.Frame> constructor =
                    new MathMatrixConstructor<TextureState.Frame>();
            constructor.addRow();
            constructor.addColumn();
			/* The width and height of the texture part */
            int partWidth = frameWidth;
            int partHeight = frameHeight;
			/* Manually fill the first index */
            FillTexturePartMatrix(constructor,
                                  0, 0,
                                  Direction.NONE,
                                  states,
                                  numFramesLeft,
                                  currentState,
                                  stateFrame);
			/* Automatically fill the rest */
            while (true) {
                if (numFramesLeft.isZero() == true) break;
				/* Adding a column if possible */
                if (partWidth + frameWidth <= maxTexSize) {
                    constructor.addColumn();
                    partWidth += frameWidth;
                } else break;
				/* Filling down */
                FillTexturePartMatrix(constructor,
                                      0, constructor.getCols() - 1,
                                      Direction.DOWN,
                                      states,
                                      numFramesLeft,
                                      currentState,
                                      stateFrame);
                if (numFramesLeft.isZero() == true) break;
				/* Adding a row if possible */
                if (partHeight + frameHeight <= maxTexSize) {
                    constructor.addRow();
                    partHeight += frameHeight;
                } else break;
				/* Filling left */
                FillTexturePartMatrix(constructor,
                                      constructor.getRows() - 1,
                                      constructor.getCols() - 1,
                                      Direction.LEFT,
                                      states,
                                      numFramesLeft,
                                      currentState,
                                      stateFrame);
            }
            TexturePart texturePart = new TexturePart(constructor.finish(),
                                                      frameWidth, frameHeight);
            textureParts.add(texturePart);
        }
        TexturePart[] texturePartArray = textureParts.toArray(new TexturePart[0]);
        return texturePartArray;
    }

    /**
     * Given a direction, returns a function to applied every iteration in
     * TextureLoader.FillTexturePartMatrix
     */
    private static Function0<Void> GetFillDirFunction(Direction dir,
                                                      final IntWrapper row,
                                                      final IntWrapper col) {
        Function0<Void> dirFunction = null;
        switch (dir) {
            case DOWN: {
                dirFunction = new Function0<Void>() {
                    public Void run() {
                        row.setInt(row.getInt() + 1);
                        return null;
                    }
                };
                break;
            }
            case UP: {
                dirFunction = new Function0<Void>() {
                    public Void run() {
                        row.setInt(row.getInt() - 1);
                        return null;
                    }
                };
                break;
            }
            case LEFT: {
                dirFunction = new Function0<Void>() {
                    public Void run() {
                        col.setInt(col.getInt() - 1);
                        return null;
                    }
                };
                break;
            }
            case RIGHT: {
                dirFunction = new Function0<Void>() {
                    public Void run() {
                        col.setInt(col.getInt() + 1);
                        return null;
                    }
                };
                break;
            }
            case NONE: {
                dirFunction = new Function0<Void>() {
                    public Void run() {
                        return null;
                    }
                };
                break;
            }
        }
        return dirFunction;
    }

    /**
     * Fills the given texture part in the direction given with the frames
     * dictated by the given arguments.
     */
    private static void FillTexturePartMatrix
    (MathMatrixConstructor<TextureState.Frame> mConstructor,
     int startRow,
     int startCol,
     Direction dir,
     TextureState[] states,
     IntWrapper numFramesLeft,
     IntWrapper currentState,
     IntWrapper stateFrame) {
        IntWrapper row = new IntWrapper(startRow);
        IntWrapper col = new IntWrapper(startCol);
		/* Direction Function */
        Function0<Void> dirFunction = GetFillDirFunction(dir, row, col);
        while (mConstructor.isIn(row.getInt(), col.getInt())) {
			/*
			 * Filling the frame
			 */
            TextureState state = states[currentState.getInt()];
            TextureState.Frame frame = state.getFrame(stateFrame.getInt());
            mConstructor.set(row.getInt(), col.getInt(), frame);
			/*
			 * Future Calculations
			 */
            numFramesLeft.decrement();
            if (numFramesLeft.isZero() == true) return;
			/* Calculate the state of the next frame */
            stateFrame.increment();
            if (stateFrame.getInt() >= state.getNumFrames()) {
                currentState.increment();
                if (currentState.getInt() >= states.length)
                    throw new RuntimeException("For some reason numFramesLeft " +
                                               "was equal to " + numFramesLeft.getInt() +
                                               " instead of 0. This would have caused" +
                                               "an index out of bounds error next time" +
                                               "around since the there are no states" +
                                               "left to work with.");
                stateFrame.setInt(0);
            }
            if (dir == Direction.NONE) return;
            dirFunction.run();
        }
    }


}
