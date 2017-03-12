package com.a2client.model;

import com.a2client.Terrain;
import com.a2client.util.Utils;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Terrain.WATER_LEVEL;
import static com.a2client.model.Grid.CHUNK_SIZE;
import static com.a2client.model.Tile.TILE_ATLAS_SIZE;

/**
 * кусочек грида для оптимизации рендера карты
 * Created by arksu on 22.02.15.
 */
public class GridChunk
{
	private static final Logger _log = LoggerFactory.getLogger(GridChunk.class.getName());

	/**
	 * меш
	 */
	private Mesh _mesh;

	private Mesh _waterMesh;

	/**
	 * границы чанка для определения видимости
	 */
	private BoundingBox _boundingBox;

	/**
	 * отступ данного грида в тайлах
	 */
	private int _gx, _gy;

	/**
	 * отступ чанка внутри грида
	 */
	private int _cx, _cy;

	/**
	 * грид в котором создаем чанк
	 */
	private Grid _grid;

	/**
	 * этот чанк на границе мира? есть тайлы с неопределенной высотой...
	 */
	private boolean _isBorder = false;

	/**
	 * добавить отображение нормалей в меш, их видно в wireframe режиме
	 */
	private static final boolean SHOW_NORMALS = false;

	private float _maxHeight;
	private float _minHeight;

	// 8 = 3 coord + 3 normal + 2 uv; 4 = вершины для тайла
	private static int VERTEX_SIZE = 8;
	private static int VERTEX_COUNT = SHOW_NORMALS ? 8 : 4;

	private float[][] _vertex = new float[CHUNK_SIZE + 3][CHUNK_SIZE + 3];
	private Vector3[][] _normal = new Vector3[CHUNK_SIZE + 3][CHUNK_SIZE + 3];

	public GridChunk(Grid grid, int cx, int cy)
	{
		_grid = grid;

		_cx = cx;
		_cy = cy;

		// отступ данного грида в тайлах
		_gx = _grid.getTc().x;
		_gy = _grid.getTc().y;

		_waterMesh = null;

		fillVertex();
		fillNormals();
		makeMesh();

		// определим нужна ли вода в этом чанке?
		// хоть одна вершина ниже уровня воды?
		if (_minHeight < WATER_LEVEL)
		{
			makeWater();
		}

		_boundingBox = new BoundingBox(
				new Vector3(_gx + _cx, _minHeight, _gy + _cy),
				new Vector3(_gx + _cx + CHUNK_SIZE, _maxHeight, _gy + _cy + CHUNK_SIZE));

	}

	private void makeMesh()
	{
		float[] vertices;

		short[] index;

		// 8 = 3 coord + 3 normal + 2 uv; 4 = вершины для тайла
		vertices = new float[CHUNK_SIZE * CHUNK_SIZE * VERTEX_SIZE * VERTEX_COUNT];
		// 6 = 3 + 3 два треугольника
		index = new short[CHUNK_SIZE * CHUNK_SIZE * (SHOW_NORMALS ? 18 : 6)];

		_mesh = new Mesh(
				true,
				vertices.length / VERTEX_SIZE,
				index.length,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
		);
		int idx = 0;
		int idv = 0;
		Vector2 uv;
		int tx, ty;
		Float h, hX, hY, hXY;
		short vertex_count = 0;
		Vector3 normal0, normal1, normal2, normal3;

		for (int x = 0; x < CHUNK_SIZE; x++)
		{
			for (int y = 0; y < CHUNK_SIZE; y++)
			{
				// абсолютные координаты тайла
				tx = _cx + _gx + x;
				ty = _cy + _gy + y;
				uv = Tile.getTileUV(_grid._tiles[_cy + y][_cx + x]);

				// 0
				h = _vertex[x + 1][y + 1];
				vertices[idx++] = tx;
				vertices[idx++] = h;
				vertices[idx++] = ty;

				normal0 = _normal[x + 1][y + 1].cpy();
				vertices[idx++] = normal0.x;
				vertices[idx++] = normal0.y;
				vertices[idx++] = normal0.z;

				vertices[idx++] = uv.x;
				vertices[idx++] = uv.y;

				// 1
				hX = _vertex[x + 2][y + 1];
				vertices[idx++] = tx + 1;
				vertices[idx++] = hX;
				vertices[idx++] = ty;

				normal1 = _normal[x + 2][y + 1].cpy();
				vertices[idx++] = normal1.x;
				vertices[idx++] = normal1.y;
				vertices[idx++] = normal1.z;

				vertices[idx++] = uv.x + TILE_ATLAS_SIZE;
				vertices[idx++] = uv.y;

				// 2
				hY = _vertex[x + 1][y + 2];
				vertices[idx++] = tx;
				vertices[idx++] = hY;
				vertices[idx++] = ty + 1;

				normal2 = _normal[x + 1][y + 2].cpy();
				vertices[idx++] = normal2.x;
				vertices[idx++] = normal2.y;
				vertices[idx++] = normal2.z;

				vertices[idx++] = uv.x;
				vertices[idx++] = uv.y + TILE_ATLAS_SIZE;

				// 3
				hXY = _vertex[x + 2][y + 2];
				vertices[idx++] = tx + 1;
				vertices[idx++] = hXY;
				vertices[idx++] = ty + 1;

				normal3 = _normal[x + 2][y + 2].cpy();
				vertices[idx++] = normal3.x;
				vertices[idx++] = normal3.y;
				vertices[idx++] = normal3.z;

				vertices[idx++] = uv.x + TILE_ATLAS_SIZE;
				vertices[idx++] = uv.y + TILE_ATLAS_SIZE;

				// normals
				if (SHOW_NORMALS)
				{
					float s = 0.5f;
					normal0.scl(s);
					normal1.scl(s);
					normal2.scl(s);
					normal3.scl(s);
					vertices[idx++] = tx + normal0.x;
					vertices[idx++] = h + normal0.y;
					vertices[idx++] = ty + normal0.z;
					vertices[idx++] = 0;
					vertices[idx++] = 1;
					vertices[idx++] = 0;
					vertices[idx++] = 0;
					vertices[idx++] = 0;

					vertices[idx++] = tx + 1 + normal1.x;
					vertices[idx++] = hX + normal1.y;
					vertices[idx++] = ty + normal1.z;
					vertices[idx++] = 0;
					vertices[idx++] = 1;
					vertices[idx++] = 0;
					vertices[idx++] = 0;
					vertices[idx++] = 0;

					vertices[idx++] = tx + normal2.x;
					vertices[idx++] = hY + normal2.y;
					vertices[idx++] = ty + 1 + normal2.z;
					vertices[idx++] = 0;
					vertices[idx++] = 1;
					vertices[idx++] = 0;
					vertices[idx++] = 0;
					vertices[idx++] = 0;

					vertices[idx++] = tx + 1 + normal3.x;
					vertices[idx++] = hXY + normal3.y;
					vertices[idx++] = ty + 1 + normal3.z;
					vertices[idx++] = 0;
					vertices[idx++] = 1;
					vertices[idx++] = 0;
					vertices[idx++] = 0;
					vertices[idx++] = 0;
				}

				// indices
				float rightDelta = Math.abs(h - hXY);
				float leftDelta = Math.abs(hX - hY);

				_maxHeight = Utils.max(_maxHeight, h, hX, hY, hXY);
				_minHeight = Utils.min(_minHeight, h, hX, hY, hXY);

				//index
				if (rightDelta < leftDelta)
				{
					index[idv++] = vertex_count;
					index[idv++] = (short) (vertex_count + 3);
					index[idv++] = (short) (vertex_count + 1);

					index[idv++] = vertex_count;
					index[idv++] = (short) (vertex_count + 2);
					index[idv++] = (short) (vertex_count + 3);
				}
				else
				{
					index[idv++] = vertex_count;
					index[idv++] = (short) (vertex_count + 2);
					index[idv++] = (short) (vertex_count + 1);

					index[idv++] = (short) (vertex_count + 1);
					index[idv++] = (short) (vertex_count + 2);
					index[idv++] = (short) (vertex_count + 3);
				}

				if (SHOW_NORMALS)
				{
					index[idv++] = (short) (vertex_count + 4);
					index[idv++] = vertex_count;
					index[idv++] = vertex_count;

					index[idv++] = (short) (vertex_count + 5);
					index[idv++] = (short) (vertex_count + 1);
					index[idv++] = (short) (vertex_count + 1);

					index[idv++] = (short) (vertex_count + 6);
					index[idv++] = (short) (vertex_count + 2);
					index[idv++] = (short) (vertex_count + 2);

					index[idv++] = (short) (vertex_count + 7);
					index[idv++] = (short) (vertex_count + 3);
					index[idv++] = (short) (vertex_count + 3);
				}

				vertex_count += VERTEX_COUNT;
			}
		}
		_mesh.setVertices(vertices);
		_mesh.setIndices(index);
	}

	private void fillVertex()
	{
		_maxHeight = 0;
		_minHeight = WATER_LEVEL;
		for (int x = -1; x <= CHUNK_SIZE + 1; x++)
		{
			for (int y = -1; y <= CHUNK_SIZE + 1; y++)
			{
				int tx;
				int ty;

				// абсолютные координаты тайла
				tx = _cx + _gx + x;
				ty = _cy + _gy + y;

				HeightAverage height = new HeightAverage(tx, ty, this);
				_vertex[x + 1][y + 1] = height.h;
				if (x >= 0 && x <= CHUNK_SIZE && y >= 0 && y <= CHUNK_SIZE)
				{
					_isBorder = _isBorder || (height.isBorder);
					_maxHeight = Utils.max(_maxHeight, height.h);
					_minHeight = Utils.min(_minHeight, height.h);
				}
			}
		}
	}

	private void fillNormals()
	{
		for (int x = 0; x <= CHUNK_SIZE; x++)
		{
			for (int y = 0; y <= CHUNK_SIZE; y++)
			{
				float hL = _vertex[x][y + 1];
				float hR = _vertex[x + 2][y + 1];
				float hU = _vertex[x + 1][y];
				float hD = _vertex[x + 1][y + 2];
				_normal[x + 1][y + 1] = new Vector3(hL - hR, 2f, hU - hD).nor();
			}
		}
	}

	private void makeWater()
	{
		final float x = _gx + _cx;
		final float y = _gy + _cy;

		_waterMesh = new Mesh(
				true,
				4,
				6,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
		);

		float[] vertices = new float[]{
				x, WATER_LEVEL, y, 0, 1, 0, 0, 0,
				x + CHUNK_SIZE, WATER_LEVEL, y, 0, 1, 0, 1, 0,
				x, WATER_LEVEL, y + CHUNK_SIZE, 0, 1, 0, 0, 1,
				x + CHUNK_SIZE, WATER_LEVEL, y + CHUNK_SIZE, 0, 1, 0, 1, 1

		};

		short[] indices = new short[]{
				0, 2, 1,
				1, 2, 3
		};
		_waterMesh.setVertices(vertices);
		_waterMesh.setIndices(indices);

	}

	public static class HeightAverage
	{
		public final float h;
		private final boolean isBorder;

		public HeightAverage(int tx, int ty, GridChunk chunk)
		{
			// получаем высоты соседних тайлов
			float h1 = chunk.getHeight(tx, ty); // right
			float h2 = chunk.getHeight(tx - 1, ty - 1); // left
			float h3 = chunk.getHeight(tx - 1, ty);// down
			float h4 = chunk.getHeight(tx, ty - 1);// up

			// если хоть один из соседних тайлов не определен поставим признак того что это граница чанка
			isBorder = h1 <= Terrain.FAKE_HEIGHT || h2 <= Terrain.FAKE_HEIGHT || h3 <= Terrain.FAKE_HEIGHT || h4 <= Terrain.FAKE_HEIGHT;

			// выставим тайлам которых нет
			h1 = h1 <= Terrain.FAKE_HEIGHT ? 0f : h1;
			h2 = h2 <= Terrain.FAKE_HEIGHT ? 0f : h2;
			h3 = h3 <= Terrain.FAKE_HEIGHT ? 0f : h3;
			h4 = h4 <= Terrain.FAKE_HEIGHT ? 0f : h4;

			// посчитаем среднюю высоту вершины по четырем соседним тайлам
			h = (h1 + h2 + h3 + h4) / 4f;

		}
	}

	/**
	 * получить высоту указанного тайла
	 */
	public float getHeight(int tx, int ty)
	{
		int x = tx - _gx;
		int y = ty - _gy;
		if (x >= 0 && x < Terrain.GRID_SIZE && y >= 0 && y < Terrain.GRID_SIZE)
		{
			return _grid._heights[y][x];
		}
		return Terrain.getTileHeight(tx, ty);
	}

	public Mesh getMesh()
	{
		return _mesh;
	}

	public Mesh getWaterMesh()
	{
		return _waterMesh;
	}

	public boolean isHaveWater()
	{
		return _waterMesh != null;
	}

	public BoundingBox getBoundingBox()
	{
		return _boundingBox;
	}

	public void clear()
	{
		_vertex = null;
		_normal = null;
		_boundingBox = null;
		_mesh.dispose();
		if (_waterMesh != null)
		{
			_waterMesh.dispose();
		}
	}

	public boolean isBorder()
	{
		return _isBorder;
	}
}
