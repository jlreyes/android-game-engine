Android Game Engine (AGE) - Introduction
================================================================================
Author: jlreyes (James Reyes)<br />

This project is a 2D Android game engine written in bulk over the summer of 2012.
I have spent a couple days recently working to separate it from its initial
game and fixing bugs.

The game engine is fully functional and work on any Android device running
version 2.2 or higher (OpenGL ES 2 is used). There is minimal allocation and
no allocation at all in the game loop in order to eliminate garbage collection
slowdowns. Garbage collection should only occur during loading.

I really recommend looking over the example game described in the "Examples"
section below. A lot of the framework for your game can be taken from there.

The source code contains a decent amount of documentation (there can always
be more), so make sure to check it out if you want to know about all the
available methods.

The JavaDoc is located at <code>doc/index.html</code>

Structure
================================================================================
AGE splits all of its tasks into separate threads. That is, we have a
separate UI, logic, rendering, and animation threads. At the same time, we have
many other temporary threads such as loading threads that are managed by the
scheduler. The scheduler thread is the first thread initialized when the
game begins. It proceeds to start all the other threads I just listed. It also
manages pausing and resuming threads when the application is paused and resumed.

The logic thread runs the main game loop. Every frame it updates each scene,
generates a list of Renderables and passes them off to the rendering thread.
It also manages loading and killing of scenes.

The rendering thread simply draws the most recently available list of
renderables as fast as it can.

The UI thread listens for events and passes them off to the logic thread for
consumption in the next update loop.

The animation thread updates displayed textures that have frames.

Renderables
================================================================================
Renderable is the class used to describe an object that is to be rendered by
OpenGL. A renderable is assumed to have vertices, indices, and a texture.
Since this is a 2D engine, the vertices are two-dimensional.

There are many types of renderables included, and most of the time you should
never have to create your own. However, if you are interested, the renderables
are located under <code>src/.../rendering/renderable</code>. 

The shaders used are located at <code>RenderUtils.FRAGMENT_SHADER</code> and
<code>RenderUtils.VERTEX_SHADER</code>. They originated from
<code>templates/shaders</code>.

Textures
================================================================================
Textures are a pain on Android. First of all, there is no standard
compression format ***that is good***. ETC1 is available on almost all
Android phones supporting OpenGL ES 2. However, it does not support an
Alpha channel. For 2D sprite based games, this is terrible. However, if one
wants to be future-proof and standard, ETC1 is the best option.

To deal with this, all textures are split into two textures, an RGB texture
and an Alpha texture. The Alpha Texture is uncompressed due to quality issues,
but the RGB texture is compressed using ETC1. If you wish to turn on ETC1
compression for Alpha textures, there is only one line in
<code>src/.../sprites/textures/TextureLoader.java</code> you need to change.
I'll probably make it an option you can easily change in the future.

Another problem is that not all phones have the same maximum texture size.
So even if users provide me with nicely formatted textures, they may be
too large for some devices.

Dealing with this is a little trickier. First, note that in most 2D games,
a sprite has different states such as walking, running, jumping, etc. So
in AGE, you provide me with a high, medium, and low resolution version
of a texture state in a strip format (|1|2|3|), create a texture state
info class, attach that to a texture info class, and provide me an instance
of that class. For every texture info object, I generate as many texture
images/parts (I minimize the texture sizes, of course)  as I need for that
texture and then I save all the info and texture ETC1/Bitmap info on the
device. This only occurs the first time the user loads the game, or when
the version number in the texture object increases. Otherwise, the
texture initializer simply reads the file data and verifies its integrity.

A benefit to this approach is that implementing downloadable textures is
very easy. It is on my TODO list below.

Here is an example of declaring textures taken from
<code>examples/Test Game</code>.

```java
public class Textures extends TextureInitActivity {

    public static final TexInfo_Null NULL_TEXTURE = new TexInfo_Null();
    private static class TexInfo_Null  extends TexInfo {
        public TexInfo_Null() {
            super("NullTexture", 1,
                  new int[] {4, 4},
                  new int[] {4, 4},
                  new int[] {4, 4},
                  new TexStateInfo[]{
                        new TexStateInfo("default",
                                         new int[]{60},
                                         R.drawable.null_texture_default_high,
                                         R.drawable.null_texture_default_med,
                                         R.drawable.null_texture_default_low)
                });
        }
    }

    public static final TexInfo_Load LOADING = new TexInfo_Load();
    public static class TexInfo_Load extends TexInfo {
        public static final TexStateInfo DEFAULT =
                new TexStateInfo("default", new int[] {60},
                                 R.drawable.loading_default_high,
                                 R.drawable.loading_default_high,
                                 R.drawable.loading_default_high);

        public TexInfo_Load() {
            super("Loading", 1,
                  new int[] {256, 128},
                  new int[] {256, 128},
                  new int[] {256, 128},
                  new TexStateInfo[]{DEFAULT});
        }
    }

    public static final TexInfo_MC MAIN_CHARACTER = new TexInfo_MC();
    public static class TexInfo_MC extends TexInfo {
        public static final TexStateInfo DEFAULT =
                new TexStateInfo("default",
                                 new int[] {60},
                                 R.drawable.main_character_default_high,
                                 R.drawable.main_character_move_med,
                                 R.drawable.main_character_default_low);
        public static final TexStateInfo WALKING =
                new TexStateInfo("walking",
                                 new int[] {1000, 1000},
                                 R.drawable.main_character_move_high,
                                 R.drawable.main_character_move_med,
                                 R.drawable.main_character_move_low);

        public TexInfo_MC() {
            super("MainCharacter", 8,
                  new int[]{256, 256},
                  new int[]{128, 128},
                  new int[]{64, 64},
                  new TexStateInfo[] {DEFAULT, WALKING});
        }
    }

```

So to create a new <code>Texture</code> instance of the texture for the
main character, all I would do is call (if the texInfo was under Textures)
<code>TextureLoader.LoadTexture(Textures.MAIN_CHARACTER, scheduler)</code>
where <code>scheduler</code> is the game's scheduler.

A texture instance is reusable and, in fact, it is best to reuse a texture
when you can to save memory. There is no downside for non-animated textures.
For animated textures, all animations will occur at the same time.

To help debug, there is a texture viewer activity in
<code>examples/Tex Game</code>. Feel free to use it for your own project.

Sprites
================================================================================
Entities in AGE are represented as sprites. A sprite holds a renderable and a
texture that represent this sprite. You can do anything you would expect to
a sprite including defining its scale, angle, and position. A sprite has an
update function called every loop, but if you follow the recommended design
pattern, it is rarely used.

There is type of sprite called a <code>SpriteWrapper</code> that can be used if
you want to wrap multiple sprites in a single sprite. This allows you to move
the sprite wrapper and subsequently move all contained sprites as well.

Here is an example of a sprite (taken from <code>examples/Test Game</code>).

```java
  public class MainCharacter extends RectSprite {
      public MainCharacter(MainLayer layer) {
          super("MainCharacter", layer, Textures.MAIN_CHARACTER, true, 20, 20);
      }

      @Override
      public void update(long deltaTime) {}

      @Override
      public boolean onTouchDown(MotionEvent e) {
          return true;
      }

      @Override
      public void onTouchCancel(MotionEvent e) {}

      @Override
      public boolean onTouchUp(MotionEvent e) {
          ((MainLayer) this.getLayer()).onMainCharacterTap();
          return true;
      }
  }
```

So <code>new MainCharacter(myLayer)</code> would create a new MainCharacter
sprite on layer myLayer with the texture matching the texture info
Textures.MAIN_CHARACTER. The sprite would be movable and would appear at
coordinates (20, 20).

* <code>update(long deltaTime)</code> is called every update loop with deltaTime
  being the time since the last update.
* <code>onTouchDown</code> is called when a touch even originates on this sprite.
  Return true if and only if you wish to handle this event.
* <code>onTouchUp</code> is called only after <code>onTouchDown</code> is fired
  and only if the touch event did not move too far from the orignal touch point.
  Return true if and only if you wish to handle this event. Since this event is
  dependent on <code>onTouchDown</code> occurring, you must return true in
  <code>onTouchDown</code> as well for this to be called.
* <code>onTouchCancel</code> is called when a touch down event was called
  but the finger moved too far or another touch event appeared. For this
  to be called, <code>onTouchDown</code> must return true.

To see more you can do with sprites, take a look at the example game or the
javadoc.


Layers
================================================================================
A layer is a class that holds a set of sprites and updates every loop,
doing something based off of the state of the overall Scene. Scenes are
explained below, but they hold as many layers as needed and every loop have
a "state".

A layer contains a load function that you should override to load sprites.
The loading happens on a separate thread.

Every layer holds a Camera Sprite. A Camera Sprite's width and height define
the viewport that we can see. Its position is the position of the viewport.
A layer can optionally hold an Indirect Camera Sprite. This is a type of
camera that mimics the camera of another layer. You define the camera for a
layer when you implement the class. You can supply a camera an update function
to execute every loop.

Here is an example of a created layer (taken from
<code>examples/Test Game</code>).

***Any references to the DFA or the "state" of a scene can be understood be looking
at the Scene section below.***

```java
public class MainLayer extends Layer {
    private MainCharacter mMainCharacter;

    public MainLayer(StartScene scene) {
        super("MainLayer", scene);
    }

    /**
     * Called when this layer is loaded. All sprite initialization happens here.
     */
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
        Button button = new Button("toprightbutton", this, onButtonTap,
                                   25, 25, 1, 0, 10, 10);
        this.addSprite(button);
    }

    /** Returns a new instance of this layer's camera */
    @Override
    public GameCamera createCamera() {
        return new LayerCamera("MainCamera", this, 100, 0, 0, 1, 0);
    }

    /**
     * Called every frame no matter what.
     * @param deltaTime How much time (in ms) has advanced since the last frame
     * @param state The state that our Scene DFA is in. */
    @Override
    public void updateOnState(long deltaTime, Scene.SceneState state) {
        if (state == StartScene.STATE_BUTTON_ON) onButtonOn(deltaTime);
        else if (state == StartScene.STATE_BUTTON_OFF) onButtonOff(deltaTime);
        else throw new IllegalStateException();
    }

    /* Called when the button is on. I.E, we have not tapped or untapped the
     * main character */
    private void onButtonOff(long deltaTime) {
    }

    /* Called when the button is on. I.E, we have tapped the main character */
    private void onButtonOn(long deltaTime) {
        this.mMainCharacter.rotateBy(1);
    }

    /* Called after the main character has been tapped. We sent an event to the
     * scene */
    public void onMainCharacterTap() {
        Scene.SceneState oldState = this.getParentScene().getCurrentState();
        this.getParentScene().setSceneEvent(StartScene.EVENT_BUTTON_PRESS);
        if (oldState == StartScene.STATE_BUTTON_OFF)
            this.mMainCharacter.setTextureState(Textures.MAIN_CHARACTER.WALKING);
        else
            this.mMainCharacter.setTextureState(Textures.MAIN_CHARACTER.DEFAULT);
    }
}

```

Scenes
================================================================================
A scene is a independent set of layers that has a state controlled by a
Deterministic Finite Automaton (http://en.wikipedia.org/wiki/Deterministic_finite_automaton).
Quite simply, it is a state machine that on a given node, goes to another
node based on an event or "self-loops" and stays on itself. You are required to
a supply a DFA via an implemented function. But you can pass
<code>DFA.EMPTY_DFA</code> if you do not wish to use a DFA.

To transition from one state to another simply call
<code>scene.setSceneEvent</code>. If you do not call this method in a frame,
the frame's event is assumed to be null, which causes a self-loop. Since
it is possible for more than one event to be set for a frame, events are
queued in a priority queue. When you create an event you specify its priority.

The benefit of using a DFA is that is forces you to think about the state of
your scene in all your layers. Furthermore, since events are queued, you don't
have to worry about two conflicting things happening in the same loop or the
ordering in which they appear. It allows you to treat each layer independently.

See <code>src/.../datastructures/DFA.java</code> for more details.

You supply an array of layers in your scene via an implemented method. The
order of the array you supply is also the layering order.

Because scenes are independent, it is possible to display more than one
scene on the screen at a time. In fact, there is a load scene that is loaded
and then displayed every time another scene is being loaded. To load a scene,
or kill a scene, send a <code>GameCommand</code> to the logic manager with
<code>logicManager.addGameCommand</code>. You can view the type of game
commands and arguments in <code>src/.../threading/logic/GameCommand.java</code>.

To describe a scene to the game, use the <code>SceneInfo</code> class under
<code>src/.../scenes/SceneController.java</code>. For now, the scene info
class simply contains a class object for the scene, but in the future it
may contain more. To clarify, a <code>SceneInfo</code> instance is what
we pass to the logic manager to add or remove a scene. 

Here is an example of a scene (taken from <code>examples/Test Game</code>).

```java
public class StartScene extends Scene {
    /* Scene States */
    public static final SceneState STATE_BUTTON_OFF = new SceneState();
    public static final SceneState STATE_BUTTON_ON = new SceneState();
    /* Scene Events. We give each a priority. A higher priority indicates
     * a higher priority event. */
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


    /* Our DFA uses SceneStates for its nodes and SceneEvents for its
     * transitions. */
    @Override
    protected DFA<SceneState, SceneEvent> loadEventDFA() {
        /* We first intialize all variables needed to create the DFA.
         * - We have a set of states/nodes
         * - We have a set of possible events/i.e, the alphabet.
         * - We have a Map of transitions. That is, given a node/state,
         *   we have a Map from an event to another node/state
         * - We have a starting state/node
         * - We have accepting state/nodes (not used in AGE)
         */
        HashSet<SceneState> states = new HashSet<SceneState>();
        HashSet<SceneEvent> alphabet = new HashSet<SceneEvent>();
        HashMap<SceneState, HashMap<SceneEvent, SceneState>> transitions =
                new HashMap<SceneState, HashMap<SceneEvent, SceneState>>();
        SceneState startState = null;
        HashSet<SceneState> acceptStates = new HashSet<SceneState>();

        /* Adding all of our states */
        states.add(STATE_BUTTON_OFF);
        states.add(STATE_BUTTON_ON);

        /* Adding all of our events */
        alphabet.add(EVENT_BUTTON_PRESS);

        /* Creating maps for each node/state */
        HashMap<SceneEvent, SceneState> BUTTON_OFF_MAP =
                new HashMap<SceneEvent, SceneState>();
        HashMap<SceneEvent, SceneState> BUTTON_ON_MAP =
                new HashMap<SceneEvent, SceneState>();

        /* State: Button Off Map */
        BUTTON_OFF_MAP.put(EVENT_BUTTON_PRESS, STATE_BUTTON_ON);
        transitions.put(STATE_BUTTON_OFF, BUTTON_OFF_MAP);

        /* State: Button On Map */
        BUTTON_ON_MAP.put(EVENT_BUTTON_PRESS, STATE_BUTTON_OFF);
        transitions.put(STATE_BUTTON_ON, BUTTON_ON_MAP);

        /* Setting the starting state */
        startState = STATE_BUTTON_OFF;

        /* Setting the accepting states, again not used in AGE */
        acceptStates.add(STATE_BUTTON_OFF);
        acceptStates.add(STATE_BUTTON_ON);

        /* Returning the new DFA */
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
```

Usage
================================================================================
I assume you have basic knowledge of Java and Android development, so if you
have both and something is confusing- please let me know and I'll try to be
more clear.

1. Create an Android Application project in Eclipse, Intellij, or your favorite
   Android ide.
2. Include the android-game-engine.jar as a dependency in your Java app. You
   can use the src folder for the source code.
3. You need to add a couple of things to your AndroidManifest.xml.

  ```xml
  <uses-sdk android:minSdkVersion="8" android:targetSdkVersion="17"/>
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
  <uses-feature android:glEsVersion="0x00020000" android:required="true" />
  <supports-gl-texture android:name="GL_OES_compressed_ETC1_RGB8_texture" />
  ```

  We need the permissions in order to save and read textures (see the texture
  section). ETC1 is used to compress the textures and we use OpenGL ES
  2 so we need to require both of those features. Technically the target SDK
  can be whatever you want as long as it is greater than the Min SDK number.

4. You need to create a game activity. That is, the activity that your
   game will run in. Here is an example activity class (taken from
   <code>examples/Test Game</code>).

  ```java
  public class GameActivity extends AGEActivity {

      /* Do any initializing here */
      @Override
      protected void init() {}

      /* Returns the texture initialization activity's class object. I will
       * talk more about this in step 5. */
      @Override
      protected Class<? extends TextureInitActivity> getTexInitActivityClass() {
          return Textures.class;
      }

      /* Returns the first scene to be loaded. */
      @Override
      protected SceneController.SceneInfo getStartSceneInfo() {
          return Scenes.START_SCENE;
      }

      /* Returns the load scene for your game. */
      @Override
      protected SceneController.SceneInfo getLoadSceneInfo() {
          return Scenes.LOAD_SCENE;
      }
  }

  ```

  This shouldn't be necessary if you're using an IDE, but make sure to add the
  activity to your manifest with the intent filter to appear on the homescreen.
  Also, you should make sure to specify that the activity should only allow
  landscape orientation. So something like this:

  ```xml
  <activity android:name="GameActivity"
                    android:label="@string/app_name"
                    android:screenOrientation="landscape">
    <intent-filter>
      <action android:name="android.intent.action.MAIN"/>
      <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
  </activity>

  ```

5. You need to create a TextureInitActivity. This activity will be
   launched when you first start your game. This activity basically examines
   all the textures you have declared and determines if their corresponding
   files are on the device. If they are not, they are outdated, or they are
   corrupted, the files are created in this activity.

   Here is an example Texture activity (pulled from
   <code>examples/Test Game</code>).

  ```java
  public class Textures extends TextureInitActivity {
      /*...*/
      @Override
      public TexController.Resolution getResolution() {
          return TexController.Resolution.HIGH;
      }

      @Override
      public TexInfo[] getTextures() {
          return new TexInfo[] {
              NULL_TEXTURE, MAIN_CHARACTER, LOADING
          };
      }
      /*...*/
  }

  ```

  There are two main methods you implement in your Texture Initialization
  activity:
  * <code>getResolution</code> returns the texture resolution you want to load
    on the device. For more, see the textures section below.
  * <code>getTextures</code> returns an array of textures that your game uses.
    I use my Textures class to declare my textures so I can refer to my texture
    info objects in game as Textures.MAIN_CHARACTER.

  Again, make sure to add the activity to your manifest.
  ```xml
  <activity android:name=".Textures"
            android:screenOrientation="landscape" />
  ```

6. Create your start scene. See the Scenes section above to see how. Or check
   out the example in <code>examples/Test Game</code>

   Make sure to create a SceneInfo object for this class and to supply it to
   your Game Activity.

   ```java
    public class Scenes {
        public static final SceneInfo START_SCENE = new SceneInfo(StartScene.class);
    }

    public class GameActivity extends AGEActivity {
      @Override
      protected SceneController.SceneInfo getStartSceneInfo() {
          return Scenes.START_SCENE;
      }
    }
    ```


7. Create your loading scene. A loading scene is slightly different from other
   scenes in that they are loaded on the logic thread (so keep the asset loading
   small!). Their layer methods are, by default, slightly different too.
   However, theoretically, a load scene could be anything. A load scene is no
   different than any other scene under the hood.

   Here is an example LoadScene implementation and LoadLayer implementation.
   It is taken from <code>examples/Test Game</code>:

   ```java
   public class MyLoadScene extends LoadScene {
       public MyLoadScene(Scheduler scheduler) {
           super(scheduler);
       }

       @Override
       protected LoadLayer[] createLoadLayers() {
           return new LoadLayer[] {
               new MyLoadLayer(this)
           };
       }
   }


   public class MyLoadLayer extends LoadLayer {
     public MyLoadLayer(LoadScene scene) {
         super(scene);
     }

     public void load(Loader.PercentDone p) {
         super.load(p);
         this.addSprite(new LoadText(this));
     }

     @Override
     public void onUpdate(int percent) {}

     @Override
     public GameCamera createCamera() {
         LayerCamera c = new LayerCamera("LoadCamera", this, 10, 0, 0, 1, 0); 
         c.setAnchorPoint(LayerCamera.AnchorPoint.CENTER);
         return c;
     }
   }
   ```
   As you can see, the only thing you need to implement in the Load Scene is
   the method returning all the layers.

   Note that the <code>Load.PercentDone p</code> object refers to the percent
   that the actual scene being loaded is done. It is not, like in other
   scenes, meant to be updated.

   The <code>onUpdate</code> method is meant to be implemented and what you
   do with it is up to you (the percent passed is the percent that the scene
   being loaded is at).

   The <code>createCamera</code> method is exactly the same as in normal layers.

   Make sure to create a SceneInfo object for this class and to supply it to
   your Game Activity.

   ```java
    public class Scenes {
        public static final SceneInfo LOAD_SCENE = new SceneInfo(MyLoadScene.class);
    }

    public class GameActivity extends AGEActivity {
      @Override
      protected SceneController.SceneInfo getLoadSceneInfo() {
          return Scenes.LOAD_SCENE;
      }
    }
   ```

Examples
================================================================================
See <code>examples/Test Game</code> for a basic example of a working Android
Game written in this library (note that its not exactly a game, just a demo).

The demo also includes an activity for viewing the generated texture files
stored on the device. This can be helpful for debugging Texture issues,
so feel free to use this in your application.

Just import the project into Eclipse or Intellij and test it on your Android
device.

Misc
================================================================================
A couple of interesting things I had to do include:


<code>.../datastructures/MinAllocHashSet.java</code><br />
I had to implement a HashSet based on arrays in order to prevent allocation from
removing and adding elements.


<code>.../sprites/textures/TextureLoader.java</code><br />
Dealing with textures on Android was a pain. To see how I did it, looking
at this file would be the most enlightening.

TODO
================================================================================
Note I am not actively working on any of these features, this is just a list of
features I intend to add if/when I pick this project up in the future.

- Better touch handling
- Different sized texture frames
- Layer grids and camera bounds checking
- font sprites including numbers and text
- Cleaner cleanup of temporary threads
- Music and Sound Manager
- Custom exception handling
- Error Game Commands
- VBOs
- Texture downloading
- Scene, Sprite, Texture management tool (to auto-generate necessary java)

License
================================================================================
This work is licensed under the Creative Commons Attribution-NonCommercial 3.0
Unported License. To view a copy of this license, visit
http://creativecommons.org/licenses/by-nc/3.0/.
