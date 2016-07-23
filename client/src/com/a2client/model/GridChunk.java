package com.a2client.model;

import com.a2client.MapCache;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.model.Tile.TILE_ATLAS_SIZE;

/**
 * кусочек грида для оптимизации рендера карты
 * Created by arksu on 22.02.15.
 */
public class GridChunk
{
	private static final Logger _log = LoggerFactory.getLogger(GridChunk.class.getName());

	/**
	 * размер чанка в тайлах
	 */
	public static final int CHUNK_SIZE = 10;

	/**
	 * меш
	 */
	private Mesh _mesh;

	/**
	 * массив вершин
	 */
	private float[] _vertex;

	/**
	 * массив индексов
	 */
	private short[] _index;

	/**
	 * границы чанка для определения видимости
	 */
	private BoundingBox _boundingBox;

	int ox, oy;

	private Grid _grid;

	public GridChunk(Grid grid, int gx, int gy)
	{
		_grid = grid;
		_vertex = new float[CHUNK_SIZE * CHUNK_SIZE * 9 * 4];
		_index = new short[CHUNK_SIZE * CHUNK_SIZE * 6];
		_mesh = new Mesh(
				true,
				_vertex.length / 3,
				_index.length,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
		);

		makeMesh(gx, gy);
	}

	protected void makeMesh(int gx, int gy)
	{
		int idx = 0;
		int idv = 0;
		ox = _grid.getTc().x;
		oy = _grid.getTc().y;
		_boundingBox = new BoundingBox(new Vector3(ox + gx, -1, oy + gy),
									   new Vector3(ox + gx + CHUNK_SIZE, 3, oy + gy + CHUNK_SIZE));

		short vertex_count = 0;
		NormalHeight nh;
		for (int x = gx; x < gx + CHUNK_SIZE; x++)
		{
			for (int y = gy; y < gy + CHUNK_SIZE; y++)
			{
				int tx;
				int ty;
				Vector2 uv;

				tx = ox + x;
				ty = oy + y;
//				int h = tx + ty * 3 + x * 5 + y;
//				f = (h % 10) / 40f;
//				f = 0;

				// 0 =====
				nh = new NormalHeight(tx, ty);
				_vertex[idx++] = tx;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				idx += 1; // skip color

				uv = Tile.getTileUV(_grid._tiles[y][x]);
				_vertex[idx++] = uv.x;
				_vertex[idx++] = uv.y;

				// 1 =====
				nh = new NormalHeight(tx + 1, ty);
				_vertex[idx++] = tx + 1;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				idx += 1; // skip color

				_vertex[idx++] = uv.x + TILE_ATLAS_SIZE;
				_vertex[idx++] = uv.y;

				// 2 =====
				nh = new NormalHeight(tx, ty + 1);
				_vertex[idx++] = tx;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty + 1;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				idx += 1; // skip color

				_vertex[idx++] = uv.x;
				_vertex[idx++] = uv.y + TILE_ATLAS_SIZE;

				// 3 =====
				nh = new NormalHeight(tx + 1, ty + 1);
				_vertex[idx++] = tx + 1;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty + 1;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				idx += 1; // skip color

				_vertex[idx++] = uv.x + TILE_ATLAS_SIZE;
				_vertex[idx++] = uv.y + TILE_ATLAS_SIZE;

				//index
				_index[idv++] = vertex_count;
				_index[idv++] = (short) (vertex_count + 3);
				_index[idv++] = (short) (vertex_count + 1);

				_index[idv++] = (short) (vertex_count + 0);
				_index[idv++] = (short) (vertex_count + 2);
				_index[idv++] = (short) (vertex_count + 3);

				vertex_count += 4;

			}
		}
		_mesh.setVertices(_vertex);
		_mesh.setIndices(_index);
	}

	private class NormalHeight
	{
		public float h;
		public Vector3 normal;

		public NormalHeight(int tx, int ty)
		{
			float h1 = MapCache.getTileHeight(tx, ty);
			float h2 = MapCache.getTileHeight(tx - 1, ty - 1);
			float h3 = MapCache.getTileHeight(tx - 1, ty);
			float h4 = MapCache.getTileHeight(tx, ty - 1);

			h1 = Math.max(-10f, h1);
			h2 = Math.max(-10f, h2);
			h3 = Math.max(-10f, h3);
			h4 = Math.max(-10f, h4);

			h = (h1 + h2 + h3 + h4) / 4f;

			Vector3 v1 = new Vector3(h1, 1f, 0f);
			Vector3 v2 = new Vector3(-h2, 1f, 0f);
			Vector3 v3 = new Vector3(0f, 1f, h3);
			Vector3 v4 = new Vector3(0f, 1f, -h4);

			normal = v1.add(v2).add(v3).add(v4);
		}
	}

	public Mesh getMesh()
	{
		return _mesh;
	}

	public BoundingBox getBoundingBox()
	{
		return _boundingBox;
	}

	public void clear()
	{
		_mesh.dispose();
	}
}
