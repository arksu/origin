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
	 * массив вершин
	 */
	private final float[] _vertex;

	/**
	 * массив индексов
	 */
	private final short[] _index;

	/**
	 * границы чанка для определения видимости
	 */
	private BoundingBox _boundingBox;

	/**
	 * отступ данного грида в тайлах
	 */
	int _gx, _gy;

	/**
	 * отступ чанка внутри грида
	 */
	int _cx, _cy;

	/**
	 * грид в котором создаем чанк
	 */
	private Grid _grid;

	/**
	 * этот чанк на границе мира? есть тайлы с неопределенной высотой...
	 */
	private boolean _isBorder = false;

	private final NormalHeight[][] _heights = new NormalHeight[CHUNK_SIZE + 2][CHUNK_SIZE + 2];

	private float _maxHeight;
	private float _minHeight;

	public GridChunk(Grid grid, int gx, int gy)
	{
		_grid = grid;
		_vertex = new float[CHUNK_SIZE * CHUNK_SIZE * 8 * 4];
		_index = new short[CHUNK_SIZE * CHUNK_SIZE * 6];
		_cx = gx;
		_cy = gy;
		_waterMesh = null;
		makeMesh();
	}

	/**
	 * получить нормаль и высоту вершины
	 * @param tx, ty от 0 до CHUNK_SIZE+2, 0 это -1 от начала чанка. и до +1 длины чанка. 2 лишних вершины для избыточности
	 */
	public NormalHeight getNormalHeight(int tx, int ty)
	{
//		_log.debug("tx="+tx+" ty="+ty);
		NormalHeight normalHeight = _heights[tx - _gx - _cx + 1][ty - _gy - _cy + 1];
		if (normalHeight != null)
		{
			return normalHeight;
		}
		normalHeight = new NormalHeight(tx, ty, this);
		_heights[tx - _gx - _cx + 1][ty - _gy - _cy + 1] = normalHeight;
		return normalHeight;
	}

	protected void makeMesh()
	{
		_mesh = new Mesh(
				true,
				_vertex.length / 8,
				_index.length,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
		);

		int idx = 0;
		int idv = 0;

		// отступ данного грида в тайлах
		_gx = _grid.getTc().x;
		_gy = _grid.getTc().y;

		short vertex_count = 0;
		NormalHeight nh;

		// _isBorder запомним навсегда! т.к. это явно граничный чанк в гриде

//		for (int x = _cx - 1; x <= _cx + CHUNK_SIZE; x++)
//		{
//			getNormalHeight(_gx + x, _gy + _cy - 1);
//			getNormalHeight(_gx + x, _gy + _cy + CHUNK_SIZE);
//		}
//		for (int y = _cy - 1; y <= _cy + CHUNK_SIZE; y++)
//		{
//			getNormalHeight(_gx + _cx - 1, _gy + y);
//			getNormalHeight(_gx + _cx + CHUNK_SIZE, _gy + y);
//		}

		_maxHeight = 0;
		_minHeight = WATER_LEVEL;

		for (int x = _cx; x < _cx + CHUNK_SIZE; x++)
		{
			for (int y = _cy; y < _cy + CHUNK_SIZE; y++)
			{
				int tx;
				int ty;
				Vector2 uv;

				Float h, hX, hY, hXY;

				// абсолютные координаты тайла
				tx = _gx + x;
				ty = _gy + y;

				// 0 =====
				nh = getNormalHeight(tx, ty);
				_isBorder = _isBorder || nh.isBorder;
				_vertex[idx++] = tx;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty;
				h = nh.h;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				uv = Tile.getTileUV(_grid._tiles[y][x]);
				_vertex[idx++] = uv.x;
				_vertex[idx++] = uv.y;

				// 1 =====
				nh = getNormalHeight(tx + 1, ty);
				_isBorder = _isBorder || nh.isBorder;
				_vertex[idx++] = tx + 1;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty;
				hX = nh.h;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				_vertex[idx++] = uv.x + TILE_ATLAS_SIZE;
				_vertex[idx++] = uv.y;

				// 2 =====
				nh = getNormalHeight(tx, ty + 1);
				_isBorder = _isBorder || nh.isBorder;
				_vertex[idx++] = tx;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty + 1;
				hY = nh.h;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				_vertex[idx++] = uv.x;
				_vertex[idx++] = uv.y + TILE_ATLAS_SIZE;

				// 3 =====
				nh = getNormalHeight(tx + 1, ty + 1);
				_isBorder = _isBorder || nh.isBorder;
				_vertex[idx++] = tx + 1;
				_vertex[idx++] = nh.h;
				_vertex[idx++] = ty + 1;
				hXY = nh.h;

				// normal
				_vertex[idx++] = nh.normal.x;
				_vertex[idx++] = nh.normal.y;
				_vertex[idx++] = nh.normal.z;

				_vertex[idx++] = uv.x + TILE_ATLAS_SIZE;
				_vertex[idx++] = uv.y + TILE_ATLAS_SIZE;

				float rightDelta = Math.abs(h - hXY);
				float leftDelta = Math.abs(hX - hY);

				_maxHeight = Utils.max(_maxHeight, h, hX, hY, hXY);
				_minHeight = Utils.min(_minHeight, h, hX, hY, hXY);

				//index
				if (rightDelta < leftDelta)
				{
					_index[idv++] = vertex_count;
					_index[idv++] = (short) (vertex_count + 3);
					_index[idv++] = (short) (vertex_count + 1);

					_index[idv++] = vertex_count;
					_index[idv++] = (short) (vertex_count + 2);
					_index[idv++] = (short) (vertex_count + 3);
				}
				else
				{
					_index[idv++] = vertex_count;
					_index[idv++] = (short) (vertex_count + 2);
					_index[idv++] = (short) (vertex_count + 1);

					_index[idv++] = (short) (vertex_count + 1);
					_index[idv++] = (short) (vertex_count + 2);
					_index[idv++] = (short) (vertex_count + 3);
				}

				vertex_count += 4;
			}
		}
		_mesh.setVertices(_vertex);
		_mesh.setIndices(_index);

		_boundingBox = new BoundingBox(
				new Vector3(_gx + _cx, _minHeight, _gy + _cy),
				new Vector3(_gx + _cx + CHUNK_SIZE, _maxHeight, _gy + _cy + CHUNK_SIZE));

		// определим нужна ли вода в этом чанке?
		// хоть одна вершина ниже уровня воды?
		if (_minHeight < WATER_LEVEL)
		{
			makeWater();
		}
	}

	protected void makeWater()
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

	public class NormalHeight
	{
		public final float h;
		public final Vector3 normal;
		public final boolean isBorder;

		public NormalHeight(int tx, int ty, GridChunk chunk)
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

			// посчитаем нормаль
			normal = new Vector3(h1 - h2, 2.0f, h3 - h4).nor();
			normal.rotate(90f + 45f, 0, 1, 0);
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
		_mesh.dispose();
	}

	public boolean isBorder()
	{
		return _isBorder;
	}
}
