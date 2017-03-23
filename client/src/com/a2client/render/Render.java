package com.a2client.render;

import com.a2client.Config;
import com.a2client.ObjectCache;
import com.a2client.Player;
import com.a2client.Terrain;
import com.a2client.g3d.Model;
import com.a2client.g3d.ModelBatch;
import com.a2client.model.GameObject;
import com.a2client.render.postprocess.OutlineEffect;
import com.a2client.render.postprocess.PostProcess;
import com.a2client.render.shadows.Shadow;
import com.a2client.render.skybox.Icosahedron;
import com.a2client.render.skybox.Skybox;
import com.a2client.render.water.WaterFrameBuffers;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Terrain.WATER_LEVEL;
import static org.lwjgl.opengl.GL30.GL_CLIP_DISTANCE0;

/**
 * примитивный рендер, пока один. может еще добавим других
 * Created by arksu on 25.02.15.
 */
public class Render
{
	private static final Logger _log = LoggerFactory.getLogger(Render.class.getName());

	/**
	 * версия шейдеров. прописывается ВО ВСЕ шейдеры в самом начале
	 */
	public static final String SHADER_VERSION = "#version 330";

	/**
	 * где лежат все шейдеры
	 */
	public static final String SHADER_DIR = "assets/shaders/";

	/**
	 * задаем плоскость отсечения (нужно для воды)
	 * и передаем в шейдеры
	 */
	public static Vector3 clipNormal = new Vector3(0, -1, 0);
	public static float clipHeight = 1.5f;

	private Game _game;
	private Environment _environment;

	private ModelBatch _modelBatch;

	private Terrain _terrain;
	private Skybox _skybox;
	private WaterFrameBuffers _waterFrameBuffers;
	private Shadow _shadow;
	private PostProcess _postProcess;

	/**
	 * объект в который попадает луч из мыши (объект под мышью)
	 */
	private GameObject _selected;
	private GameObject _oldSelected;

	private float _selectedDist;

	private FrameBuffer _frameBuffer;
	private ShaderProgram _modelShader;
	private ShaderProgram _shadowShader;
	private ShaderProgram _defaultShader;

	private Mesh fullScreenQuad;
	private Mesh testQuad1;
	private Mesh testQuad2;

//	ModelInstance _testModel;

	public static Matrix4 toShadowMapSpace;

	public Render(Game game)
	{
		ShaderProgram.pedantic = false;
		_game = game;

		_environment = new Environment();
		_environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		_environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		_defaultShader = makeShader("modelView");
		_modelShader = _defaultShader;
		_modelBatch = new ModelBatch();

		_skybox = new Skybox();
		_terrain = new Terrain(this);

		_postProcess = new PostProcess();
		_postProcess.addEffect(new OutlineEffect(true));
//		_postProcess.addEffect(new ContrastEffect(true));
//		_postProcess.addEffect(new DepthOfFieldEffect(true));
//		_postProcess.addEffect(new MotionBlurEffect(true));
//		_postProcess.addEffect(new HorizontalBlurEffect(1f / 2f));
//		_postProcess.addEffect(new VerticalBlurEffect(1f / 2f));
//		_postProcess.addEffect(new HorizontalBlurEffect(1f / 8f));
//		_postProcess.addEffect(new VerticalBlurEffect(1f / 8f));
//		_postProcess.addEffect(new EmptyEffect(true));

		_frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

		fullScreenQuad = createFullScreenQuad();
		// for debug
		testQuad1 = createTestQuad(0.7f, -1, 0.3f);
		testQuad2 = createTestQuad(0.7f, 0, 0.3f);

//		_testModel = ModelManager.getInstance().getModelByType(2);

		Icosahedron.init();
	}

	public void render(Camera camera)
	{
		Skybox.updateSunPos();
		_skybox.updateSkyParams();

		toShadowMapSpace = null;

		// SHADOWS =====================================================================================================
		if (Config.getInstance()._renderShadows)
		{
			if (_shadow == null)
			{
				_shadow = new Shadow(_game.getCamera(), this);
			}
			// todo
//			_modelBatch = _shadow.getModelBatch();
			_terrain._shader = getShadowShader();

			_shadow.update();
			toShadowMapSpace = _shadow.getToShadowMapSpaceMatrix();
			_shadow.getFrameBuffer().begin();

			Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl.glCullFace(GL20.GL_BACK);

			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			_modelShader = _shadowShader;
			renderObjects(camera, false);
			_modelShader = _defaultShader;

			_shadow.getFrameBuffer().end();

			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE6);
			_shadow.getFrameBuffer().bindDepthTexture();
		}
		else
		{
			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE6);
			Gdx.gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}

//		_modelBatch = _simpleModelBatch;
		_terrain._shader = _terrain._shaderTerrain;

		// WATER 1 REFLECTION ==========================================================================================
		if (Config.getInstance()._renderImproveWater)
		{
			if (_waterFrameBuffers == null)
			{
				_waterFrameBuffers = new WaterFrameBuffers();
			}
			float camDistance = 2 * (camera.position.y - WATER_LEVEL);
			camera.position.y -= camDistance;
			camera.direction.y = -camera.direction.y;
			camera.up.set(0, 1, 0);
			camera.update(false);
			clipNormal = new Vector3(0, 1, 0);
			clipHeight = -WATER_LEVEL;

			_waterFrameBuffers.getReflectionFrameBuffer().begin();

			Gdx.gl.glEnable(GL_CLIP_DISTANCE0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl.glCullFace(GL20.GL_BACK);

			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			_skybox.Render(camera, _environment);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			renderTerrain(camera, toShadowMapSpace);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			renderObjects(camera, false);

			_waterFrameBuffers.getReflectionFrameBuffer().end();

			camera.position.y += camDistance;
			camera.direction.y = -camera.direction.y;
			camera.update(false);

			// WATER 2 REFRACTION, UNDER WATER =============================================================================
			clipNormal = new Vector3(0, -1, 0);
			// чуть чуть приподнимем. иначе у берега видно дырки
			clipHeight = WATER_LEVEL + 0.05f;

			_waterFrameBuffers.getRefractionFrameBuffer().begin();

			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			// под водой всяко не видать неба
//		_skybox.Render(camera, _environment);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			renderTerrain(camera, toShadowMapSpace);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			renderObjects(camera, false);

			_waterFrameBuffers.getRefractionFrameBuffer().end();
		}
		else
		{
			camera.update(false);
		}

		// POST PROCESSING (prepare)
		if (Config.getInstance()._renderPostProcessing)
		{
			_postProcess.getFrameBuffer().begin();
		}

		// MAIN RENDER =================================================================================================
//		_modelBatch = _depthModelBatch;
		DefaultShader.defaultCullFace = GL20.GL_BACK;

		Gdx.gl.glDisable(GL_CLIP_DISTANCE0);
		clipNormal = new Vector3(0, 0, 0);
//		Gdx.gl.glClearColor(0.1f, 0.3f, 0.9f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);

		Gdx.gl.glDepthRangef(0.999f, 0.999f);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		_skybox.Render(camera, _environment);
		Gdx.gl.glDepthRangef(0, 1);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		renderTerrain(camera, toShadowMapSpace);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		renderObjects(camera, true);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		renderWater(camera);

//		Icosahedron.render(((GameCamera) camera));

//		if (_game.getWorldMousePos() != null)
//		{
//			_modelBatch.begin(camera);
//			_testModel.transform.setTranslation(_game.getTerrainPoint());
//			_modelBatch.render(_testModel, _environment);
//			_modelBatch.end();
//		}
		// END MAIN RENDER =============================================================================================

		// POST PROCESSING
		if (Config.getInstance()._renderPostProcessing)
		{
			_postProcess.getFrameBuffer().end();
			_postProcess.doPostProcess();
		}
		// END POST PROCESSING

		if (Config.getInstance()._renderOutline)
		{
			// выводим содержимое буфера

			ShaderProgram program = _terrain._shaderCel;

			program.begin();
//			_waterFrameBuffers.getReflectionFrameBuffer().getColorBufferTexture().bind();
//			_shadow.getFrameBuffer().getColorBufferTexture().bind();
			_postProcess.getFrameBuffer().bindDepthTexture();
			testQuad1.render(program, GL20.GL_TRIANGLE_STRIP);

			_waterFrameBuffers.getRefractionFrameBuffer().bindDepthTexture();
//			_shadow.getFrameBuffer().getColorBufferTexture().bind();
			testQuad2.render(program, GL20.GL_TRIANGLE_STRIP);
			program.end();
		}
	}

	protected void renderObjects(Camera camera, boolean findIntersect)
	{
		if (ObjectCache.getInstance() != null)
		{
			// ????
//			Gdx.gl.glEnable(GL11.GL_BLEND);
//			Gdx.gl.glBlendFunc(GL20.GL_EQUAL, GL20.GL_ONE);
			if (findIntersect)
			{
				_oldSelected = _selected;
				_selected = null;
				_selectedDist = 100500;
			}
			_modelBatch.begin(camera, _modelShader);
			prepareModelShader(camera);
			for (GameObject o : ObjectCache.getInstance().getObjects())
			{
				Model model = o.getModel();
				// если объект попадает в поле зрения камеры
				if (_modelBatch.render(model))
				{
					model.setUserData(o == _oldSelected && o.getObjectId() != Player.getInstance().getObjectId() && findIntersect ? Boolean.TRUE : Boolean.FALSE);

					// найдем текущий выбранный объект (в который попадает мышь)
					// попадает ли луч из мыши в объект?
					if (_game.getCamera().getRay() != null && findIntersect)
					{
						Vector3 intersection = new Vector3();
						if (Intersector.intersectRayBounds(_game.getCamera().getRay(), o.getBoundingBox(),
						                                   intersection))
						{
							// дистанция до объекта
							float dist = intersection.dst(camera.position);
							// если дистанция меньше предыдушего - обновим объект в который попадает мышь
							if (dist < _selectedDist)
							{
								_selected = o;
								_selectedDist = dist;
							}
						}
					}
				}
			}
			_modelBatch.end();
			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		}
	}

	protected void renderTerrain(Camera camera, Matrix4 toShadowMapSpace)
	{
		_terrain.render(camera, _environment, toShadowMapSpace);
	}

	protected void renderWater(Camera camera)
	{
		if (Config.getInstance()._renderImproveWater)
		{
			_terrain.renderImproveWater(camera, _environment);
		}
		else
		{
			_terrain.renderSimpleWater(camera, _environment);
		}
	}

	public ShaderProgram getShadowShader()
	{
		if (_shadowShader == null)
		{
			_shadowShader = makeShader("shadow");
		}
		return _shadowShader;
	}

	public ModelBatch getModelBatch()
	{
		return _modelBatch;
	}

	/**
	 * квад для вывода FBO на экран
	 */
	public static Mesh createFullScreenQuad()
	{
		float[] verts = new float[16];
		int i = 0;

		verts[i++] = -1.f; // x1
		verts[i++] = -1.f; // y1
		verts[i++] = 0.f; // u1
		verts[i++] = 0.f; // v1

		verts[i++] = 1.f; // x2
		verts[i++] = -1.f; // y2
		verts[i++] = 1.f; // u2
		verts[i++] = 0.f; // v2

		verts[i++] = -1.f; // x3
		verts[i++] = 1.f; // y2
		verts[i++] = 0.f; // u3
		verts[i++] = 1.f; // v3

		verts[i++] = 1.f; // x4
		verts[i++] = 1.f; // y4
		verts[i++] = 1.f; // u4
		verts[i] = 1.f; // v4

		Mesh tmpMesh = new Mesh(true, 4, 0,
		                        new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
		                        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));
		tmpMesh.setVertices(verts);
		return tmpMesh;
	}

	public Mesh createTestQuad(float size, float x, float y)
	{
		float[] verts = new float[16];
		int i = 0;

		verts[i++] = x; // x1
		verts[i++] = y; // y1
		verts[i++] = 0.f; // u1
		verts[i++] = 0.f; // v1

		verts[i++] = x + size; // x2
		verts[i++] = y; // y2
		verts[i++] = 1.f; // u2
		verts[i++] = 0.f; // v2

		verts[i++] = x; // x3
		verts[i++] = y + size; // y2
		verts[i++] = 0.f; // u3
		verts[i++] = 1.f; // v3

		verts[i++] = x + size; // x4
		verts[i++] = y + size; // y4
		verts[i++] = 1.f; // u4
		verts[i] = 1.f; // v4

		Mesh tmpMesh = new Mesh(true, 4, 0,
		                        new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
		                        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));
		tmpMesh.setVertices(verts);
		return tmpMesh;
	}

	public int getChunksRendered()
	{
		return _terrain.getChunksRendered();
	}

	public int getWaterChunksRendered()
	{
		return _terrain.getChunksWaterRendered();
	}

	public GameObject getSelected()
	{
		return _selected;
	}

	public int getRenderedObjects()
	{
		return _modelBatch.getRenderedCounter();
	}

	public WaterFrameBuffers getWaterFrameBuffers()
	{
		return _waterFrameBuffers;
	}

	public static ShaderProgram makeShader(String name)
	{
		return makeShader(name, name);
	}

	public static ShaderProgram makeShader(String vertFile, String fragFile)
	{
		String vertSource = Gdx.files.internal(SHADER_DIR + vertFile + "Vertex.glsl").readString();
		String fragSource = Gdx.files.internal(SHADER_DIR + fragFile + "Fragment.glsl").readString();

		vertSource = SHADER_VERSION + "\n" + vertSource;
		fragSource = SHADER_VERSION + "\n" + fragSource;

		ShaderProgram program = new ShaderProgram(vertSource, fragSource);

		if (!program.isCompiled())
		{
			_log.warn("shader FAILED: " + program.getLog());
			_log.warn("shader V " + program.getVertexShaderSource());
			_log.warn("shader F " + program.getFragmentShaderSource());
		}
		return program;
	}

	public void onResize(int width, int height)
	{
		_postProcess.onResize();
	}

	public void dispose()
	{
		_postProcess.dispose();
		if (_frameBuffer != null) _frameBuffer.dispose();
	}

	private void prepareModelShader(Camera camera)
	{
		_modelShader.setUniformMatrix("u_projTrans", camera.projection);
		_modelShader.setUniformMatrix("u_viewTrans", camera.view);
		_modelShader.setUniformMatrix("u_projViewTrans", camera.combined);

		_modelShader.setUniformf("ucameraPosition", camera.position.x, camera.position.y, camera.position.z,
		                         1.1881f / (camera.far * camera.far));
		_modelShader.setUniformf("ucameraDirection", camera.direction);

		_modelShader.setUniformf("u_ambient", Color.WHITE);

		_modelShader.setUniformf("u_skyColor", Skybox.fogColor.r, Skybox.fogColor.g, Skybox.fogColor.b);
		_modelShader.setUniformf("u_density", Skybox.fogEnabled ? Skybox.fogDensity : 0f);
		_modelShader.setUniformf("u_gradient", Skybox.fogGradient);
//		_modelShader.setUniformf("u_lightPosition", new Vector3(1000, 1500, 100));
		_modelShader.setUniformf("u_lightPosition", Skybox.sunPosition);

		_modelShader.setUniformf("u_shadowDistance", -1);
		_modelShader.setUniformf("u_clipPlane", Render.clipNormal.x, Render.clipNormal.y, Render.clipNormal.z, Render.clipHeight);

		_modelShader.setUniformi("u_texture", 0);
		_modelShader.setUniformi("u_textureNormal", 1);
		_modelShader.setUniformi("u_textureSpecular", 2);
	}
}
