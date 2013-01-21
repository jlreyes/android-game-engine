package com.jlreyes.libraries.android_game_engine.rendering.renderable;

import android.opengl.GLES20;
import com.jlreyes.libraries.android_game_engine.rendering.RenderUtils;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;

import java.nio.FloatBuffer;

public class Polygon extends Renderable {
    public static final String TAG = "Polygon";

    private FloatBuffer mVertices;
    private int mNumVertices;

    public Polygon(float[] vertices) {
        super(Renderable.RenderType.TRIANGLE_STRIP, vertices);        /* Get vertices created by parent */
        this.mVertices = getVertices();
        this.mNumVertices = getNumVertices();
    }

    @Override
    public boolean isVisible(LayerCamera gameCamera) {
        /* We will go through all the triangles composing this polygon and
         * determine if they intersect with the gamecamera view */
        return true;
    }

    @Override
    public void draw(int positionHandle,
                     int mvpMatrixHandle,
                     int texCoordHandle,
                     int texRGBHandle,
                     int texAHandle,
                     float ratio,
                     float[] mvpMatrix,
                     float[] viewMatrix,
                     float[] modelMatrix,
                     float[] projectionMatrix) {        /* Transform the model matrix as necessary */
        updateModelMatrix(modelMatrix);
		/* Pass position information */
        mVertices.position(0);
        GLES20.glVertexAttribPointer(positionHandle,
                                     RenderUtils.VERTEX_DIM, // Vector Components
                                     GLES20.GL_FLOAT, // Component Type
                                     false, // No normalization
                                     RenderUtils.VERTEX_DIM *  // Byte offset
                                     RenderUtils.GL_FLOAT_SIZE,// between vertices
                                     mVertices);
        GLES20.glEnableVertexAttribArray(positionHandle);
		/* Create the final combined Model View Projection matrix for 
		 * rendering */
        RenderUtils.CreateMVPMatrix(mvpMatrix,
                                    viewMatrix,
                                    modelMatrix,
                                    projectionMatrix);
		/* Passing the mvpMatrix to the opengl program */
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
		/* Binding the texture, making sure no other thread is updating it. */
        Texture texture = getTexture();
        synchronized (texture) {
        	/* Binding texCoordinate */
            FloatBuffer texIndices = texture.getActiveIndices();
            texIndices.position(0);
            GLES20.glVertexAttribPointer(texCoordHandle,
                                         Texture.VERTEX_DIM,
                                         GLES20.GL_FLOAT,
                                         false,
                                         Texture.VERTEX_DIM *
                                         RenderUtils.GL_FLOAT_SIZE,
                                         texIndices);
            GLES20.glEnableVertexAttribArray(texCoordHandle);
    		/* Binding the RGB texture */
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            int activeRGBHandle = texture.getActiveRGBHandle();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, activeRGBHandle);
            GLES20.glUniform1i(texRGBHandle, 0);
    		/* Binding the Alpha texture */
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            int activeAHandle = texture.getActiveAHandle();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, activeAHandle);
            GLES20.glUniform1i(texAHandle, 1);
        }
        
		/* Drawing the renderable */
        GLES20.glDrawArrays(getRenderType().GL_RENDER_ID, 0, mNumVertices);
    }

    @Override
    public Polygon copy() {
		/* Deep copy of vertices */
        float[] vertices = new float[mNumVertices * RenderUtils.VERTEX_DIM];
        for (int i = 0; i < vertices.length; i++)
            vertices[i] = mVertices.get(i);
		/* New Renderable */
        Polygon polygon = new Polygon(vertices);
        return polygon;
    }

}
