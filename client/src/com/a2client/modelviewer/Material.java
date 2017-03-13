package com.a2client.modelviewer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * материал для мешей
 * Created by arksu on 13.03.17.
 */
public class Material
{
	private Texture _diffuse;
	private Texture _normal;
	private Texture _specular;

	private boolean _castShadows;
	private boolean _receiveShadows;

	public Material(ModelDesc.Material material)
	{

	}

	public void bind(ShaderProgram shaderProgram)
	{

	}
}
