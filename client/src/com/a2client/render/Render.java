package com.a2client.render;

import com.a2client.Config;
import com.a2client.ModelManager;
import com.a2client.ObjectCache;
import com.a2client.Terrain;
import com.a2client.model.GameObject;
import com.a2client.render.water.WaterFrameBuffers;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Terrain.WATER_LEVEL;

/**
 * примитивный рендер, пока один. может еще добавим других Created by arksu on
 * 25.02.15.
 */
public class Render
{
	private static final Logger _log = LoggerFactory.getLogger(Render.class.getName());

	public static final String SHADER_VERSION = "#version 140";

	public static final int GL_CLIP_DISTANCE0 = 0x3000;

	public static Vector3 clipNormal = new Vector3(0, -1, 0);
	public static float clipHeight = 1.5f;

	private Game _game;

	private ModelBatch _modelBatch;

	private Terrain _terrain;

	private Skybox _skybox;

	private WaterFrameBuffers _waterFrameBuffers;

	private Environment _environment;

	private GameObject _selected;

	private float _selectedDist;
	private int _renderedObjects;

	FrameBuffer frameBuffer;
	FrontFaceDepthShaderProvider _depthShaderProvider;
	ModelBatch _depthModelBatch;
	ModelBatch _simpleModelBatch;

	private Mesh fullScreenQuad;
	private Mesh testQuad1;
	private Mesh testQuad2;

	ModelInstance _testModel;

	public Render(Game game)
	{
		ShaderProgram.pedantic = false;
		_game = game;

		_environment = new Environment();
		_environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		_environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		_depthShaderProvider = new FrontFaceDepthShaderProvider();
		_depthModelBatch = new ModelBatch(_depthShaderProvider);

		_simpleModelBatch = new ModelBatch(new ModelShaderProvider());
		_modelBatch = _depthModelBatch;

		_skybox = new Skybox();
		_waterFrameBuffers = new WaterFrameBuffers();
		_terrain = new Terrain(_waterFrameBuffers);

		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

		fullScreenQuad = createFullScreenQuad();
		testQuad1 = createTestQuad(0.7f, -1, 0.3f);
		testQuad2 = createTestQuad(0.7f, 0, 0.3f);

		// _modelInstance3.copy();

		// test2.d
		// System.out.println(test2.);


		_testModel = ModelManager.getInstance().getModelByType(19);
	}

	public void render(Camera camera)
	{
		boolean water = true;
		_skybox.updateDayNight();

		/*if (Config._renderOutline)
		{
			_modelBatch = _depthModelBatch;
			_terrain._shader = _terrain._shaderDepth;

			frameBuffer.begin();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glCullFace(GL20.GL_FRONT);
			// Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
			// Gdx.gl.glDepthMask(true);

			renderTerrain(camera);
			renderObjects(camera, false);
			frameBuffer.end();
		}*/

		_modelBatch = _simpleModelBatch;
		_terrain._shader = _terrain._shaderTerrain;


		// WATER 1 REFLECTION ==========================================================================================
		if (water)
		{
			float camDistance = 2 * (camera.position.y - WATER_LEVEL);
			camera.position.y -= camDistance;
			camera.direction.y = -camera.direction.y;
			camera.update(true);
			clipNormal = new Vector3(0, 1, 0);
			clipHeight = -WATER_LEVEL;
			_waterFrameBuffers.getReflectionFrameBuffer().begin();

			Gdx.gl.glEnable(GL_CLIP_DISTANCE0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glEnable(GL20.GL_CULL_FACE);
			Gdx.gl.glCullFace(GL20.GL_BACK);

			_skybox.Render(camera, _environment);
			renderTerrain(camera);
			renderObjects(camera, false);

			_waterFrameBuffers.getReflectionFrameBuffer().end();
			camera.position.y += camDistance;
			camera.direction.y = -camera.direction.y;
			camera.update(true);

			// WATER 2 REFRACTION, UNDER WATER =============================================================================
			clipNormal = new Vector3(0, -1, 0);
			clipHeight = WATER_LEVEL;
			_waterFrameBuffers.getRefractionFrameBuffer().begin();

			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			// под водой всяко не видать неба
//		_skybox.Render(camera, _environment);
			renderTerrain(camera);
			renderObjects(camera, false);

			_waterFrameBuffers.getRefractionFrameBuffer().end();
		}
		// MAIN RENDER =================================================================================================

		Gdx.gl.glDisable(GL_CLIP_DISTANCE0);
//		clipNormal = new Vector3(0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		_skybox.Render(camera, _environment);
		renderWater(camera);
		renderTerrain(camera);
		renderObjects(camera, true);

		if (_game.getWorldMousePos() != null)
		{
			_modelBatch.begin(camera);
			_testModel.transform.setTranslation(_game.getWorldMousePos());
			_modelBatch.render(_testModel, _environment);
			_modelBatch.end();
		}

		// GUIGDX.getSpriteBatch().begin();

		/*if (Config._renderOutline)
		{
			// выводим содержимое буфера
			frameBuffer.getColorBufferTexture().bind();

			ShaderProgram program = _terrain._shaderOutline;
//			 ShaderProgram program = _terrain._shaderCel;

			program.begin();
			program.setUniformf("size", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
			fullScreenQuad.render(program, GL20.GL_TRIANGLE_STRIP);
			program.end();
		}*/

		if (Config._renderOutline)
		{
			// выводим содержимое буфера

			ShaderProgram program = _terrain._shaderCel;

			program.begin();
//			program.setUniformf("size", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
			_waterFrameBuffers.getReflectionFrameBuffer().getColorBufferTexture().bind();
			testQuad1.render(program, GL20.GL_TRIANGLE_STRIP);
			_waterFrameBuffers.getRefractionFrameBuffer().getColorBufferTexture().bind();
			testQuad2.render(program, GL20.GL_TRIANGLE_STRIP);
			program.end();
		}

		// GUIGDX.getSpriteBatch().setShader(_terrain._shader);
		// _terrain._shader.begin();
		// Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D,
		// frameBuffer.getDepthBufferHandle());
		// _terrain._shader.setUniformi("u_texture", 0);
		// _terrain._shader.end();

		// GUIGDX.getSpriteBatch().end();
	}

	protected void renderObjects(Camera camera, boolean findIntersect)
	{
		_renderedObjects = 0;
		if (ObjectCache.getInstance() != null)
		{
			_selected = null;
			_selectedDist = 100500;
			_modelBatch.begin(camera);
			for (GameObject o : ObjectCache.getInstance().getObjects())
			{
				ModelInstance model = o.getModel();
				// если объект попадает в поле зрения камеры
				if (model != null && camera.frustum.boundsInFrustum(o.getBoundingBox()))
				{
//					if (o.getTypeId() == 1 && _game.getWorldMousePos() != null)
//					{
//						model.transform.setTranslation(_game.getWorldMousePos());
//					}
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

	protected void renderTerrain(Camera camera)
	{
//		_terrain._shader.setUniformf();
		_terrain.render(camera, _environment);
	}

	protected void renderWater(Camera camera)
	{
		_terrain.renderWater(camera, _environment);
	}

	public Mesh createFullScreenQuad()
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

	public GameObject getSelected()
	{
		return _selected;
	}

	public int getRenderedObjects()
	{
		return _renderedObjects;
	}

	public static ShaderProgram makeShader(String vertFile, String fragFile)
	{
		String vertSource = Gdx.files.internal(vertFile).readString();
		String fragSource = Gdx.files.internal(fragFile).readString();

		vertSource = SHADER_VERSION + "\n" + vertSource;
		fragSource = SHADER_VERSION + "\n" + fragSource;

		ShaderProgram program = new ShaderProgram(vertSource, fragSource);

		if (!program.isCompiled())
		{
			_log.warn("shader ERROR " + program.getLog());
			_log.warn("shader V " + program.getVertexShaderSource());
			_log.warn("shader F " + program.getFragmentShaderSource());
		}
		return program;
	}
}
