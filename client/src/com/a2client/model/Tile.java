package com.a2client.model;

import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Работа с тайалами
 * Created by arksu on 22.02.15.
 */
public class Tile
{
    private static final Logger _log = LoggerFactory.getLogger(Tile.class.getName());

    public static final int ATLAS_WIDTH = 4;
    public static float TILE_ATLAS_SIZE = 1f / ATLAS_WIDTH;

    public static Vector2 getTileUV(byte tile)
    {
        int idx = getTileIndex(tile);
        int x = idx % ATLAS_WIDTH;
        int y = idx / ATLAS_WIDTH;

        return new Vector2(x * TILE_ATLAS_SIZE, y * TILE_ATLAS_SIZE);
    }

    public static int getTileIndex(byte tile)
    {
        switch (tile)
        {
            case 1:
                return 2;
            case 2:
                return 3;
            case 30:
                return 0;
            case 35:
                return 1;
            default:
                return 4;
        }
    }
}
