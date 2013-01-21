package com.jlreyes.libraries.android_game_engine.rendering;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class RenderUtils {
    public static final int VERTEX_DIM = 2;
    public static final int GL_FLOAT_SIZE = 4;

    private static final String TAG = "RenderUtils";

    /* Fragment Shader */
    public static final String FRAGMENT_SHADER = "precision mediump float;\n" +
                                                 "\n" +
                                                 "uniform sampler2D u_RGBTexture;\n" +
                                                 "uniform sampler2D u_ATexture;\n" +
                                                 "\n" +
                                                 "varying vec2 v_TexCoordinate;\n" +
                                                 "\n" +
                                                 "void main() {\n" +
                                                 "     vec4 rgbTexel = texture2D(u_RGBTexture, v_TexCoordinate);\n" +
                                                 "     vec4 aTexel = texture2D(u_ATexture, v_TexCoordinate);\n" +
                                                 "     vec4 texel = vec4(rgbTexel.rgb, aTexel.a);\n" +
                                                 "     gl_FragColor = texel;\n" +
                                                 "}";

    /* Vertex Shader */
    public static final String VERTEX_SHADER = "uniform mat4 u_MVPMatrix;\n" +
                                               "\n" +
                                               "attribute vec2 a_Position;\n" +
                                               "attribute vec2 a_TexCoordinate;\n" +
                                               "\n" +
                                               "varying vec2 v_TexCoordinate;\n" +
                                               "\n" +
                                               "void main() {\n" +
                                               "    v_TexCoordinate = vec2(a_TexCoordinate.x, 1.0 - a_TexCoordinate.y);\n" +
                                               "    vec4 position = vec4(0.0 - a_Position.x, a_Position.y, 0.0, 1.0);\n" +
                                               "    gl_Position = u_MVPMatrix * position;\n" +
                                               "}";

    /**
     * Given a view, model, and projection matrix, performs matrix multiplication
     * in the correct order and returns a combined matrix to render.
     *
     * @param viewMatrix       The view matrix.
     * @param modelMatrix      The model matrix.
     * @param projectionMatrix The projection matrix.
     */
    public static void CreateMVPMatrix(float[] mvpMatrix,
                                       float[] viewMatrix,
                                       float[] modelMatrix,
                                       float[] projectionMatrix) {
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);
    }


    /**
     * Helper function to compile a shader.
     *
     * @param shaderType   The shader type.
     * @param shaderSource The shader source code.
     * @return An OpenGL handle to the shader.
     */
    public static int compileShader(final int shaderType, final String shaderSource) {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle,
                                 GLES20.GL_COMPILE_STATUS,
                                 compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " +
                           GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle   An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes           Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }
}
