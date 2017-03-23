package com.a2client.modelviewer;

import com.a2client.Input;
import com.a2client.g3d.Model;
import com.a2client.g3d.ModelBatch;
import com.a2client.g3d.ModelData;
import com.a2client.gui.GUIGDX;
import com.a2client.render.GameCamera;
import com.a2client.render.skybox.Skybox;
import com.a2client.screens.BaseScreen;
import com.a2client.util.Keys;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.a2client.render.Render.makeShader;

/**
 * экран для просмотра моделей
 * Created by arksu on 12.03.17.
 */
public class ViewScreen extends BaseScreen
{
	private List<Model> _models = new ArrayList<>();

	private GameCamera _gameCamera;
	private ModelBatch _modelBatch;
	private ShaderProgram _shader;
	private boolean _cameraDrag = false;
	private boolean[] _oldMouseButtons = new boolean[3];
	private float _rotateAngle = 0f;
	private boolean _isRotate = true;
	private float _yOffset = 0;

	//		private String MODEL_NAME = "rifle";
//	private String MODEL_NAME = "handgun";
	private String MODEL_NAME = "rabbit";
//	private String MODEL_NAME = "untitled";

	public ViewScreen()
	{
		ShaderProgram.pedantic = false;

		_gameCamera = new GameCamera();
		_shader = makeShader("modelView");

		_modelBatch = new ModelBatch();
		ModelData modelData = new ModelData(MODEL_NAME);
		ModelData modelData2 = null;// new ModelData("rifle");

		Model rotatingModel = new Model(modelData);
		_models.add(rotatingModel);

		Random random = new Random();
		for (int i = 0; i < 0; i++)
		{
			Model model;
			if (random.nextInt(1) == 0)
			{
				model = new Model(modelData);
			}
			else
			{
				model = new Model(modelData2);
			}
			final int range = 10;
			float x = random.nextInt(range) - range / 2;
			float y = random.nextInt(range) - range / 2;
			float z = random.nextInt(range) - range / 2;

			model.setPos(x, y, z);
			float a = random.nextFloat() * 360;
			model.getTransform().rotate(0, 1, 0, a);
			model.updateWorldTransform();

			if (random.nextInt(2) == 0)
			{
				rotatingModel.addChild(model);
			}
			else
			{
				_models.add(model);
			}
		}

//		Animation animation = _models.get(0).getData().getAnimation();
//		if (animation != null)
//		{
//			animation.play();
//		}
		_models.get(0).play();
	}

	@Override
	public void resize(int width, int height)
	{
		_gameCamera.onResize(width, height);
		super.resize(width, height);
	}

	@Override
	public void onUpdate()
	{
		if (Input.MouseBtns[0] && !_oldMouseButtons[0])
		{
			_cameraDrag = true;
			_gameCamera.startDrag(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
		}
		else if (!Input.MouseBtns[0] && _oldMouseButtons[0])
		{
			_cameraDrag = false;
		}
		_oldMouseButtons = Input.MouseBtns.clone();

		if (_cameraDrag)
		{
			_gameCamera.updateDrag(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
		}

		_gameCamera.update();

		if (Input.KeyDown(Keys.W))
		{
			_yOffset += 0.1f;
		}
		if (Input.KeyDown(Keys.S))
		{
			_yOffset -= 0.1f;
		}
		if (Input.KeyHit(Keys.SPACE))
		{
			_isRotate = !_isRotate;
		}

		if (_isRotate)
		{
			_rotateAngle += 50 * ModelViewer.deltaTime;
		}

		Matrix4 tmp = new Matrix4();
		tmp.translate(0, _yOffset, 0);
		tmp.rotate(0, 1, 0, _rotateAngle);
//		tmp.scale(3, 3, 3);

		_models.get(0).setPos(0, _yOffset, 0);
		_models.get(0).setHeading(_rotateAngle, true);

		_models.get(0).update();

//		Animation animation = _models.get(0).getData().getAnimation();
//		if (animation != null)
//		{
//			animation.update();
//		}

		super.onUpdate();
	}

	@Override
	public void onRender()
	{
		GUIGDX.Text("", 5, 5, "FPS: " + Gdx.graphics.getFramesPerSecond());
		GUIGDX.Text("", 5, 25, "mesh switch: " + _modelBatch.getSwitchMeshCounter());
		GUIGDX.Text("", 5, 45, "material switch: " + _modelBatch.getSwitchMaterialCounter());
		GUIGDX.Text("", 5, 65, "render objects: " + _modelBatch.getRenderedCounter());
	}

	@Override
	public void onRender3D()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		_gameCamera.update(false);

		// ======================================================

		_modelBatch.begin(_gameCamera, _shader, false);
		prepareShader();
		for (Model model : _models)
		{
			_modelBatch.render(model);
		}
		_modelBatch.end();

		// ======================================================
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
	}

	private void prepareShader()
	{
		_shader.setUniformMatrix("u_projTrans", _gameCamera.projection);
		_shader.setUniformMatrix("u_viewTrans", _gameCamera.view);
		_shader.setUniformMatrix("u_projViewTrans", _gameCamera.combined);

		_shader.setUniformf("u_cameraPosition", _gameCamera.position.x, _gameCamera.position.y, _gameCamera.position.z,
		                    1.1881f / (_gameCamera.far * _gameCamera.far));
		_shader.setUniformf("u_cameraDirection", _gameCamera.direction);

		_shader.setUniformf("u_ambient", Color.WHITE);

		_shader.setUniformf("u_skyColor", Skybox.fogColor.r, Skybox.fogColor.g, Skybox.fogColor.b);
		_shader.setUniformf("u_density", Skybox.fogEnabled ? Skybox.fogDensity : 0f);
		_shader.setUniformf("u_gradient", Skybox.fogGradient);
		_shader.setUniformf("u_lightPosition", new Vector3(100, 1500, 100));

		_shader.setUniformf("u_shadowDistance", -1);

		_shader.setUniformi("u_texture", 0);
		_shader.setUniformi("u_textureNormal", 1);
		_shader.setUniformi("u_textureSpecular", 2);
	}
}
