package com.jlreyes.libraries.android_game_engine.rendering.renderable;

import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;

public class RenderablesWrapper extends Renderable {
    public static final String TAG = "RenderablesWrapper";

    private Renderable[] mRenderables;

    public RenderablesWrapper(int numRenderables) {
        super();
        this.mRenderables = new Renderable[numRenderables];
    }

    @Override
    public boolean isVisible(LayerCamera gameCamera) {        /* If all elements in this renderablewrapper are null, this wrapper
         * is not visible */
        for (Renderable renderable : mRenderables)
            if (renderable != null) return true;
        return false;
    }

    @Override
    public void draw(int positionhandle,
                     int mvpMatrixHandle,
                     int texCoordHandle,
                     int texRGBHandle,
                     int texAHandle,
                     float ratio,
                     float[] mvpMatrix,
                     float[] viewMatrix,
                     float[] modelMatrix,
                     float[] projectionMatrix) {
        for (Renderable renderable : mRenderables) {
            if (renderable != null)
                renderable.draw(positionhandle,
                                mvpMatrixHandle,
                                texCoordHandle,
                                texRGBHandle,
                                texAHandle,
                                ratio,
                                mvpMatrix,
                                viewMatrix,
                                modelMatrix,
                                projectionMatrix);
        }

    }

    @Override
    public Renderable copy() {
        return new RenderablesWrapper(mRenderables.length);
    }

    public void set(int i, Renderable renderable) {
        mRenderables[i] = renderable;
    }

    /*
     * Getters and Setters
     */
    public Renderable[] getRenderables() {
        return mRenderables;
    }

    public void setRenderables(Renderable[] renderables) {
        this.mRenderables = renderables;
    }
}
