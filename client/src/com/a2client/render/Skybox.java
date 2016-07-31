package com.a2client.render;

import com.a2client.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 25.07.16.
 */
public class Skybox
{
	private static final Logger _log = LoggerFactory.getLogger(Skybox.class.getName());

	private static final float SIZE = 400f;

	/**
	 * degrees per second
	 */
	private static final float ROTATE_SPEED = 0.8f;

	private static final float[] VERTICES = {
			-SIZE, SIZE, -SIZE,
			-SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,
			SIZE, SIZE, -SIZE,
			-SIZE, SIZE, -SIZE,

			-SIZE, -SIZE, SIZE,
			-SIZE, -SIZE, -SIZE,
			-SIZE, SIZE, -SIZE,
			-SIZE, SIZE, -SIZE,
			-SIZE, SIZE, SIZE,
			-SIZE, -SIZE, SIZE,

			SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,

			-SIZE, -SIZE, SIZE,
			-SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			SIZE, -SIZE, SIZE,
			-SIZE, -SIZE, SIZE,

			-SIZE, SIZE, -SIZE,
			SIZE, SIZE, -SIZE,
			SIZE, SIZE, SIZE,
			SIZE, SIZE, SIZE,
			-SIZE, SIZE, SIZE,
			-SIZE, SIZE, -SIZE,

			-SIZE, -SIZE, -SIZE,
			-SIZE, -SIZE, SIZE,
			SIZE, -SIZE, -SIZE,
			SIZE, -SIZE, -SIZE,
			-SIZE, -SIZE, SIZE,
			SIZE, -SIZE, SIZE
	};

	private Cubemap _cubemap;
	private Cubemap _cubemapNight;

	private ShaderProgram _shader;

	private Mesh _mesh;

	private float _rotate;

	private float _time = 5000;

	public Skybox()
	{
		_cubemap = new Cubemap(
				Gdx.files.internal("assets/skybox/right.png"),
				Gdx.files.internal("assets/skybox/left.png"),
				Gdx.files.internal("assets/skybox/top.png"),
				Gdx.files.internal("assets/skybox/bottom.png"),
				Gdx.files.internal("assets/skybox/back.png"),
				Gdx.files.internal("assets/skybox/front.png")
		);
		_cubemap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		_cubemapNight = new Cubemap(
				Gdx.files.internal("assets/skybox/nightRight.png"),
				Gdx.files.internal("assets/skybox/nightLeft.png"),
				Gdx.files.internal("assets/skybox/nightTop.png"),
				Gdx.files.internal("assets/skybox/nightBottom.png"),
				Gdx.files.internal("assets/skybox/nightBack.png"),
				Gdx.files.internal("assets/skybox/nightFront.png")
		);
		_cubemapNight.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		_shader = Render.makeShader("skyboxVertex.glsl", "skyboxFragment.glsl");

		makeMesh();

	}

	public void clear()
	{
		_cubemap.dispose();
	}

	public void Render(Camera camera, Environment environment)
	{
		_shader.begin();
		prepareShader(camera, environment);

		Matrix4 matrix4 = new Matrix4().idt();
		matrix4.translate(camera.position);

		_mesh.render(_shader, GL20.GL_TRIANGLES);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);

		_shader.end();
	}

	protected void prepareShader(Camera camera, Environment environment)
	{
		_rotate += ROTATE_SPEED * Main.deltaTime;

		_shader.setUniformMatrix("u_projTrans", camera.projection);

		Matrix4 tmp;
		tmp = camera.view.cpy();
		tmp.translate(camera.position);
		tmp.rotate(0, 1, 0, _rotate);
		_shader.setUniformMatrix("u_viewTrans", tmp);

		_shader.setUniformf("u_skyColor", Fog.skyColor.r, Fog.skyColor.g, Fog.skyColor.b);
		_shader.setUniformf("u_density", Fog.enabled ? Fog.density : 0f);
		_shader.setUniformf("u_gradient", Fog.gradient);

		setTextures();
	}

	protected void makeMesh()
	{
		_mesh = new Mesh(true, VERTICES.length / 3, 0,
						 new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position")
		);
		_mesh.setVertices(VERTICES);
	}

	public void updateDayNight()
	{
		_time += Main.deltaTime * 500f;
		_time %= 24000;

	}
	protected void setTextures() {
		Cubemap texture1;
		Cubemap texture2;

		float blendValue;

		if(_time >= 0 && _time < 5000){
			texture1 = _cubemapNight;
			texture2 = _cubemapNight;
			blendValue = (_time - 0)/(5000 - 0);
		}else if(_time >= 5000 && _time < 8000){
			texture1 = _cubemapNight;
			texture2 = _cubemap;
			blendValue = (_time - 5000)/(8000 - 5000);
		}else if(_time >= 8000 && _time < 21000){
			texture1 = _cubemap;
			texture2 = _cubemap;
			blendValue = (_time - 8000)/(21000 - 8000);
		}else{
			texture1 = _cubemap;
			texture2 = _cubemapNight;
			blendValue = (_time - 21000)/(24000 - 21000);
		}

		_shader.setUniformf("u_blendValue", blendValue);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		texture1.bind();
		_shader.setUniformi("u_texture1", 0);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE1);
		texture2.bind();
		_shader.setUniformi("u_texture2", 1);

	}
}
