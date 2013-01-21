package com.jlreyes.libraries.android_game_engine.rendering;

import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;

import java.util.ArrayList;

/**
 * Stores an arrayList of Renderables.
 *
 * @author jlreyes
 */
public class RenderInfo {
    private ArrayList<Renderable> mRenderables;

    public RenderInfo() {
        this.mRenderables = new ArrayList<Renderable>();
    }

    public ArrayList<Renderable> getRenderables() {
        return mRenderables;
    }

    public void setRenderables(ArrayList<Renderable> mRenderables) {
        this.mRenderables = mRenderables;
    }

    public void append(RenderInfo renderInfo) {
        ArrayList<Renderable> renderables = renderInfo.getRenderables();        /* Prevent toArray allocation */
        int length = renderables.size();
        for (int i = 0; i < length; i++) mRenderables.add(renderables.get(i));
    }

    public void clear() {
        mRenderables.clear();
    }
}
