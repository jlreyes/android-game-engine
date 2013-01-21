package com.jlreyes.libraries.android_game_engine.scenes.scenes.loadscene;

import com.jlreyes.libraries.android_game_engine.datastructures.DFA;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.scenes.Loader.PercentDone;
import com.jlreyes.libraries.android_game_engine.scenes.Scene;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;

import java.util.HashMap;
import java.util.HashSet;

public abstract class LoadScene extends Scene {
    public static class LoadState extends SceneState {
        private PercentDone mPercentDone;

        public void setPercentDone(PercentDone p) {
            this.mPercentDone = p;
        }

        public int getPercentDone() {
            return this.mPercentDone.getPercentDone();
        }
    }

    public LoadScene(Scheduler scheduler) {
        super(scheduler);
    }

    public void load(PercentDone p) {
        super.load(p);
        ((LoadState) this.getCurrentState()).setPercentDone(p);
    }

    protected Layer[] createLayers() {
        return this.createLoadLayers();
    }

    protected abstract LoadLayer[] createLoadLayers();

    @Override
    protected DFA<SceneState, SceneEvent> loadEventDFA() {
        HashSet<SceneState> states = new HashSet<SceneState>();
        LoadState loadState = new LoadState();
        states.add(loadState);
        return new DFA<SceneState, SceneEvent>(states,
                                               new HashSet<SceneEvent>(),
                                               new HashMap<SceneState, HashMap<SceneEvent, SceneState>>(),
                                               loadState,
                                               new HashSet<SceneState>());
    }

    @Override
    public String getName() {
        return "load";
    }
}
