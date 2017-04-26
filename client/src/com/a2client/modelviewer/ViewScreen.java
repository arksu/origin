package com.a2client.modelviewer;

import com.a2client.Config;
import com.a2client.Input;
import com.a2client.g3d.Model;
import com.a2client.g3d.ModelBatch;
import com.a2client.g3d.ModelData;
import com.a2client.gui.GUI;
import com.a2client.gui.GUIGDX;
import com.a2client.gui.GUI_StringList;
import com.a2client.render.GameCamera;
import com.a2client.render.Render;
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

import java.io.File;

import static com.a2client.render.Render.makeShader;

/**
 * экран для просмотра моделей
 * Created by arksu on 12.03.17.
 */
public class ViewScreen extends BaseScreen
{
	private GUI_StringList _modelNames;
	private GUI_StringList _modelAnimations;

	private GameCamera _gameCamera;
	private ModelBatch _modelBatch;
	private ShaderProgram _shader;
	private boolean _cameraDrag = false;
	private boolean[] _oldMouseButtons = new boolean[3];
	private float _rotateAngle = 0f;
	private boolean _isRotate = true;
	private float _yOffset = 0;

	private String MODEL_NAME = "player";

	private Model _activeModel;
	private Model _equip;

	public ViewScreen()
	{
		_modelNames = new GUI_StringList(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				String s = getItem(getSelectedItem());
				MODEL_NAME = s;
				reloadModel();
			}
		};
		_modelNames.setPos(10, 100);
		_modelNames.setSize(140, 300);

		_modelAnimations = new GUI_StringList(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				String s = getItem(getSelectedItem());
				_activeModel.playAnimation(s);
			}
		};
		_modelAnimations.setPos(10, 450);
		_modelAnimations.setSize(140, 200);

		findModelNames();
		Skybox.sunPosition = new Vector3(100, 150, -100);
		ShaderProgram.pedantic = false;

		_gameCamera = new GameCamera();
		_shader = makeShader("modelView");

		_modelBatch = new ModelBatch();

		reloadModel();
	}

	private void findModelNames()
	{
		_modelNames.clear();
		File f = new File(Config.MODELS_DIR);
		File[] files = f.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				String name = file.getName();
				if (name.endsWith(".mdl"))
				{
					_modelNames.Add(name.substring(0, name.length() - 4));
				}
			}
		}
	}

	private void reloadModel()
	{
		ModelData modelData = new ModelData(MODEL_NAME);
		_activeModel = new Model(modelData);

		_modelAnimations.clear();
		for (String s : _activeModel.getData().getAnimations().keySet())
		{
			_modelAnimations.Add(s);
		}
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
		Render.frameFlag = !Render.frameFlag;
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
		if (Input.KeyHit(Keys.R))
		{
			_activeModel.playMergeAnimation("arms_up");
		}
		if (Input.KeyHit(Keys.T))
		{
			_activeModel.playAnimation("idle");
		}
		if (Input.KeyHit(Keys.F))
		{
			ModelData data = new ModelData("axe");
			Model model = new Model(data);
			model.bindTo(_activeModel, "EquipHand.L");
		}
		if (Input.KeyHit(Keys.G))
		{
//			_equip.bindTo(_activeModel, "EquipHand.L");
			ModelData data = new ModelData("axe");
			Model model = new Model(data);
			model.bindTo(_activeModel, "EquipHand.R");
		}
		if (Input.KeyHit(Keys.B))
		{
			_equip.unbind();
		}
		if (Input.KeyHit(Keys.H))
		{
			_equip.playAnimation("ArmatureAction");
		}

		if (_isRotate)
		{
			_rotateAngle += 50 * ModelViewer.deltaTime;
		}

		Matrix4 tmp = new Matrix4();
		tmp.translate(0, _yOffset, 0);
		tmp.rotate(0, 1, 0, _rotateAngle);
//		tmp.scale(3, 3, 3);

		if (_activeModel != null)
		{
			_activeModel.setPos(0, _yOffset, 0);
			_activeModel.setHeading(_rotateAngle, true);

//		_equip.update();
			_activeModel.update();
		}

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
		if (_activeModel != null)
		{
			_modelBatch.render(_activeModel);
		}
//		_modelBatch.render(_equip);
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
		_shader.setUniformf("u_lightPosition", Skybox.sunPosition);

		_shader.setUniformf("u_shadowDistance", -1);

		_shader.setUniformi("u_texture", 0);
		_shader.setUniformi("u_textureNormal", 1);
		_shader.setUniformi("u_textureSpecular", 2);
	}
}
