package com.a2client;

import com.a2client.model.Grid;
import com.a2client.model.GridChunk;
import com.a2client.render.Fog;
import com.a2client.util.Utils;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.a2client.model.Grid.CHUNK_SIZE;
import static com.a2client.render.Render.makeShader;

/**
 * ландшафт мира и все что с ним связано
 */
public class Terrain
{
	private static final Logger _log = LoggerFactory.getLogger(Terrain.class.getName());

	// сколько единиц координат в одном тайле
	public static final int TILE_SIZE = 12;
	// размер одного грида в тайлах
	public static final int GRID_SIZE = 100;
	public static final int GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE;
	// размер одного грида в байтах для передачи по сети
	public static final int GRID_SIZE_BYTES = GRID_SIZE * GRID_SIZE * 2;

	public static List<Grid> grids = new LinkedList<>();

	public static final float FAKE_HEIGHT = -100000f;

	public ShaderProgram _shaderTerrain;
	public ShaderProgram _shaderWater;

	public ShaderProgram _shaderCel;
	public ShaderProgram _shaderOutline;
	public ShaderProgram _shaderDepth;

	public ShaderProgram _shader;

	private int _chunksRendered = 0;

	private Texture _tileAtlas;

	public Terrain()
	{

		_shaderTerrain = makeShader("assets/shaders/terrainVertex.glsl", "assets/shaders/terrainFragment.glsl");
		_shaderWater = makeShader("assets/shaders/waterVertex.glsl", "assets/shaders/waterFragment.glsl");

		_shaderCel = makeShader("assets/cel_vert.glsl", "assets/cel_frag.glsl");
		_shaderOutline = makeShader("assets/outline_vert.glsl", "assets/outline_frag.glsl");
		_shaderDepth = makeShader("assets/depth_vertex.glsl", "assets/depth_frag.glsl");

		_tileAtlas = Main.getAssetManager().get(Config.RESOURCE_DIR + "tiles_atlas.png", Texture.class);
		_tileAtlas.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

	}

	public void render(Camera camera, Environment environment)
	{
		_shader.begin();
		prepareShader(camera, environment, _shader);
		_tileAtlas.bind();

		_chunksRendered = 0;
		for (Grid grid : grids)
		{
			_chunksRendered += grid.render(_shader, camera, false);
		}
		_shader.end();
	}

	public void renderWater(Camera camera, Environment environment)
	{
		_shaderWater.begin();
		prepareShader(camera, environment, _shaderWater);
//		_tileAtlas.bind();

		_chunksRendered = 0;
		for (Grid grid : grids)
		{
			_chunksRendered += grid.render(_shader, camera, true);
		}
		_shaderWater.end();
	}

	protected void prepareShader(Camera camera, Environment environment, ShaderProgram shader)
	{
		shader.setUniformMatrix("u_projTrans", camera.projection);
		shader.setUniformMatrix("u_viewTrans", camera.view);
		shader.setUniformMatrix("u_projViewTrans", camera.combined);

		Matrix4 tmp = new Matrix4();
		tmp.set(camera.combined.cpy()).mul(new Matrix4().idt());
		shader.setUniformMatrix("u_projViewWorldTrans", tmp);

		shader.setUniformf("u_cameraPosition", camera.position.x, camera.position.y, camera.position.z,
						   1.1881f / (camera.far * camera.far));
		shader.setUniformf("u_cameraDirection", camera.direction);

		shader.setUniformMatrix("u_worldTrans", new Matrix4().idt());

		shader.setUniformf("u_ambient", ((ColorAttribute) environment.get(ColorAttribute.AmbientLight)).color);

		shader.setUniformf("u_skyColor", Fog.skyColor.r, Fog.skyColor.g, Fog.skyColor.b);
		shader.setUniformf("u_density", Fog.enabled ? Fog.density : 0f);
		shader.setUniformf("u_gradient", Fog.gradient);

		shader.setUniformi("u_texture", 0);

	}

	public int getChunksRendered()
	{
		return _chunksRendered;
	}

	/**
	 * добавить грид в мир
	 */
	public static void addGrid(Grid grid)
	{
		grids.add(grid);
		long timeMillis = System.currentTimeMillis();
		for (Grid grid1 : grids)
		{
			grid1.fillChunks(false);
		}
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
		while (i < grids.size())
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

	private static boolean gridInside(Vec2i gc, int px, int py)
	{
		return (gc.x >= px - GRID_FULL_SIZE - GRID_FULL_SIZE && gc.x < px + GRID_FULL_SIZE &&
				gc.y >= py - GRID_FULL_SIZE - GRID_FULL_SIZE && gc.y < py + GRID_FULL_SIZE);
	}

	/**
	 * удалить все гриды из мира
	 */
	public static void clear()
	{
		grids.clear();
	}

	/**
	 * получить грид по абсолютным координатам тайла
	 */
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

	/**
	 * получить высоту указанного тайла. абсолютные координаты тайла
	 */
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

	/**
	 * получить высоту ландшафта в указанной точки. с учетом интерполяции внутри тайла
	 * @param x абсолютные мировые координаты
	 */
	public static float getHeight(float x, float y)
	{
		int tx = ((int) Math.floor(x));
		int ty = ((int) Math.floor(y));

		int ox = tx;
		int oy = ty;

		Grid grid = getGrid(tx, ty);
		if (grid != null)
		{
			// отнимем координаты грида
			tx -= grid.getTc().x;
			ty -= grid.getTc().y;
			// координаты чанка внутри грида
			int cx = tx / CHUNK_SIZE;
			int cy = ty / CHUNK_SIZE;
			GridChunk chunk = grid.getChunk(cx, cy);
			if (chunk != null)
			{
				float xCoord = x - ox;
				float yCoord = y - oy;

				float height;
//				ox;
//				oy;
				if (xCoord <= (1 - yCoord))
				{
					GridChunk.NormalHeight h = chunk.getNormalHeight(ox, oy);
					GridChunk.NormalHeight hX = chunk.getNormalHeight(ox + 1, oy);
					GridChunk.NormalHeight hY = chunk.getNormalHeight(ox, oy + 1);

					height = Utils.baryCentric(
							new Vector3(0, h.h, 0),
							new Vector3(1, hX.h, 0),
							new Vector3(0, hY.h, 1),
							new Vector2(xCoord, yCoord));
				}
				else
				{
					GridChunk.NormalHeight hX = chunk.getNormalHeight(ox + 1, oy);
					GridChunk.NormalHeight hY = chunk.getNormalHeight(ox, oy + 1);
					GridChunk.NormalHeight hXY = chunk.getNormalHeight(ox + 1, oy + 1);
					height = Utils.baryCentric(
							new Vector3(1, hX.h, 0),
							new Vector3(1, hXY.h, 1),
							new Vector3(0, hY.h, 1),
							new Vector2(xCoord, yCoord));
				}
				return height;
			}
		}

		return FAKE_HEIGHT;
	}

}
