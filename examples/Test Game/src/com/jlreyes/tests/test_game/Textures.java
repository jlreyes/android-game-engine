package com.jlreyes.tests.test_game;

import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController.TexStateInfo;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController.TexInfo;
import com.jlreyes.libraries.android_game_engine.texinit.TextureInitActivity;

/**
 * User: James
 * Date: 1/14/13
 * Time: 11:23 PM
 */
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
}
