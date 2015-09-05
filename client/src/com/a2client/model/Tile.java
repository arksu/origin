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

	public static final int TILE_WATER_LOW = 2;
	public static final int TILE_WATER_DEEP = 1;
	public static final int TILE_HOLE = 5;
	public static final int TILE_SETT = 10;
	public static final int TILE_PLOWED = 20;
	public static final int TILE_FOREST_LEAF = 25;
	public static final int TILE_FOREST_FIR = 30;

	public static final int TILE_GRASS = 35;
	public static final int TILE_SWAMP = 40;
	public static final int TILE_DIRT = 45;

	public static final int TILE_SAND = 50;
	public static final int TILE_HOUSE = 70;
	public static final int TILE_CELLAR = 75;
	public static final int TILE_CAVE = 80;

	public static int getTileIndex(byte tile)
	{
		switch (tile)
		{
			case TILE_WATER_DEEP:
				return 3;
			case TILE_WATER_LOW:
				return 2;
			case TILE_FOREST_LEAF:
				return 0;
			case TILE_FOREST_FIR:
				return 0;
			case TILE_GRASS:
				return 1;

			default:
				return 4;
		}
	}
}
