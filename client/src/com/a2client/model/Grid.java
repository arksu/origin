package com.a2client.model;

import com.a2client.Config;
import com.a2client.util.OpenSimplexNoise;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Terrain.GRID_SIZE;
import static com.a2client.Terrain.TILE_SIZE;

/**
 * 1 грид поверхности игрового мира
 */
public class Grid
{
	private static final Logger _log = LoggerFactory.getLogger(Grid.class.getName());

	private static OpenSimplexNoise noise = new OpenSimplexNoise();

	/**
	 * размер чанка в тайлах
	 */
	public static final int CHUNK_SIZE = 20;

	/**
	 * на сколько кусков делим грид
	 */
	private static final int CHUNKS_COUNT = GRID_SIZE / CHUNK_SIZE;

	/**
	 * типы тайлов
	 */
	public final byte[][] _tiles = new byte[GRID_SIZE][GRID_SIZE];

	/**
	 * высоты тайлов
	 */
	public final float[][] _heights = new float[GRID_SIZE][GRID_SIZE];

	/**
	 * координаты грида в абсолютных мировых координатах (11 точек на тайл)
	 */
	private final Vec2i _gc;

	/**
	 * координаты грида в координатах тайлов
	 */
	private final Vec2i _tc;

	/**
	 * разбиваем весь грид на кусочки, выводим их только если они попадают в область камеры
	 */
	private final GridChunk[][] _chunks = new GridChunk[CHUNKS_COUNT][CHUNKS_COUNT];

	public Grid(Vec2i c, byte[] data)
	{
		_gc = c;
		_tc = _gc.div(TILE_SIZE);
		fillTiles(data);
		fillHeights();
		makeTerrainObjects();
	}

	public void fillChunks(boolean force)
	{
		int bordered = 0;
		for (int x = 0; x < CHUNKS_COUNT; x++)
		{
			for (int y = 0; y < CHUNKS_COUNT; y++)
			{
				// перестроим только пограничные чанки или если явно указано что перестраивать весь грид
				if (force || (_chunks[x][y] != null && _chunks[x][y].isBorder()))
				{
					_chunks[x][y].clear();
					_chunks[x][y] = null;
					bordered++;
				}

				if (_chunks[x][y] == null)
				{
					_chunks[x][y] = new GridChunk(this, x * CHUNK_SIZE, y * CHUNK_SIZE);
				}
			}
		}
//		_log.debug("bordered : " + bordered);
//		_log.debug("fillChunks end");
	}

	public int render(ShaderProgram shaderProgram, Camera camera, boolean isWater)
	{
		int chunksRendered = 0;
		for (GridChunk[] list : _chunks)
		{
			if (list != null)
			{
				for (GridChunk chunk : list)
				{
					if (camera.frustum.boundsInFrustum(chunk.getBoundingBox()))
					{
						if (isWater)
						{
							if (chunk.getWaterMesh() != null)
							{
								chunk.getWaterMesh().render(
										shaderProgram,
										Config._renderTerrainWireframe ? GL20.GL_LINE_STRIP : GL20.GL_TRIANGLES);
							}

						}
						else
						{
							chunk.getMesh().render(
									shaderProgram,
									Config._renderTerrainWireframe ? GL20.GL_LINE_STRIP : GL20.GL_TRIANGLES);
						}
						chunksRendered++;
					}
				}
			}
		}
		return chunksRendered;
	}

	public static Color getTileColor(byte tile)
	{
		switch (tile)
		{
			case 30:
				return new Color(0.5f, 0.8f, 0, 1);
			case 35:
				return new Color(0, 0.8f, 0.1f, 1);
			case 1:
				return new Color(0.2f, 0, 1f, 1);
			case 2:
				return Color.BLUE;
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

	private void fillHeights()
	{
		double div = 5d;
		for (int x = 0; x < GRID_SIZE; x++)
		{
			for (int y = 0; y < GRID_SIZE; y++)
			{
				double tx = _tc.x + x;
				double ty = _tc.y + y;
				float h = ((float) noise.eval(tx / div, ty / div)) * 2.8f;
//				float h = 0;
				_heights[y][x] = h;
			}
		}
	}

	public Vec2i getGC()
	{
		return _gc;
	}

	public Vec2i getTc()
	{
		return _tc;
	}

	public void setData(byte[] data)
	{
		fillTiles(data);
		fillHeights();
	}

	public GridChunk getChunk(int x, int y)
	{
		return _chunks[x][y];
	}

	/**
	 * создать террайн объекты (локальные)
	 */
	public void makeTerrainObjects()
	{
		// TODO makeTerrainObjects
	}

	/**
	 * освободить все ресурсы (удалить локальные объекты террайн)
	 */
	public void release()
	{

	}
}
