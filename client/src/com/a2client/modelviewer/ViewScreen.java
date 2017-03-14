package com.a2client.modelviewer;

import com.a2client.Input;
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

import static com.a2client.render.Render.makeShader;

/**
 * экран для просмотра моделей
 * Created by arksu on 12.03.17.
 */
public class ViewScreen extends BaseScreen
{
	private ModelData _modelData;
	private GameCamera _gameCamera;
	private ShaderProgram _shader;
	private boolean _cameraDrag = false;
	private boolean[] _oldMouseButtons = new boolean[3];
	private float _rotateAngle = 0f;
	private boolean _isRotate = true;
	private float _yOffset = -5;

//		private String MODEL_NAME = "rifle";
	private String MODEL_NAME = "handgun";
//	private String MODEL_NAME = "player";
//	private String MODEL_NAME = "untitled";

	public ViewScreen()
	{
		ShaderProgram.pedantic = false;

		_gameCamera = new GameCamera();
		_shader = makeShader("modelView");

		_modelData = new ModelData(MODEL_NAME);
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
			_rotateAngle += 60 * ModelViewer.deltaTime;
		}

		super.onUpdate();
	}

	@Override
	public void onRender()
	{
		GUIGDX.Text("", 5, 5, "FPS: " + Gdx.graphics.getFramesPerSecond());
	}

	@Override
	public void onRender3D()
	{

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		_gameCamera.update(false);

		_shader.begin();

		prepareShader();

//		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
//		_texture.bind();

		_modelData.render(_shader, GL20.GL_TRIANGLES);

		_shader.end();
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
	}

	private void prepareShader()
	{
		_shader.setUniformMatrix("u_projTrans", _gameCamera.projection);
		_shader.setUniformMatrix("u_viewTrans", _gameCamera.view);
		_shader.setUniformMatrix("u_projViewTrans", _gameCamera.combined);

		Matrix4 tmp = new Matrix4();
		tmp.set(_gameCamera.combined.cpy()).mul(new Matrix4().idt());
		_shader.setUniformMatrix("u_projViewWorldTrans", tmp);

		_shader.setUniformf("u_cameraPosition", _gameCamera.position.x, _gameCamera.position.y, _gameCamera.position.z,
		                    1.1881f / (_gameCamera.far * _gameCamera.far));
		_shader.setUniformf("u_cameraDirection", _gameCamera.direction);

		tmp = new Matrix4();
		tmp.translate(0, _yOffset, 0);
		tmp.rotate(0, 1, 0, _rotateAngle);
		_shader.setUniformMatrix("u_worldTrans", tmp);

		_shader.setUniformf("u_ambient", Color.WHITE);

		_shader.setUniformf("u_skyColor", Skybox.fogColor.r, Skybox.fogColor.g, Skybox.fogColor.b);
		_shader.setUniformf("u_density", Skybox.fogEnabled ? Skybox.fogDensity : 0f);
		_shader.setUniformf("u_gradient", Skybox.fogGradient);
		_shader.setUniformf("u_lightPosition", new Vector3(1000, 1500, 100));

		_shader.setUniformf("u_shadowDistance", -1);

		_shader.setUniformi("u_texture", 0);
	}
}
