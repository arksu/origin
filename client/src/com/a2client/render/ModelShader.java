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
	public final int u_density;
	public final int u_gradient;
	public final int u_clipPlane;

	public final static Uniform skyColor = new Uniform("u_skyColor");
	public final static Uniform density = new Uniform("u_density");
	public final static Uniform gradient = new Uniform("u_gradient");
	public final static Uniform clipPlane = new Uniform("u_clipPlane");

	public final static Setter skyColorSetter = new GlobalSetter()
	{
		@Override
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes)
		{
			shader.set(inputID, Fog.skyColor.r, Fog.skyColor.g, Fog.skyColor.b);
		}
	};
	public final static Setter densitySetter = new GlobalSetter()
	{
		@Override
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes)
		{
			shader.set(inputID, Fog.enabled ? Fog.density : 0f);
		}
	};
	public final static Setter gradientSetter = new GlobalSetter()
	{
		@Override
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes)
		{
			shader.set(inputID, Fog.gradient);
		}
	};
	public final static Setter clipPlanetSetter = new GlobalSetter()
	{
		@Override
		public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes)
		{
			shader.set(inputID, Render.clipNormal.x, Render.clipNormal.y, Render.clipNormal.z, Render.clipHeight);
		}
	};

	public ModelShader(Renderable renderable, Config config)
	{
		super(renderable, config);
		u_skyColor = register(skyColor, skyColorSetter);
		u_density = register(density, densitySetter);
		u_gradient = register(gradient, gradientSetter);
		u_clipPlane = register(clipPlane, clipPlanetSetter);
	}

	@Override
	public void render(final Renderable renderable)
	{
//		if (!renderable.material.has(BlendingAttribute.Type))
//		{
//			context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		}
//		else
//		{
//			context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//		}

		//		if (!renderable.material.has(BlendingAttribute.Type))
//		{
//			context.setDepthTest(GL20.GL_LEQUAL);
//			context.setBlending(false, GL20.GL_ONE, GL20.GL_ONE);
//		}
//		else
//		{
//			context.setDepthTest(GL20.GL_EQUAL);
//			context.setBlending(true, GL20.GL_ONE, GL20.GL_ONE);
//		}
		super.render(renderable);
	}
}
