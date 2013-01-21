package com.jlreyes.libraries.android_game_engine.utils.math;

/**
 * Standard math matrix.
 *
 * @param <E> The type of element stored in this matrix.
 * @author jlreyes
 */
public class MathMatrix<E> {
    /* Row, Col format */
    public Object[][] mMatrix;

    public MathMatrix(int rows, int cols) {        /* Make sure rows and cols are valid */
        if (rows <= 0 || cols <= 0)
            throw new RuntimeException("Cannot create a matrix of size " +
                                       rows + "x" + cols);
        mMatrix = new Object[rows][];
        for (int i = 0; i < rows; i++)
            mMatrix[i] = new Object[cols];
    }

    public void set(int row, int col, E e) {
        checkBounds(row, col);
        mMatrix[row][col] = (Object) e;
    }

    public E get(int row, int col) {
        checkBounds(row, col);
        @SuppressWarnings("unchecked")
        E e = (E) mMatrix[row][col];
        return e;
    }

    public void checkBounds(int row, int col) {
        if (isIn(row, col) == false)
            throw new RuntimeException("(" + row + ", " + col + ") is not a" +
                                       " valid (row, col) index in matrix " +
                                       this.toString() + " with dimensions " +
                                       getNumRows() + "x" + getNumCols());
    }

    public boolean isIn(int row, int col) {
        return (0 <= row && row < getNumRows())
               && (0 <= col && col < getNumCols());
    }

    /*
     * Getters and setters
     */
    public int getNumRows() {
        return mMatrix.length;
    }

    public int getNumCols() {
        return mMatrix[0].length;
    }
}
