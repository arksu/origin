package com.a2client.render;

import com.a2client.MapCache;
import com.a2client.ObjectCache;
import com.a2client.Terrain;
import com.a2client.model.GameObject;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * примитивный рендер, пока один. может еще добавим других
 * Created by arksu on 25.02.15.
 */
public class Render1
{
	private static final Logger _log = LoggerFactory.getLogger(Render1.class.getName());

	private Game _game;

	//
	private Model _model;
	private Model _model2;
	private Model _model3;
	private ModelInstance _modelInstance;
	private ModelInstance _modelInstance2;
	private ModelInstance _modelInstance3;
	private ModelBatch _modelBatch;

	//
	Terrain _terrain;

	//
	private Environment _environment;

	private GameObject _selected;

	private float _selectedDist;
	private int _renderedObjects;
	private final Shader _shader;

	FrameBuffer frameBuffer;
	FrontFaceDepthShaderProvider depthshaderprovider;
	ModelBatch depthModelBatch;
	ModelBatch simpleModelBatch;

	private Mesh fullScreenQuad;

	public Render1(Game game)
	{
		ShaderProgram.pedantic = false;
		_game = game;

		_environment = new Environment();
		_environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		_environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		depthshaderprovider = new FrontFaceDepthShaderProvider();
		depthModelBatch = new ModelBatch(depthshaderprovider);

		simpleModelBatch = new ModelBatch();
		_modelBatch = depthModelBatch;

		ModelLoader loader = new ObjLoader();
		_model = loader.loadModel(Gdx.files.internal("assets/debug/invader.obj"));
		_model2 = loader.loadModel(Gdx.files.internal("assets/debug/block.obj"));
		_model3 = loader.loadModel(Gdx.files.internal("assets/debug/ship.obj"));
		_modelInstance = new ModelInstance(_model);
		_modelInstance2 = new ModelInstance(_model2);
		_modelInstance3 = new ModelInstance(_model3);
		_terrain = new Terrain();

		_shader = new ShaderTest();
		_shader.init();
		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

		fullScreenQuad = createFullScreenQuad();
		
	}

	public void render(Camera camera)
	{
		_modelBatch = depthModelBatch;
		_terrain._shader = _terrain._shaderDepth;

		frameBuffer.begin();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT
				| GL20.GL_DEPTH_BUFFER_BIT);

//		Gdx.gl.glCullFace(GL20.GL_BACK);
//		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
//		Gdx.gl.glDepthMask(true);

		_terrain.Render(camera, _environment);
		renderObjects(camera);
		frameBuffer.end();

//		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT
//				| GL20.GL_DEPTH_BUFFER_BIT);

		_modelBatch = simpleModelBatch;
		_terrain._shader = _terrain._shaderBasic;
		_terrain.Render(camera, _environment);
		renderObjects(camera);

//		GUIGDX.getSpriteBatch().begin();

		frameBuffer.getColorBufferTexture().bind();

		ShaderProgram program = _terrain._shaderOutline;
//		ShaderProgram program = _terrain._shaderCel;

		program.begin();
		
		program.setUniformf("size", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		
		fullScreenQuad.render(program, GL20.GL_TRIANGLE_STRIP);
		program.end();

//			GUIGDX.getSpriteBatch().setShader(_terrain._shader);
//			_terrain._shader.begin();
//			Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, frameBuffer.getDepthBufferHandle());
//			_terrain._shader.setUniformi("u_texture", 0);
//			_terrain._shader.end();

//		GUIGDX.getSpriteBatch().end();
	}

	protected void renderObjects(Camera camera)
	{
		_renderedObjects = 0;
		if (ObjectCache.getInstance() != null)
		{
			_selected = null;
			_selectedDist = 100500;
			Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			_modelBatch.begin(camera);
			for (GameObject o : ObjectCache.getInstance().getObjects())
			{
				BoundingBox boundingBox = new BoundingBox(o.getBoundingBox());
				Vector2 oc = new Vector2(o.getCoord()).scl(1f / MapCache.TILE_SIZE);
				Vector3 add = new Vector3(oc.x, 0, oc.y);
				boundingBox.min.add(add);
				boundingBox.max.add(add);
				if (camera.frustum.boundsInFrustum(boundingBox))
				{
					_renderedObjects++;

					if (!o.isInteractive())
					{
						if (o.getTypeId() == 1)
						{
							_modelInstance3.transform.setToTranslation(oc.x, 0.5f, oc.y);
							_modelBatch.render(_modelInstance3, _environment);
						}
						else
						{
							_modelInstance.transform.setToTranslation(oc.x, 0.5f, oc.y);
							_modelBatch.render(_modelInstance, _environment);
						}
					}
					else
					{
						_modelInstance2.transform.setToTranslation(oc.x, 0.5f, oc.y);
						_modelBatch.render(_modelInstance2, _environment);
					}
					Vector3 intersection = new Vector3();
					if (Intersector.intersectRayBounds(ray, boundingBox, intersection))
					{
						float dist = intersection.dst(camera.position);
						if (dist < _selectedDist)
						{
							_selected = o;
							_selectedDist = dist;
						}
					}
				}
			}
			_modelBatch.end();
		}
	}

	public Mesh createFullScreenQuad(){
		float[] verts = new float[16];
		int i = 0;
//		verts[i++] = -1.f; // x1
//		verts[i++] = -1.f; // y1
//		verts[i++] =  0.f; // u1
//		verts[i++] =  0.f; // v1
//		verts[i++] =  1.f; // x2
//		verts[i++] = -1.f; // y2
//		verts[i++] =  1.f; // u2
//		verts[i++] =  0.f; // v2
//		verts[i++] =  1.f; // x3
//		verts[i++] =  1.f; // y2
//		verts[i++] =  1.f; // u3
//		verts[i++] =  1.f; // v3
//		verts[i++] = -1.f; // x4
//		verts[i++] =  1.f; // y4
//		verts[i++] =  0.f; // u4
//		verts[i] =  1.f; // v4

		verts[i++] = -1.f; // x1
		verts[i++] = -1.f; // y1
		verts[i++] =  0.f; // u1
		verts[i++] =  0.f; // v1

		verts[i++] =  1.f; // x2
		verts[i++] = -1.f; // y2
		verts[i++] =  1.f; // u2
		verts[i++] =  0.f; // v2

		verts[i++] =  -1.f; // x3
		verts[i++] =  1.f; // y2
		verts[i++] =  0.f; // u3
		verts[i++] =  1.f; // v3

		verts[i++] =  1.f; // x4
		verts[i++] =  1.f; // y4
		verts[i++] =  1.f; // u4
		verts[i] =  1.f; // v4

		Mesh tmpMesh = new Mesh(true, 4, 0
				, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position")
				, new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));
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
}
