package com.a2client.model;

import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Color;

import static com.a2client.MapCache.*;

public class Grid
{
    public byte[][] _tiles;
    /**
     * координаты грида
     */
    private Vec2i _gc;

    public Grid(Vec2i c, byte[] data)
    {
        this._gc = c;
        _tiles = new byte[GRID_SIZE][GRID_SIZE];
        fillTiles(data);
        makeTerrainObjects();
        //        save_debug();
    }

    public static Color getTileColor(byte tile)
    {
        switch (tile)
        {
            case 30:
                return new Color(0.5f, 1, 0, 1);
            case 1:
                return new Color(0.2f, 0, 1f, 1);
            case 2:
                return Color.BLUE;
            case 35:
                return Color.GREEN;
            default:
                return Color.WHITE;
        }
    }

    private void fillTiles(byte[] data)
    {
        for (int i = 0; i < GRID_SIZE; i++)
        {
            System.arraycopy(data, i * GRID_SIZE, _tiles[i], 0, GRID_SIZE);
        }
    }

    public Vec2i getGC()
    {
        return _gc;
    }

    public void setData(byte[] data)
    {
        fillTiles(data);
    }

    /**
     * создать террайн объекты (локальные)
     */
    public void makeTerrainObjects()
    {

    }

    /**
     * освободить все ресурсы (удалить локальные объекты террайн)
     */
    public void release()
    {

    }
}
