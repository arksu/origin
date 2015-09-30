package com.a2client.model;

import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import static com.a2client.MapCache.GRID_SIZE;

public class Grid
{
	public byte[][] _tiles;
	/**
	 * координаты грида
	 */
	private Vec2i _gc;

	private GridChunk[] _chunks;

	public Grid(Vec2i c, byte[] data)
	{
		this._gc = c;
		_tiles = new byte[GRID_SIZE][GRID_SIZE];
		fillTiles(data);
		makeTerrainObjects();
		fillChunks();
	}

	public void fillChunks()
	{
		int chunksCount = GRID_SIZE / GridChunk.CHUNK_SIZE;
		_chunks = new GridChunk[chunksCount * chunksCount];
		for (int x = 0; x < chunksCount; x++)
		{
			for (int y = 0; y < chunksCount; y++)
			{
				_chunks[x + y * chunksCount] = new GridChunk(this, x * GridChunk.CHUNK_SIZE, y * GridChunk.CHUNK_SIZE);
			}
		}
	}

	public int render(ShaderProgram shaderProgram, Camera camera)
	{
		int result = 0;
		for (GridChunk c : _chunks)
		{
			if (camera.frustum.boundsInFrustum(c.getBoundingBox()))
			{
				c.getMesh().render(shaderProgram, GL20.GL_TRIANGLES);
				result++;
			}
		}
		return result;
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
