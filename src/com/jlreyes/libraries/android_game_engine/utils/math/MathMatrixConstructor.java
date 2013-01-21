package com.jlreyes.libraries.android_game_engine.utils.math;

import java.util.ArrayList;

public class MathMatrixConstructor<E> {
    private ArrayList<ArrayList<Object>> mMatrix;
    private int mRows;
    private int mCols;

    public MathMatrixConstructor() {
        this.mMatrix = new ArrayList<ArrayList<Object>>();
        this.mRows = 0;
        this.mCols = 0;
    }

    public void addRow() {
        mRows += 1;
        mMatrix.add(new ArrayList<Object>());
        for (int i = 0; i < mCols; i++)
            mMatrix.get(mRows - 1).add(null);
    }

    public void addColumn() {
        mCols += 1;
        for (ArrayList<Object> row : mMatrix) row.add(null);
    }

    public void set(int row, int col, E e) {
        checkBounds(row, col);
        mMatrix.get(row).set(col, (Object) e);
    }

    public void checkBounds(int row, int col) {
        if (isIn(row, col) == false)
            throw new RuntimeException("(" + row + ", " + col + ") is not a" +
                                       " valid (row, col) index in matrix " +
                                       this.toString() + " with dimensions " +
                                       mRows + "x" + mCols);
    }

    public boolean isIn(int row, int col) {
        return (0 <= row && row < mRows) && (0 <= col && col <= mCols);
    }

    public MathMatrix<E> finish() {
        int rows = getRows();
        int cols = getCols();
        MathMatrix<E> mathMatrix = new MathMatrix<E>(rows, cols);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                @SuppressWarnings("unchecked")
                E e = (E) mMatrix.get(row).get(col);
                mathMatrix.set(row, col, e);
            }
        }
        return mathMatrix;
    }

	/*
     * Getters and Setters
	 */

    public int getRows() {
        return mRows;
    }

    public int getCols() {
        return mCols;
    }
}
