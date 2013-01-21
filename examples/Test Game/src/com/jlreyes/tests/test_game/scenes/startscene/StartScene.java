package com.jlreyes.tests.test_game.scenes.startscene;

import com.jlreyes.libraries.android_game_engine.datastructures.DFA;
import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.scenes.Scene;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.tests.test_game.R;

import java.util.HashMap;
import java.util.HashSet;

/**
 * User: James
 * Date: 1/14/13
 * Time: 11:25 PM
 */
public class StartScene extends Scene {
    /* Scene States */
    public static final SceneState STATE_BUTTON_OFF = new SceneState();
    public static final SceneState STATE_BUTTON_ON = new SceneState();
    /* Scene Events */
    public static final SceneEvent EVENT_BUTTON_PRESS = new SceneEvent(1);

    public StartScene(Scheduler scheduler) {
        super(scheduler);
    }

    @Override
    protected Layer[] createLayers() {
        return new Layer[] {
                new MainLayer(this)
        };
    }

    @Override
    protected DFA<SceneState, SceneEvent> loadEventDFA() {
        /* Initializing DFA parameters */
        HashSet<SceneState> states = new HashSet<SceneState>();
        HashSet<SceneEvent> alphabet = new HashSet<SceneEvent>();
        HashMap<SceneState, HashMap<SceneEvent, SceneState>> transitions =
                new HashMap<SceneState, HashMap<SceneEvent, SceneState>>();
        SceneState startState = null;
        HashSet<SceneState> acceptStates = new HashSet<SceneState>();
        /* Adding States */
        states.add(STATE_BUTTON_OFF);
        states.add(STATE_BUTTON_ON);
        /* Adding Events */
        alphabet.add(EVENT_BUTTON_PRESS);
        /* Creating Transition Maps */
        HashMap<SceneEvent, SceneState> BUTTON_OFF_MAP =
                new HashMap<SceneEvent, SceneState>();
        HashMap<SceneEvent, SceneState> BUTTON_ON_MAP =
                new HashMap<SceneEvent, SceneState>();
        /* Button off transition */
        BUTTON_OFF_MAP.put(EVENT_BUTTON_PRESS, STATE_BUTTON_ON);
        transitions.put(STATE_BUTTON_OFF, BUTTON_OFF_MAP);
        /* Button on transitions */
        BUTTON_ON_MAP.put(EVENT_BUTTON_PRESS, STATE_BUTTON_OFF);
        transitions.put(STATE_BUTTON_ON, BUTTON_ON_MAP);
        /* Setting the start state */
        startState = STATE_BUTTON_OFF;
        /* Setting the accept states */
        acceptStates.add(STATE_BUTTON_OFF);
        acceptStates.add(STATE_BUTTON_ON);
        return new DFA<SceneState, SceneEvent>(states,
                                               alphabet,
                                               transitions,
                                               startState,
                                               acceptStates);
    }

    @Override
    public String getName() {
        return "StartScene";
    }
}
