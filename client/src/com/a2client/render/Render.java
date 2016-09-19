package com.a2client.render;

import com.a2client.*;
import com.a2client.model.GameObject;
import com.a2client.render.postprocess.OutlineEffect;
import com.a2client.render.postprocess.PostProcess;
import com.a2client.render.shadows.Shadow;
import com.a2client.render.skybox.Icosahedron;
import com.a2client.render.skybox.Skybox;
import com.a2client.render.water.WaterFrameBuffers;
import com.a2client.screens.Game;
import com.a2client.util.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
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
	 * ну нету этого определения в libgdx, корявая поделка....
	 */
	private static final int GL_CLIP_DISTANCE0 = 0x3000;

	/**
	 * задаем плоскость отсечения (нужно для воды)
	 * и передаем в шейдеры
	 */
	public static Vector3 clipNormal = new Vector3(0, -1, 0);
	public static float clipHeight = 1.5f;

	/**
	 * позиция солнца, учитывается при всем освещении
	 * также передается в шейдеры
	 */
	public static Vector3 sunPosition = new Vector3(10000, 20000, 10000);

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
	private static GameObject _oldSelected;

	private float _selectedDist;
	private int _renderedObjects;

	private FrameBuffer _frameBuffer;
	private DepthShaderProvider _depthShaderProvider;
	private ModelBatch _depthModelBatch;
	private ModelBatch _simpleModelBatch;
	private ShaderProgram _shadowShader;

	private Mesh fullScreenQuad;
	private Mesh testQuad1;
	private Mesh testQuad2;

	ModelInstance _testModel;

	public static Matrix4 toShadowMapSpace;

	public Render(Game game)
	{
		ShaderProgram.pedantic = false;
		_game = game;

		_environment = new Environment();
		_environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		_environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		_depthShaderProvider = new DepthShaderProvider();
		_depthModelBatch = new ModelBatch(_depthShaderProvider);

		_simpleModelBatch = new ModelBatch(new ModelShaderProvider());
		_modelBatch = _depthModelBatch;

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

		_testModel = ModelManager.getInstance().getModelByType(19);

		Icosahedron.init();
	}

	public void render(Camera camera)
	{
		updateSunPos();
		_skybox.updateDayNight();

//		Matrix4 toShadowMapSpace = null;
		toShadowMapSpace = null;
/*
		if (Config._renderOutline)
		{
			_modelBatch = _depthModelBatch;
			_terrain._shader = _terrain._shaderDepth;

			_frameBuffer.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glCullFace(GL20.GL_FRONT);
			// Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
			// Gdx.gl.glDepthMask(true);

			renderTerrain(camera, null);
			renderObjects(camera, false);
			_frameBuffer.end();
		}
*/

		// SHADOWS =====================================================================================================
		if (Config.getInstance()._renderShadows)
		{
			if (_shadow == null)
			{
				_shadow = new Shadow(_game.getCamera(), this);
			}
			_modelBatch = _shadow.getModelBatch();
			_terrain._shader = getShadowShader();

			_shadow.update();
			toShadowMapSpace = _shadow.getToShadowMapSpaceMatrix();
			_shadow.getFrameBuffer().begin();

			Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl.glCullFace(GL20.GL_BACK);

			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			renderObjects(camera, false);

			_shadow.getFrameBuffer().end();

			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE6);
			_shadow.getFrameBuffer().bindDepthTexture();
		}
		else
		{
			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE6);
			Gdx.gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}

		_modelBatch = _simpleModelBatch;
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

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		_skybox.Render(camera, _environment);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		renderTerrain(camera, toShadowMapSpace);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		renderObjects(camera, true);

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		renderWater(camera);

		Icosahedron.render(((GameCamera) camera));

//		if (_game.getWorldMousePos() != null)
//		{
//			_modelBatch.begin(camera);
//			_testModel.transform.setTranslation(_game.getWorldMousePos());
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

		/*if (Config._renderOutline)
		{
			// выводим содержимое буфера
			_frameBuffer.getColorBufferTexture().bind();

			ShaderProgram program = _terrain._shaderOutline;
//			 ShaderProgram program = _terrain._shaderCel;

			program.begin();
			program.setUniformf("size", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
			fullScreenQuad.render(program, GL20.GL_TRIANGLE_STRIP);
			program.end();
		}*/

		if (Config.getInstance()._renderOutline)
		{
			// выводим содержимое буфера9

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
		_renderedObjects = 0;
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
			_modelBatch.begin(camera);
			for (GameObject o : ObjectCache.getInstance().getObjects())
			{
				ModelInstance model = o.getModel();
				// если объект попадает в поле зрения камеры
				if (model != null && camera.frustum.boundsInFrustum(o.getBoundingBox()))
				{
					model.userData = o == _oldSelected && o.getObjectId() != Player.getInstance().getObjectId() && findIntersect ? Boolean.TRUE : Boolean.FALSE;
					_modelBatch.render(model, _environment);
					_renderedObjects++;

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
			_shadowShader = makeShader(Shadow.VERTEX, Shadow.FRAGMENT);
		}
		return _shadowShader;
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

	public static GameObject getOldSelected()
	{
		return _oldSelected;
	}

	public int getRenderedObjects()
	{
		return _renderedObjects;
	}

	public WaterFrameBuffers getWaterFrameBuffers()
	{
		return _waterFrameBuffers;
	}

	float sunDistance = 1000;
	public static float sunAngle = 20f;
	public static boolean sunMoving = true;
	// время дня в 24 часовом формате (0-24)
	public static float sunTime = 0f;

	public void updateSunPos()
	{
		if (Input.KeyHit(Keys.SPACE)) sunMoving = !sunMoving;
		if (sunMoving)
		{
			sunAngle -= Main.deltaTime * 20.9f;
		}

		Vector3 pos = new Vector3(0, sunDistance, 0);
//		pos.rotate(90f - 5f, 1, 0, 0);
		pos.rotate(sunAngle, 0, 0, 1);

		sunPosition.set(pos);

		DirectionalLightsAttribute lights = ((DirectionalLightsAttribute) _environment.get(DirectionalLightsAttribute.Type));
		lights.lights.get(0).set(0.8f, 0.8f, 0.8f, -sunPosition.x, -sunPosition.y, -sunPosition.z);
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
}
