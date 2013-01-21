package com.jlreyes.libraries.android_game_engine.scenes;

import android.content.Context;
import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.io.GameView;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.scenes.Loader.PercentDone;
import com.jlreyes.libraries.android_game_engine.scenes.Scene.SceneState;
import com.jlreyes.libraries.android_game_engine.sprites.Sprite;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.GameCamera;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.IndirectCamera;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TextureLoader;
import com.jlreyes.libraries.android_game_engine.sprites.textures.types.TexType;

import java.util.ArrayList;

/**
 * Layer within a scene.
 *
 * @author jlreyes
 */
public abstract class Layer {
    public static final int MAX_TAP_DURATION = 500; /* MS */
    public static final float MAX_TAP_TRAVEL_DISTANCE = 5.0f; /* UNITS */

    private String mName;
    private Scene mParentScene;
    private CullGrid mCullGrid;
    private Sprite[] mSprites;

    private Sprite mWatchedSprite;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private long mInitialTouchTime;

    private GameCamera mCamera;

    public Layer(String name, Scene scene) {
        this.mName = scene.getName() + "." + name;
        this.mParentScene = scene;
    }

    /**
     * Called on loading.
     */
    public void load(PercentDone percentDone) {
        this.mCullGrid = null;
        this.mWatchedSprite = null;
        this.mCamera = this.createCamera();
        this.mSprites = new Sprite[1];
        this.mSprites[0] = this.mCamera.getInstance();
    }

    /**
     * Called during load. Clients must return a camera to use for this layer.
     */
    public abstract GameCamera createCamera();

    /**
     * Called when the application is about to be paused.
     * We lose our context, so no need to unregister textures.
     */
    public void onPause() {}

    /**
     * Called when the application is about to be resumed. We reload every
     * sprite's textures.
     */
    public void onResume() {
        Context context = this.getParentScene().getScheduler().getContext();
        GameView gameView = this.getParentScene().getScheduler().getGameView();
        for (Sprite sprite : mSprites) {
            if (sprite == null) continue;
            Texture texture = sprite.getTexture();
            if (texture == null) continue;
            TexController.TexInfo texInfo = texture.getTexInfo();
            TextureLoader.TexInfo texData;
            try {
                texData = TextureLoader.TexFromStorage(texInfo, context);
            } catch (Exception e) {
                throw new RuntimeException("Something happened to your texture" +
                                           "files while the game was paused!" +
                                           " Restart the game to have them" +
                                           " recreated.");
            }
            TexType[] rgbTexes = texData.getRGBTexs();
            TexType[] aTexes = texData.getATexs();
            texture.registerWithOpenGL(gameView, rgbTexes, aTexes);
            int length = rgbTexes.length;
            for (int i = 0; i < length; i++) {
                rgbTexes[i].recycle();
                aTexes[i].recycle();
                rgbTexes[i] = null;
                aTexes[i] = null;
            }
        }
    }

    public void onUpdateStart() {
        for (Sprite sprite : mSprites)
            /* Make sure we only handle sprites owned by this layer */
            if (sprite.getLayer() == this) sprite.onStartLogicStep();
    }

    /**
     * Updates the layer depending on the state and then calls the update method
     * for each sprite in this layer. Updates this layer's camera last.
     */
    public void update(long deltaTime, SceneState state) {        /* Update the layer */
        updateOnState(deltaTime, state);
		/* Update the camera reference, if we need to */
        if (this.mCamera instanceof IndirectCamera)
            mSprites[0] = this.mCamera.getInstance();
        for (Sprite sprite : mSprites) {
			/* Skip the camera for now */
            if (sprite != this.mCamera) sprite.update(deltaTime);
        }
		/* If this layer does not control a camera, we let whichever layer
		 * DOES control camera update the camera */
        if (this.mCamera instanceof LayerCamera) this.mCamera.update(deltaTime);
    }

    /**
     * Called on at the end of an update.
     *
     * @param renderables list where we should add the renderables from this
     *                    layer
     * @param thread      The thread we will pass control to.
     */
    public void onUpdateFinish(ArrayList<Renderable> renderables, Thread thread) {
        for (Sprite sprite : mSprites) {
		    /* Grab the sprites active renderable */
            Renderable renderable = null;
		    /* End the sprite if it is owned by this layer */
            if (sprite.getLayer() == this)
                renderable = sprite.onEndLogicStep(thread);
            else renderable = sprite.getActiveRenderable();
            /* If this sprite is visible, add it to the given list and pass it
             * to the given thread. */
            if (renderable.isVisible(getCamera()) == true)
                renderables.add(renderable);
            else sprite.release(thread);
        }
    }

    /**
     * Updates this layer differently depending on the given scene state.
     *
     * @param deltaTime The time passed since the last game step
     */
    public abstract void updateOnState(long deltaTime, SceneState state);

    /**
     * Processes the given input event. We only attempt to check if a sprite
     * was tapped.
     *
     * @return null if no sprite handled the event, the sprite that handled
     *         the event otherwise
     */
    public Sprite processInput(MotionEvent inputEvent) {
        int action = inputEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            /* If we are not watching any sprite, there is nothing to do */
            case MotionEvent.ACTION_MOVE: {
                if (this.mWatchedSprite == null) return null;
                break;
            }
            /* When we release a pointer, notify the watched sprite */
            case MotionEvent.ACTION_UP: {
                if (this.mWatchedSprite == null) return null;
                boolean handled = this.mWatchedSprite.onTouchUp(inputEvent);
                Sprite watchedSprite = this.mWatchedSprite;
                this.mWatchedSprite = null;
                return handled ? watchedSprite : null;
            }
            /* We stop all watching when a second pointer comes down */
            case MotionEvent.ACTION_POINTER_DOWN: {
                this.mWatchedSprite.onTouchCancel(inputEvent);
                this.mWatchedSprite = null;
                return null;
            }
            default:
                return null;
        }

        /* See what is being touched */
        LayerCamera layerCamera = this.mCamera.getInstance();
        float x = layerCamera.screenPixelToGameCoordX(inputEvent.getX());
        float y = layerCamera.screenPixelToGameCoordY(inputEvent.getY());
        Sprite sprite = spriteAt(x, y);

        if (action == MotionEvent.ACTION_DOWN) {
            /* If we are note touching anything, do nothing */
            if (sprite == null) return null;
            /* If the sprite does not handle the touch, do nothing */
            boolean handled = sprite.onTouchDown(inputEvent);
            if (handled == false) return null;

            /* If we get here, we need to watch this sprite */
            this.mWatchedSprite = sprite;
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.mInitialTouchTime = System.currentTimeMillis();
            return sprite;
        } else {
            /* We must have moved the pointer */
            boolean stopWatching = false;
            /* If we have touched for too long, we stop watching */
            long deltaTime = System.currentTimeMillis() - this.mInitialTouchTime;
            if (deltaTime >= Layer.MAX_TAP_DURATION) stopWatching = true;
            /* If we have moved too far, we stop watching */
            float deltaX = Math.abs(this.mInitialTouchX - x);
            float deltaY = Math.abs(this.mInitialTouchY - y);
            if (deltaX >= Layer.MAX_TAP_TRAVEL_DISTANCE ||
                deltaY >= Layer.MAX_TAP_TRAVEL_DISTANCE) stopWatching = true;
            /* If we need to stop watching, do so */
            if (stopWatching == true) {
                this.mWatchedSprite.onTouchCancel(inputEvent);
                this.mWatchedSprite = null;
            }
            return null;
        }
    }

    /**
     * Adds the given sprite to this layer's array of sprites. O(n), should
     * not be used dynamically.
     */
    public void addSprite(Sprite sprite) {
        addSprites(new Sprite[]{sprite});
    }

    /**
     * Appends the given array of sprites to this layer's array of sprites.
     * O(n), sprite's should not be added dynamically.
     *
     * @param sprites Array of sprites to add
     */
    public void addSprites(Sprite[] sprites) {
	    /* Create the new array of sprites */
        int newSize = mSprites.length + sprites.length;
        Sprite[] newSprites = new Sprite[newSize];
        System.arraycopy(mSprites, 0, newSprites, 0, mSprites.length);
        System.arraycopy(sprites, 0, newSprites, mSprites.length, sprites.length);
        mSprites = newSprites;
    }

    public void addSprites(ArrayList<Sprite> sprites) {
        if (sprites.size() > 0) addSprites(sprites.toArray(new Sprite[0]));
    }

    /**
     * Returns a sprite iff there is a sprite in this layer at (x, y). Returns
     * null otherwise.
     */
    public Sprite spriteAt(float x, float y) {
        for (Sprite sprite : mSprites)
            if (sprite.isTouching(x, y) == true) return sprite;
        return null;
    }

    /*
     * Camera Methods
     */
    public LayerCamera getCamera() {
        return this.mCamera.getInstance();
    }

    public void setCamera(GameCamera gameCamera) {
        this.mCamera = gameCamera;
        mSprites[0] = gameCamera;
    }

    /*
     * CullGrid methods
     */
    public void setCullGrid(CullGrid cullGrid) {
        this.mCullGrid = cullGrid;
    }

    public CullGrid getCullGrid() {
        return mCullGrid;
    }

    public boolean hasCullGrid() {
        return mCullGrid != null;
    }

    /*
     * Getters and Setters
     */

    public Sprite[] getSprites() {
        return mSprites;
    }

    public String getName() {
        return mName;
    }

    public Scene getParentScene() {
        return mParentScene;
    }

}
