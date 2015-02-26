package com.a4server.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 04.01.2015.
 */
public class Tile
{
    private static final Logger _log = LoggerFactory.getLogger(Tile.class.getName());

    public static enum TileType
    {
        TILE_UNKNOWN(0),
        TILE_WATER_LOW(2),
        TILE_WATER_DEEP(1),
        TILE_HOLE(5),
        TILE_SETT(10),
        TILE_PLOWED(20),
        TILE_FOREST_LEAF(25),
        TILE_FOREST_FIR(30),

        TILE_GRASS(35),
        TILE_SWAMP(40),
        TILE_DIRT(45),

        TILE_SAND(50),
        TILE_HOUSE(70),
        TILE_CELLAR(75),
        TILE_CAVE(80);

        private final int _code;

        TileType(int code)
        {
            _code = code;
        }

        public final int getCode()
        {
            return _code;
        }
    }

    public final TileType getType(int i)
    {
        // почему именно switch?
        // это самый быстрый способ получить результат
        // остальные компактнее по коду но проигрывают в скорости
        switch (i)
        {
            case 0:
                return TileType.TILE_UNKNOWN;
            case 1:
                return TileType.TILE_WATER_DEEP;
            case 2:
                return TileType.TILE_WATER_LOW;
            case 5:
                return TileType.TILE_HOLE;
            case 10:
                return TileType.TILE_SETT;
            case 20:
                return TileType.TILE_PLOWED;
            case 25:
                return TileType.TILE_FOREST_LEAF;
            case 30:
                return TileType.TILE_FOREST_FIR;
            case 35:
                return TileType.TILE_GRASS;
            case 40:
                return TileType.TILE_SWAMP;
            case 45:
                return TileType.TILE_DIRT;
            case 50:
                return TileType.TILE_SAND;
            case 70:
                return TileType.TILE_HOUSE;
            case 75:
                return TileType.TILE_CELLAR;
            case 80:
                return TileType.TILE_CAVE;
            default:
                return TileType.TILE_UNKNOWN;
        }
    }

    private TileType _type;

    public Tile(int t) {
        _type = getType(t);
    }
}
