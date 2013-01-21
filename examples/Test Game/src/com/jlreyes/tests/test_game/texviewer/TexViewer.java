package com.jlreyes.tests.test_game.texviewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TextureLoader;

public class TexViewer extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        LinearLayout layout = getLayout(intent.getExtras().getInt("index"));
        setContentView(layout);
    }

    private LinearLayout getLayout(int texIndex) {
    	/* Get texture and bitmaps */
        TexController.TexInfo texInfo = TexController.TEXTURES[texIndex];
    	Texture tex = TextureLoader.LoadTextureForVerification(texInfo, this);
    	Texture.TexturePart[] texParts = tex.getTextureParts();
    	/* Create Layout */
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int i = 0;
        for (Texture.TexturePart texPart : texParts) {
        	/* Get info */
        	Bitmap bitmap = texPart.generateARGBBitmap(this);
        	int width = bitmap.getWidth();
        	int height = bitmap.getHeight();
        	/* Creating container */
        	LinearLayout container = new LinearLayout(this);
        	container.setOrientation(LinearLayout.VERTICAL);
        	/* Creating title */
        	TextView rgbText = new TextView(this);
        	String rgbTitle = "Part " + i  + ":" + width + "x" + height +
                              " - Frames " + texInfo.NumFrames +
                              " - v" + texInfo.Version;
        	rgbText.setText(rgbTitle.toCharArray(), 0, rgbTitle.length());
        	container.addView(rgbText);
        	/* ARGB View */
        	ImageView rgbView = new ImageView(this);
        	rgbView.setImageBitmap(bitmap);
        	container.addView(rgbView);
        	/* Add view */
        	layout.addView(container);
        }  
        return layout; 
    }
}
