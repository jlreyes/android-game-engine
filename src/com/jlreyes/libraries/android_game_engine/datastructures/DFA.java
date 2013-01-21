package com.jlreyes.libraries.android_game_engine.datastructures;

import com.jlreyes.libraries.android_game_engine.scenes.Scene;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation of Deterministic Finite Automata where each state is type State
 * and each letter in the alphabet is type Letter
 *
 * @author jlreyes
 */
public class DFA<State, Letter> {
    public static final DFA<Scene.SceneState, Scene.SceneEvent> EMPTY_DFA =
            new DFA<Scene.SceneState, Scene.SceneEvent>(new HashSet<Scene.SceneState>(),
                                                        new HashSet<Scene.SceneEvent>(),
                                                        new HashMap<Scene.SceneState, HashMap<Scene.SceneEvent, Scene.SceneState>>(),
                                                        new Scene.SceneState(),
                                                        new HashSet<Scene.SceneState>());

    private HashSet<State> mStates;
    private HashSet<Letter> mAlphabet;
    private HashMap<State, HashMap<Letter, State>> mTransitions;
    private State mStartState;
    private HashSet<State> mAcceptStates;

    private State mCurrentState;

    public DFA(HashSet<State> states,
               HashSet<Letter> alphabet,
               HashMap<State, HashMap<Letter, State>> transitions,
               State startState,
               HashSet<State> acceptStates) {
        this.mStates = states;
        this.mAlphabet = alphabet;
        this.mTransitions = transitions;
        this.mStartState = startState;
        this.mAcceptStates = acceptStates;

        this.mCurrentState = startState;
    }

    /**
     * Steps the DFA using the given letter in the alphabet. If null is passed,
     * we do a self-loop
     */
    public void step(Letter letter) {
        if (letter == null) return;
        HashMap<Letter, State> letterTransitions =
                mTransitions.get(mCurrentState);        /* If our current state has no defined transitions, it must, so we
         * do a self-loop */
        if (letterTransitions == null) return;
        State nextState = letterTransitions.get(letter);
		/* Since this is deterministic, we do a self loop if no transition is
		 * defined. */
        if (nextState == null) return;
        mCurrentState = nextState;
    }

    /*
     * Getters and Setters
     */
    public State getCurrentState() {
        return mCurrentState;
    }

    public HashSet<State> getAcceptStates() {
        return mAcceptStates;
    }

    public State getStartState() {
        return mStartState;
    }

    public HashSet<Letter> getAlphabet() {
        return mAlphabet;
    }

    public HashSet<State> getStates() {
        return mStates;
    }
}
