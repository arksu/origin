package com.a2client;

import com.a2client.model.Grid;
import com.a2client.model.GridChunk;
import com.a2client.render.Fog;
import com.a2client.render.Render;
import com.a2client.render.shadows.ShadowBox;
import com.a2client.util.Utils;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
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

	/**
	 * уровень воды в мире
	 */
	public static final float WATER_LEVEL = -0.2f;

	public static final float WATER_WAVE_SPEED = 0.03f;

	/**
	 * сколько единиц координат в одном тайле
	 */
	public static final int TILE_SIZE = 12;

	/**
	 * размер одного грида в тайлах
	 */
	public static final int GRID_SIZE = 100;

	public static final int GRID_FULL_SIZE = GRID_SIZE * TILE_SIZE;

	/**
	 * размер одного грида в байтах для передачи по сети
	 */
	public static final int GRID_SIZE_BYTES = GRID_SIZE * GRID_SIZE * 2;

	public static List<Grid> grids = new LinkedList<>();

	public static final float FAKE_HEIGHT = -100000f;

	public ShaderProgram _shaderTerrain;
	public ShaderProgram _shaderWater;
	public ShaderProgram _shaderWaterSimple;

	public ShaderProgram _shaderCel;
	public ShaderProgram _shaderOutline;
	public ShaderProgram _shaderDepth;

	public ShaderProgram _shader;

	private int _chunksRendered = 0;
	private int _chunksWaterRendered = 0;

	private Texture _tileAtlas;
	private Texture _waterDuDv;
	private Texture _waterNormalMap;

	private final Render _render;

	private float _waterMoveFactor = 0;

	public Terrain(Render render)
	{
		_render = render;
		_shaderTerrain = makeShader("terrain", "terrain");
		_shaderWater = makeShader("water", "water");
		_shaderWaterSimple = makeShader("waterSimple", "waterSimple");

		_shaderCel = makeShader("cel", "cel");
		_shaderOutline = makeShader("outline", "outline");
		_shaderDepth = makeShader("depth", "depth");

		_tileAtlas = Main.getAssetManager().get(Config.RESOURCE_DIR + "tiles_atlas.png", Texture.class);
		_waterDuDv = Main.getAssetManager().get(Config.RESOURCE_DIR + "water/waterdudv.png", Texture.class);
		_waterNormalMap = Main.getAssetManager().get(Config.RESOURCE_DIR + "water/normalmap.png", Texture.class);

		_tileAtlas.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		_waterDuDv.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		_waterDuDv.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		_waterNormalMap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		_waterNormalMap.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
	}

	public void render(Camera camera, Environment environment, Matrix4 toShadowMapSpace)
	{
		_shader.begin();
		prepareTerrainShader(camera, environment, _shader, toShadowMapSpace);
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		_tileAtlas.bind();

		_chunksRendered = 0;
		for (Grid grid : grids)
		{
			_chunksRendered += grid.render(_shader, camera, false);
		}
		_shader.end();
	}

	public void renderImproveWater(Camera camera, Environment environment)
	{
		_waterMoveFactor += WATER_WAVE_SPEED * Main.deltaTime;
		_waterMoveFactor %= 1;
		_shaderWater.begin();
		prepareWaterShader(camera, environment, _shaderWater);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		_render.getWaterFrameBuffers().getReflectionFrameBuffer().getColorBufferTexture().bind();

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE1);
		_render.getWaterFrameBuffers().getRefractionFrameBuffer().getColorBufferTexture().bind();

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE2);
		_waterDuDv.bind();

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE3);
		_waterNormalMap.bind();

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE4);
		_render.getWaterFrameBuffers().getRefractionFrameBuffer().bindDepthTexture();

		renderWaterChunks(camera);
		_shaderWater.end();

		Gdx.gl.glDisable(GL11.GL_BLEND);
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void renderSimpleWater(Camera camera, Environment environment)
	{
		_shaderWaterSimple.begin();
		prepareTerrainShader(camera, environment, _shaderWaterSimple, null);
		Gdx.gl.glEnable(GL11.GL_BLEND);
		Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		renderWaterChunks(camera);
		_shaderWaterSimple.end();
		Gdx.gl.glDisable(GL11.GL_BLEND);
	}

	protected void renderWaterChunks(Camera camera)
	{
		_chunksWaterRendered = 0;
		for (Grid grid : grids)
		{
			_chunksWaterRendered += grid.render(_shaderWater, camera, true);
		}
	}

	protected void prepareTerrainShader(Camera camera, Environment environment,
										ShaderProgram shader,
										Matrix4 toShadowMapSpace)
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
		shader.setUniformf("u_lightPosition", Render.sunPosition);

		shader.setUniformf("u_clipPlane", Render.clipNormal.x, Render.clipNormal.y, Render.clipNormal.z, Render.clipHeight);

		if (toShadowMapSpace != null && Config._renderShadows)
		{
			shader.setUniformMatrix("u_toShadowMapSpace", toShadowMapSpace);
			shader.setUniformi("u_shadowMap", 6);
			shader.setUniformf("u_shadowDistance", ShadowBox.SHADOW_DISTANCE);
		}
		else
		{
			shader.setUniformf("u_shadowDistance", -1);
		}

		shader.setUniformi("u_texture", 0);

	}

	protected void prepareWaterShader(Camera camera, Environment environment, ShaderProgram shader)
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

		shader.setUniformf("u_moveFactor", _waterMoveFactor);
		shader.setUniformf("u_lightColor", new Vector3(1, 1, 1));
		shader.setUniformf("u_lightPosition", Render.sunPosition);

		shader.setUniformi("u_reflectionTexture", 0);
		shader.setUniformi("u_refractionTexture", 1);
		shader.setUniformi("u_dudvMap", 2);
		shader.setUniformi("u_normalMap", 3);
		shader.setUniformi("u_depthMap", 4);

		Gdx.gl.glEnable(GL11.GL_BLEND);
		Gdx.gl.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public int getChunksRendered()
	{
		return _chunksRendered;
	}

	public int getChunksWaterRendered()
	{
		return _chunksWaterRendered;
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
	 * получить грид по координатам тайла
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
	 * получить высоту указанного тайла. координаты тайла
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
