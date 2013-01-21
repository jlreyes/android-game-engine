package com.jlreyes.tests.test_game.texviewer;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.jlreyes.libraries.android_game_engine.sprites.textures.TexController;
import com.jlreyes.tests.test_game.Textures;


public class TexViewerActivity extends ListActivity {
    public static final String TAG = "Tex Viewer";

    private String[] mTextureNames;
    private ListAdapter mListAdapter;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean skipInit = this.getIntent().getBooleanExtra("texInitSkip", false);
        if (skipInit == false) {
            Intent intent = new Intent(this, Textures.class);
            intent.putExtra("returnClass", TexViewerActivity.class);
            startActivity(intent);
            this.finish();
        }

        load();
        mListAdapter = 
        		new ArrayAdapter<String>(this,
        								 android.R.layout.simple_list_item_1, 
        								 mTextureNames);
        setListAdapter(mListAdapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        super.onListItemClick(l, v, position, id);
        launchViewer(position);
    }
    
    private void load() {
        /* Load texture */
        TexController.TexInfo[] texes = TexController.TEXTURES;
        int numTexes = texes.length;
        this.mTextureNames = new String[texes.length];
        for (int i = 0; i < numTexes; i++)
            mTextureNames[i] = texes[i].Name;
    }
    
    private void launchViewer(int texIndex) {
        Intent intent = new Intent(this, TexViewer.class);
        intent.putExtra("index", texIndex);
        startActivity(intent);
    }
    
}
