package com.a2client;

import com.a2client.model.Grid;
import com.a2client.util.Vec2i;

import java.util.LinkedList;
import java.util.List;

public class MapCache
{
	// сколько единиц координат в одном тайле
	public static final int TILE_SIZE = 12;
	// размер одного грида в тайлах
	public static final int GRID_SIZE = 100;
	public static final int GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE;
	// размер одного грида в байтах для передачи по сети
	public static final int GRID_SIZE_BYTES = GRID_SIZE * GRID_SIZE * 2;

	public static List<Grid> grids = new LinkedList<Grid>();

	/**
	 * удалить гриды за пределами активности игрока (область 3 на 3)
	 * @param px координаты игрока
	 * @param py координаты игрока
	 */
	public static void removeOutsideGrids(int px, int py)
	{
		int i = 0;
		while (i < MapCache.grids.size())
		{
			// если грид за границами 9 гридов
			if (!gridInside(grids.get(i).getGC(), px, py))
			{
				// удалим
				grids.get(i).release();
				grids.remove(i);
			}
			else
			{
				i++;
			}
		}
	}

	public static boolean gridInside(Vec2i gc, int px, int py)
	{
		return (gc.x >= px - GRID_FULL_SIZE - GRID_FULL_SIZE && gc.x < px + GRID_FULL_SIZE &&
				gc.y >= py - GRID_FULL_SIZE - GRID_FULL_SIZE && gc.y < py + GRID_FULL_SIZE);
	}

	public static void clear()
	{
		grids.clear();
	}

}
