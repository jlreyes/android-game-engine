package com.jlreyes.libraries.android_game_engine.scenes;

import android.view.MotionEvent;
import com.jlreyes.libraries.android_game_engine.datastructures.DFA;
import com.jlreyes.libraries.android_game_engine.rendering.GameRenderer;
import com.jlreyes.libraries.android_game_engine.rendering.RenderInfo;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.LayerCameraRenderable;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.sprites.Sprite;
import com.jlreyes.libraries.android_game_engine.threading.Scheduler;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;

import java.util.ArrayList;
import java.util.PriorityQueue;

public abstract class Scene {
    /**
     * Dummy class where each instance represents a Scene Event
     *
     * @author jlreyes
     */
    public static class SceneEvent implements Comparable {
        private int mPriority;

        /**
         * Create a new Scene Event with the given priority. If more than one
         * Scene Event occurs in a game loop, the event with the higher priority
         * takes precedence (and the other will be thrown on the event queue).
         *
         * @param priority The priority of this scene event. A higher value
         *                 indicates a higher priority.
         */
        public SceneEvent(int priority) {
            this.mPriority = priority;
        }

        @Override
        public int compareTo(Object another) {
            SceneEvent other = (SceneEvent) another;
            return other.getPriority() - this.mPriority;
        }

        public int getPriority() {
            return this.mPriority;
        }
    }

    public static final SceneEvent NO_EVENT = new SceneEvent(Integer.MIN_VALUE);

    /**
     * Dummy class where each instance represents a scene state
     *
     * @author jlreyes
     */
    public static class SceneState {
    }

    private String mName;
    private Scheduler mScheduler;
    private GameRenderer mGameRenderer;
    private RenderInfo mRenderInfo;
    private boolean isLoaded;

    /**
     * Functions to be run at the start of the scene after updateStart()
     */
    private ArrayList<Function0<Void>> mStartFunctions;
    private Layer[] mLayers;
    private DFA<SceneState, SceneEvent> mEventDFA;
    private PriorityQueue<SceneEvent> mEventQueue;
    private SceneState mCurrentState;

    public Scene(Scheduler scheduler) {
        this.mName = this.getName();
        this.mScheduler = scheduler;
        this.mGameRenderer = scheduler.getGameRenderer();
    }

    /**
     * Initialize, updating the percentDone variable when necessary.
     */
    public void load(SceneLoader.PercentDone percentDone) {
        this.mStartFunctions = new ArrayList<Function0<Void>>();
        this.mLayers = createLayers();
        this.mEventDFA = loadEventDFA();
        this.mEventQueue = new PriorityQueue<SceneEvent>();
        this.mCurrentState = mEventDFA.getCurrentState();
        this.mRenderInfo = new RenderInfo();        /* Loading Layers */
        for (Layer layer : mLayers) layer.load(percentDone);
        this.onLoad();
        this.isLoaded = true;
    }

    /** Called after everything has been loaded */
    public void onLoad() {}


    /**
     * Called when the application is about to be paused.
     */
    public void onPause() {
        if (this.isLoaded == false) return;
        for (Layer layer : mLayers) layer.onPause();
    }

    /**
     * Called when the application is about to be resumed.
     */
    public void onResume() {
        for (Layer layer : mLayers) layer.onResume();
    }

    /**
     * Called on loading. Returns the created layers for this scene in order
     * such that element 0 in the array corresponds to the layer most in the
     * background
     */
    protected abstract Layer[] createLayers();

    /**
     * Called on loading. Returns a DFA determing the current GameEvent of the
     * scene.
     */
    protected abstract DFA<SceneState, SceneEvent> loadEventDFA();

    public void step(long deltaTime, ArrayList<MotionEvent> inputEvents) {
        updateStart();
        runStartFunctions();
        processInputEvents(inputEvents);
        onEnter(deltaTime, mCurrentState);
        updateLayers(deltaTime, mCurrentState);
        onExit(deltaTime, mCurrentState);
        updateFinish();
    }

    /**
     * First method called in game loop. Sets the current state and then
     * calls {@link Layer#onUpdateStart()} for each layer in this scene.
     */
    protected void updateStart() {
        mCurrentState = mEventDFA.getCurrentState();
        mEventQueue.clear();
        mRenderInfo.clear();
        for (Layer layer : mLayers) layer.onUpdateStart();
    }

    private void runStartFunctions() {
        int length = mStartFunctions.size();
        for (int i = 0; i < length; i++) mStartFunctions.get(i).run();
        mStartFunctions.clear();
    }

    protected void processInputEvents(ArrayList<MotionEvent> inputEvents) {
        int numEvents = inputEvents.size();
        for (int i = 0; i < numEvents; i++) {
            MotionEvent event = inputEvents.get(i);
            Sprite handlingSprite = null;
            int numLayers = mLayers.length;
            for (int j = numLayers - 1; j >= 0; j--) {
                handlingSprite = mLayers[j].processInput(event);
                if (handlingSprite != null) break;
            }
            this.handleInputEvent(event, handlingSprite);
        }
    }

    /**
     * Method called after the given motionEvent was processed.
     *
     * @param motionEvent    The motion event that occurred.
     * @param handlingSprite The sprite that handled the given event. null
     *                       if it was not handled.
     */
    protected void handleInputEvent(MotionEvent motionEvent, Sprite handlingSprite) {
    }

    /**
     * Called just before updating all layers held by this scene and just
     * after processing input events and calling updateStart and calling
     * the start functions.
     *
     * @param deltaTime The time since the last update.
     * @param state     The current state of the scene.
     */
    protected void onEnter(long deltaTime, SceneState state) {
    }

    /**
     * Updates the layers held by this scene
     *
     * @param deltaTime The time passed since the last game step.
     * @param state     The current state of the scene.
     */
    private void updateLayers(long deltaTime,
                              SceneState state) {
        for (Layer layer : mLayers) layer.update(deltaTime, mCurrentState);
    }

    /**
     * Called just after updating all layers held by this scene and just before
     * ending the update.
     *
     * @param deltaTime The time since the last update.
     * @param state     The current state of the scene.
     */
    protected void onExit(long deltaTime, SceneState state) {
    }

    /**
     * Last method called in game loop. Steps the state and deals with each
     * sprite's renderable.
     */
    protected void updateFinish() {
        this.mEventDFA.step(this.mEventQueue.poll()); /* null is okay */
        ArrayList<Renderable> renderables = mRenderInfo.getRenderables();
        int numLayers = mLayers.length;        /* Notifying each layer */
        for (Layer layer : mLayers)
            layer.onUpdateFinish(renderables, mGameRenderer.getThread());
		/* Remove excess cameraRenderables */
        int numRenderables = renderables.size();
        for (int i = 0; i < numRenderables; i++) {
            Renderable renderable1 = renderables.get(i);
            if (renderable1 instanceof LayerCameraRenderable) {
                for (int j = i + 1; j < numRenderables; j++) {
                    Renderable renderable2 = renderables.get(j);
                    if (renderable1 == renderable2) {
						/* We've found an unnecessary duplicate */
                        renderables.remove(j);
                        numRenderables -= 1;
                        j -= 1;
                    } else if (renderable2 instanceof LayerCameraRenderable)
                        break;
                }
            }
        }
    }

    /**
     * Sets the scene event for this loop
     */
    public void setSceneEvent(SceneEvent sceneEvent) {
        this.mEventQueue.add(sceneEvent);
    }

    /**
     * Request the sprite with the given name.
     */
    public Sprite requestSprite(String name) {
        for (Layer layer : mLayers) {
            Sprite[] sprites = layer.getSprites();
            for (Sprite sprite : sprites) {
                if (name.equals(sprite.getName())) return sprite;
            }
        }
        throw new RuntimeException(name + " does not exist!");
    }

    /**
     * Request the layer with the given name.
     */
    public Layer requestLayer(String name) {
        String fullName = mName + "." + name;
        for (Layer layer : mLayers) {
            if (layer.getName().equals(fullName)) return layer;
        }
        throw new RuntimeException(fullName + " does not exist!");
    }

    /*
     * Getters and Setters
     */
    public abstract String getName();

    public Scheduler getScheduler() {
        return mScheduler;
    }

    public Layer[] getLayers() {
        return mLayers;
    }

    public ArrayList<Function0<Void>> getStartFunctions() {
        return mStartFunctions;
    }

    public SceneState getCurrentState() {
        return this.mCurrentState;
    }

    public RenderInfo getRenderInfo() {
        return mRenderInfo;
    }
}
