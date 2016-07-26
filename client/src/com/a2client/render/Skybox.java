package com.a2client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 25.07.16.
 */
public class Skybox
{
	private static final Logger _log = LoggerFactory.getLogger(Skybox.class.getName());

	private static final float SIZE = 400f;

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

	private ShaderProgram _shader;

	private Mesh _mesh;

	private Quaternion q;


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

		_shader = Render.makeShader("assets/shaders/skyboxVertex.glsl", "assets/shaders/skyboxFragment.glsl");

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
		_cubemap.bind();

		Matrix4 matrix4 = new Matrix4().idt();
		matrix4.translate(camera.position);

		_mesh.render(_shader, GL20.GL_TRIANGLES);

		_shader.end();
	}

	protected void prepareShader(Camera camera, Environment environment)
	{

		_shader.setUniformMatrix("u_projTrans", camera.projection);

		Matrix4 tmp;
		tmp = camera.view;
		tmp.translate(camera.position);
		_shader.setUniformMatrix("u_viewTrans", tmp);

		_shader.setUniformf("u_skyColor", Fog.skyColor.r, Fog.skyColor.g, Fog.skyColor.b);
		_shader.setUniformf("u_density", Fog.enabled ? Fog.density : 0f);
		_shader.setUniformf("u_gradient", Fog.gradient);

		_shader.setUniformi("u_texture", 0);
	}

	protected void makeMesh()
	{
		_mesh = new Mesh(true, VERTICES.length / 3, 0,
						 new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position")
		);
		_mesh.setVertices(VERTICES);
	}
}
