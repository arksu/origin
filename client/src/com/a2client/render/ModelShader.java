package com.a2client.render;

import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 24.07.16.
 */
public class ModelShader extends DefaultShader
{
	private static final Logger _log = LoggerFactory.getLogger(ModelShader.class.getName());

	public final int u_skyColor;

	public final static Uniform skyColor = new Uniform("u_skyColor");

	public final static Setter skyColorSetter = new GlobalSetter() {
		@Override
		public void set (BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
			shader.set(inputID, Render.SKY_COLOR.r, Render.SKY_COLOR.g, Render.SKY_COLOR.b);
		}
	};


	public ModelShader(Renderable renderable, Config config)
	{
		super(renderable, config);
		u_skyColor = register(skyColor, skyColorSetter);
	}
}
