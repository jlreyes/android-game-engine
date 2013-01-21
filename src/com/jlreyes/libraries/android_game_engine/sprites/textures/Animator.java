package com.jlreyes.libraries.android_game_engine.sprites.textures;

import com.jlreyes.libraries.android_game_engine.threading.GameThread;

import java.util.ArrayList;

/**
 * GameThread handling all animation.
 *
 * @author jlreyes
 */
public class Animator extends GameThread {
    public static final String TAG = "Animator";

    private ArrayList<Texture> mTextures;

    public Animator(String name) {
        super(name);
        this.mTextures = new ArrayList<Texture>();
    }

    /**
     * Called while this state is running
     */
    @Override
    protected void onRunning() {
        synchronized (mTextures) {
            int numTextures = mTextures.size();
            for (int i = 0; i < numTextures; i++) {
                Texture texture = mTextures.get(i);
                synchronized (texture) {
                    TextureState state = texture.getActiveState();
                    TextureState.Frame frame = state.getActiveFrame();
                    long timeSinceStepped =
                            (System.currentTimeMillis() - state.getLastTimeStepped());
                    long frameTimeLength = frame.getTimeLength();
                    if (frameTimeLength <= timeSinceStepped) {
                        state.step();
                    }
                }
            }
        }
    }

    /**
     * Called when the activity is about to be destroyed
     */
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Animates the given texture
     */
    public void addTexture(Texture texture) {
        synchronized (mTextures) {
            mTextures.add(texture);
        }
    }

    public void removeTexture(Texture texture) {
        synchronized (mTextures) {
            mTextures.remove(texture);
        }
    }

	/*
	 * Getters and Setters
	 */

}
