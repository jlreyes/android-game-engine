package com.jlreyes.tests.test_game.scenes.startscene;

import com.jlreyes.libraries.android_game_engine.scenes.Layer;
import com.jlreyes.libraries.android_game_engine.scenes.Loader;
import com.jlreyes.libraries.android_game_engine.scenes.Scene;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.GameCamera;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.threading.logic.GameCommand;
import com.jlreyes.libraries.android_game_engine.threading.logic.LogicManager;
import com.jlreyes.libraries.android_game_engine.utils.math.Tuple;
import com.jlreyes.libraries.android_game_engine.utils.math.function.Function0;
import com.jlreyes.tests.test_game.Scenes;
import com.jlreyes.tests.test_game.Textures;

/**
 * User: James
 * Date: 1/15/13
 * Time: 5:51 PM
 */
public class MainLayer extends Layer {
    private MainCharacter mMainCharacter;

    public MainLayer(StartScene scene) {
        super("MainLayer", scene);
    }

    public void load(Loader.PercentDone percentDone) {
        super.load(percentDone);

        /* Creating the middle "main character" */
        this.mMainCharacter = new MainCharacter(this);
        this.addSprite(this.mMainCharacter);

        /* Creating a button */
        Function0<Void> onButtonTap = new Function0<Void>() {
            @Override
            public Void run() {
                MainLayer self = MainLayer.this;
                Scene scene = self.getParentScene();
                LogicManager l = scene.getScheduler().getLogicManager();
                l.addGameCommand(new GameCommand(GameCommand.Command.LOAD_REPLACE,
                                                 new Tuple(Scenes.START_SCENE, Scenes.START_SCENE)));
                return null;
            }
        };
        Button button = new Button("topleftbutton", this, onButtonTap,
                                   25, 25, 1, 0, 10, 10);
        this.addSprite(button);
    }

    @Override
    public GameCamera createCamera() {
        return new LayerCamera("MainCamera", this, 100, 0, 0, 1, 0);
    }

    @Override
    public void updateOnState(long deltaTime, Scene.SceneState state) {
        if (state == StartScene.STATE_BUTTON_ON) onButtonOn(deltaTime);
        else if (state == StartScene.STATE_BUTTON_OFF) onButtonOff(deltaTime);
        else throw new IllegalStateException();
    }

    private void onButtonOff(long deltaTime) {
    }

    private void onButtonOn(long deltaTime) {
        this.mMainCharacter.rotateBy(1);
    }

    public void onMainCharacterTap() {
        Scene.SceneState oldState = this.getParentScene().getCurrentState();
        this.getParentScene().setSceneEvent(StartScene.EVENT_BUTTON_PRESS);
        if (oldState == StartScene.STATE_BUTTON_OFF)
            this.mMainCharacter.setTextureState(Textures.MAIN_CHARACTER.WALKING);
        else
            this.mMainCharacter.setTextureState(Textures.MAIN_CHARACTER.DEFAULT);
    }
}
