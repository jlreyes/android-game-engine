package com.jlreyes.libraries.android_game_engine.io.storage;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;

public class StorageHelper {
    /**
     * Endianess that we use
     */
    public static final ByteOrder ENDIAN = ByteOrder.BIG_ENDIAN;

    public static String ReadRawTextFile(Context context, int resourceId) {        /* Opening the file */
        InputStream inputStream =
                context.getResources().openRawResource(resourceId);
        InputStreamReader inputStreamReader =
                new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);        /* Creating the string */
        StringBuilder text = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return text.toString();
    }
}
