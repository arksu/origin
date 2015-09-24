package com.a2client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 24.09.15.
 */
public class ShaderTest extends BaseShader
{
	private static final Logger _log = LoggerFactory.getLogger(ShaderTest.class.getName());

//	public ShaderProgram program;
	Camera camera;
	RenderContext context;

	@Override
	public void init()
	{
		program = new ShaderProgram(
				Gdx.files.internal("assets/basic_vert.glsl"),
				Gdx.files.internal("assets/basic_frag.glsl"));

		if (!program.isCompiled())
		{
			Gdx.app.log("Shader", program.getLog());
			Gdx.app.log("Shader V", program.getVertexShaderSource());
			Gdx.app.log("Shader F", program.getFragmentShaderSource());
		}
	}

	@Override
	public int compareTo(Shader other)
	{
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance)
	{
		return true;
	}

//	@Override
//	public void begin(Camera camera, RenderContext context)
//	{
//		this.camera = camera;
//		this.context = context;
//
//		program.begin();
//		program.setUniformMatrix("u_MVPMatrix", camera.combined);
//		program.setUniformMatrix("u_projViewTrans", camera.combined);
//	}

//	@Override
//	public void render(Renderable renderable)
//	{
////		program.setUniformMatrix("u_worldTrans", renderable.worldTransform);
//		renderable.mesh.render(program,
//				renderable.primitiveType,
//				renderable.meshPartOffset,
//				renderable.meshPartSize);
//	}

//	@Override
//	public void end()
//	{
//		program.end();
//	}

//	@Override
//	public void dispose()
//	{
//		program.dispose();
//	}
}
