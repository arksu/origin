package com.a2client;

import com.a2client.model.Grid;
import com.a2client.util.Vec2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class MapCache
{
	private static final Logger _log = LoggerFactory.getLogger(MapCache.class.getName());

	// сколько единиц координат в одном тайле
	public static final int TILE_SIZE = 12;
	// размер одного грида в тайлах
	public static final int GRID_SIZE = 100;
	public static final int GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE;
	// размер одного грида в байтах для передачи по сети
	public static final int GRID_SIZE_BYTES = GRID_SIZE * GRID_SIZE * 2;

	public static List<Grid> grids = new LinkedList<>();

	public static final float FAKE_HEIGHT = -100000f;

	public static void addGrid(Grid grid)
	{
		grids.add(grid);
		long timeMillis = System.currentTimeMillis();
//		_log.debug("add grid");
		for (Grid grid1 : grids)
		{
			grid1.fillChunks(false);
		}
//		grid.fillChunks();
		_log.debug("grid " + grid.getTc() + " added in " + (System.currentTimeMillis() - timeMillis) + " ms");
	}

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

	public static Grid getGrid(int tx, int ty)
	{
		for (Grid grid : grids)
		{
			Vec2i tc = grid.getTc();
			if (tx >= tc.x && tx < tc.x + GRID_SIZE
				&& ty >= tc.y && ty < tc.y + GRID_SIZE)
			{
				return grid;
			}
		}
		return null;
	}

	public static float getTileHeight(int tx, int ty)
	{
		Grid grid = getGrid(tx, ty);
		if (grid != null)
		{
			Vec2i tc = new Vec2i(tx, ty).sub(grid.getTc());
			return grid._heights[tc.y][tc.x];
		}
		return FAKE_HEIGHT;
	}

}
