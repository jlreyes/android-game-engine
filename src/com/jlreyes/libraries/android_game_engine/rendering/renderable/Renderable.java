package com.jlreyes.libraries.android_game_engine.rendering.renderable;

import android.opengl.GLES20;
import android.opengl.Matrix;
import com.jlreyes.libraries.android_game_engine.rendering.RenderUtils;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;
import com.jlreyes.libraries.android_game_engine.sprites.textures.Texture;
import com.jlreyes.libraries.android_game_engine.utils.math.FloatMathUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Class storing information, and only the information. needed to render some OpenGl object.
 *
 * @author jlreyes
 */
public abstract class Renderable {
    public static enum RenderType {
        NONE(-1),
        TRIANGLE_STRIP(GLES20.GL_TRIANGLE_STRIP);

        public int GL_RENDER_ID;

        private RenderType(int glRenderId) {
            this.GL_RENDER_ID = glRenderId;
        }
    }

    private RenderType mRenderType;
    private Texture mTexture;
    private FloatBuffer mVertices;
    private int mNumVertices;

    /**
     * Coordinate to render at
     */
    private float mPosX;
    private float mPosY;
    /**
     * Angle to render the renderable at
     */
    private float mAngle;
    /**
     * Scale to render at
     */
    private float mScaleX;
    private float mScaleY;

    public static final float TRANSLATE_EPSILON = 0.001f;
    public static final float ANGLE_EPSILON = 0.01f;
    public static final float SCALE_EPSILON = 0.01f;

    /**
     * Same as Renderable(Renderable.NONE, null)
     */
    public Renderable() {
        this(RenderType.NONE, null);
    }

    /**
     * Creates a new renderable that renders with the given type.
     */
    public Renderable(RenderType renderType, float[] vertices) {
        this.mRenderType = renderType;
        if (renderType != RenderType.NONE) {
            this.mTexture = null;
            this.mNumVertices = vertices.length / RenderUtils.VERTEX_DIM;            /* Creating the buffer we will store vertices in */
            ByteBuffer b = ByteBuffer.allocateDirect(mNumVertices *
                                                     RenderUtils.VERTEX_DIM *
                                                     RenderUtils.GL_FLOAT_SIZE);
            b = b.order(ByteOrder.nativeOrder());
            this.mVertices = b.asFloatBuffer();
            mVertices.put(vertices);
            mVertices.position(0);
            this.mPosX = 0.0f;
            this.mPosY = 0.0f;
            this.mAngle = 0.0f;
            this.mScaleX = 1.0f;
            this.mScaleY = 1.0f;
        }
    }

    /**
     * Returns true iff this renderable is visible by the given camera.
     */
    public abstract boolean isVisible(LayerCamera gameCamera);

    /**
     * Draws the renderable using the given view, model, and projection
     * matrices. Also takes a handle to the final combined MVP matrix.
     * @param mvpMatrixHandle  An openGL handle to the combined MVP matrix.
     * @param texCoordHandle   an openGL handle to the texture coordinate.
     * @param texRGBHandle     An openGL handle to the rgb texture uniform
     * @param texAHandle       An openGL handle to the alpha texture uniform.
     * @param ratio            The screen ratio
     * @param mvpMatrix        The mvpMatrix we will store data in.
     * @param viewMatrix       The view matrix.
     * @param modelMatrix      The model matrix.
     * @param projectionMatrix The projection matrix.
     */
    public abstract void draw(int positionHandle,
                              int mvpMatrixHandle,
                              int texCoordHandle,
                              int texRGBHandle,
                              int texAHandle,
                              float ratio,
                              float[] mvpMatrix,
                              float[] viewMatrix,
                              float[] modelMatrix,
                              float[] projectionMatrix);

    /**
     * Transforms the model matrix based on stored position, rotation, and
     * scaling values.
     */
    protected void updateModelMatrix(float[] modelMatrix) {
        Matrix.setIdentityM(modelMatrix, 0);
        if (FloatMathUtils.FloatsEqual(TRANSLATE_EPSILON, mPosX, 0.0f) == false ||
            FloatMathUtils.FloatsEqual(TRANSLATE_EPSILON, mPosY, 0.0f) == false)
            Matrix.translateM(modelMatrix, 0, -mPosX, mPosY, 0.0f);
        if (FloatMathUtils.FloatsEqual(ANGLE_EPSILON, mAngle, 0.0f) == false)
            Matrix.rotateM(modelMatrix, 0, mAngle, 0.0f, 0.0f, 1.0f);
        if (FloatMathUtils.FloatsEqual(SCALE_EPSILON, mScaleX, 0.0f) == false ||
            FloatMathUtils.FloatsEqual(SCALE_EPSILON, mScaleY, 0.0f) == false)
            Matrix.scaleM(modelMatrix, 0, mScaleX, mScaleY, 1.0f);
        //Log.i("Debug", "C at x=" + mPosX + " y=" + mPosY);
    }

    /**
     * Creates a deep copy of this renderable.
     */
    public abstract Renderable copy();

    public void translate(float x, float y) {
        mPosX = x;
        mPosY = y;
    }

    public void scale(float x, float y) {
        mScaleX = x;
        mScaleY = y;
    }

    public void rotate(float angle) {
        mAngle = angle;
    }

    /*
     * Getters and Setters
     */
    public RenderType getRenderType() {
        return mRenderType;
    }

    public void setTexture(Texture texture) {
        this.mTexture = texture;
    }

    public Texture getTexture() {
        return mTexture;
    }

    /**
     * Returns a new float array with the values from the stored FloatBuffer.
     */
    public float[] getVerticesAsArray() {
        int numIndices = mNumVertices * RenderUtils.VERTEX_DIM;
        float[] vertices = new float[numIndices];
        for (int i = 0; i < numIndices; i++) vertices[i] = mVertices.get(i);
        return vertices;
    }

    protected FloatBuffer getVertices() {
        return mVertices;
    }

    public int getNumVertices() {
        return mNumVertices;
    }

    public float getXPos() {
        return mPosX;
    }

    public float getYPos() {
        return mPosY;
    }

    protected float getXScale() {
        return mScaleX;
    }

    protected float getYScale() {
        return mScaleY;
    }

    public float getAngle() {
        return mAngle;
    }
}
