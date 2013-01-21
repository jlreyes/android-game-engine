package com.jlreyes.libraries.android_game_engine.scenes;

import com.jlreyes.libraries.android_game_engine.datastructures.MinAllocHashSet;
import com.jlreyes.libraries.android_game_engine.rendering.renderable.Renderable;
import com.jlreyes.libraries.android_game_engine.sprites.Sprite;
import com.jlreyes.libraries.android_game_engine.sprites.SpriteWrapper;
import com.jlreyes.libraries.android_game_engine.sprites.sprites.LayerCamera;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Grid that handles culling of immovable sprites. Each layer can have a cull
 * grid. Cull grid's are grids of cells.
 *
 * TODO GRIDS
 * @author jlreyes
 */
public class CullGrid {
    public static final String TAG = "CullGrid";
    public static final int DEFAULT_CELL_WIDTH = 5;
    public static final int DEFAULT_CELL_HEIGHT = 5;

    public static class Cell {
        private CellCoord mCellCoord;
        private HashSet<Sprite> mContent;

        public Cell(CellCoord coord) {
            this.mCellCoord = coord;
            this.mContent = new HashSet<Sprite>();
        }

        public void addSprite(Sprite sprite) {
            mContent.add(sprite);
        }

        public void removeSprite(Sprite sprite) {
            mContent.remove(sprite);
        }

        public boolean containsSprite(Sprite sprite) {
            return mContent.contains(sprite);
        }

        public CellCoord getCellCoord() {
            return mCellCoord;
        }

    }

    /**
     * A Cell Coord is the bottom-left coordinate of the cell. A cell has a
     * domain [x, cellWidth) and range [0, cellHeight)
     *
     * @author jlreyes
     */
    public static class CellCoord {
        public int x;
        public int y;

        public CellCoord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) return true;
            if ((object instanceof CellCoord) == false) return false;
            CellCoord coord = (CellCoord) object;
            if (coord.x == this.x && coord.y == this.y) return true;
            else return false;
        }
    }

    private HashMap<CellCoord, Cell> mGrid;
    private HashSet<Sprite> mSprites;
    private MinAllocHashSet<Sprite> mVisibleSprites;
    private int mCellWidth;
    private int mCellHeight;
    /**
     * Cell coordinate that the result of {@link #gridCellCoordFromWorldCellCoord(float, float)}
     * is stored in. Pre-allocated to prevent runtime allocation.
     */
    private CellCoord mCellCoordFromWorldCoordResult;

    /**
     * Same as CullGrid({@link #DEFAULT_CELL_WIDTH}, {@link #DEFAULT_CELL_HEIGHT})
     */
    public CullGrid() {
        this(DEFAULT_CELL_WIDTH, DEFAULT_CELL_HEIGHT);
    }

    public CullGrid(int cellWidth, int cellHeight) {
        this.mGrid = new HashMap<CellCoord, Cell>();
        this.mSprites = new HashSet<Sprite>();
        this.mVisibleSprites = new MinAllocHashSet<Sprite>();
        this.mCellWidth = cellWidth;
        this.mCellHeight = cellHeight;
        this.mCellCoordFromWorldCoordResult = new CellCoord(0, 0);
    }

    @SuppressWarnings("UnusedDeclaration")
    public MinAllocHashSet<Sprite> calculateVisibleSprites(LayerCamera gameCamera) {
        mVisibleSprites.clear();
        /* Getting the literal coordiantes of the game camera */
        float leftX = gameCamera.getLeftX();
        float rightX = gameCamera.getRightX();
        float bottomY = gameCamera.getBottomY();
        float topY = gameCamera.getTopY();
        /* Get the Corner cells */
        gridCellCoordFromWorldCellCoord(leftX, topY);
        Cell topLeft = mGrid.get(mCellCoordFromWorldCoordResult);
        gridCellCoordFromWorldCellCoord(rightX, topY);
        Cell topRight = mGrid.get(mCellCoordFromWorldCoordResult);
        gridCellCoordFromWorldCellCoord(rightX, bottomY);
        Cell bottomRight = mGrid.get(mCellCoordFromWorldCoordResult);
        gridCellCoordFromWorldCellCoord(leftX, bottomY);
        Cell bottomLeft = mGrid.get(mCellCoordFromWorldCoordResult);

        // TODO Add cells between these four cells

        return mVisibleSprites;
    }

    /**
     * Modifies {@link #mCellCoordFromWorldCoordResult}. Stores the cell
     */
    private void gridCellCoordFromWorldCellCoord(float x, float y) {
        int cellX = mCellWidth * (int) (x / (float) mCellWidth);
        int cellY = mCellHeight * (int) (y / (float) mCellHeight);
        mCellCoordFromWorldCoordResult.x = cellX;
        mCellCoordFromWorldCoordResult.y = cellY;
    }

    /**
     * Add the given sprite to the CullGrid using the given renderable to
     * determine the cells the sprite is contained in.
     */
    @SuppressWarnings({"UnusedParameters", "UnnecessaryReturnStatement"})
    public void addSprite(Sprite sprite, Renderable renderable) {
        /* The sprites in a sprite wrapper will add themselves */
        if (sprite instanceof SpriteWrapper) return;
        //TODO
    }

    public void removeSprite(Sprite sprite) {
        if (sprite instanceof SpriteWrapper) return;
        for (CellCoord coord : mGrid.keySet())
            mGrid.get(coord).removeSprite(sprite);
        mSprites.remove(sprite);
    }

}
