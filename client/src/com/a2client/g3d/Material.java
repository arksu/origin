package com.a2client.g3d;

import com.a2client.Config;
import com.a2client.Main;
import com.a2client.render.shadows.ShadowBox;
import com.a2client.util.Utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.lwjgl.opengl.GL13;

/**
 * материал для мешей
 * Created by arksu on 13.03.17.
 */
public class Material
{
	/**
	 * имена текстур
	 */
	private Texture _diffuse;
	private Texture _normal;
	private Texture _specular;

	/**
	 * может ли меш отбрасывать тень
	 */
	private boolean _castShadows;

	/**
	 * появляется ли тень на этом меше?
	 */
	private boolean _receiveShadows;

	/**
	 * рисовать черную обводку у объектов если включен постпроцессинг
	 */
	private boolean _outline = true;

	public Material(ModelDesc.Material material)
	{
		_castShadows = material.castShadows;
		_receiveShadows = material.receiveShadows;
		_outline = material.outline;

		TextureLoader.TextureParameter parameter = new TextureLoader.TextureParameter();

		String filter = material.filter;
		if (Utils.isEmpty(filter) || filter.equalsIgnoreCase("linear"))
		{
			parameter.minFilter = Texture.TextureFilter.Linear;
			parameter.magFilter = Texture.TextureFilter.Linear;
		}
		else if (filter.equalsIgnoreCase("nearest"))
		{
			parameter.minFilter = Texture.TextureFilter.Nearest;
			parameter.magFilter = Texture.TextureFilter.Nearest;
		}

		if (!Utils.isEmpty(material.diffuse))
		{
			Main.getAssetManager().load(Config.MODELS_DIR + material.diffuse, Texture.class, parameter);
		}

		if (!Utils.isEmpty(material.normal))
		{
			Main.getAssetManager().load(Config.MODELS_DIR + material.normal, Texture.class, parameter);
		}

		if (!Utils.isEmpty(material.specular))
		{
			Main.getAssetManager().load(Config.MODELS_DIR + material.specular, Texture.class, parameter);
		}

		Main.getAssetManager().finishLoading();

		if (!Utils.isEmpty(material.diffuse))
		{
			_diffuse = Main.getAssetManager().get(Config.MODELS_DIR + material.diffuse, Texture.class);
		}
		if (!Utils.isEmpty(material.normal))
		{
			_normal = Main.getAssetManager().get(Config.MODELS_DIR + material.normal, Texture.class);
		}
		if (!Utils.isEmpty(material.specular))
		{
			_specular = Main.getAssetManager().get(Config.MODELS_DIR + material.specular, Texture.class);
		}
	}

	public void bind(ShaderProgram shader)
	{
		if (_normal != null)
		{
			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE1);
			_normal.bind();
			shader.setUniformi("u_normalMapFlag", 1);
		}
		else
		{
			shader.setUniformi("u_normalMapFlag", 0);
		}

		if (_specular != null)
		{
			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE2);
			_specular.bind();
			shader.setUniformi("u_specularMapFlag", 1);
		}
		else
		{
			shader.setUniformi("u_specularMapFlag", 0);
		}

		if (_diffuse != null)
		{
			Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
			_diffuse.bind();
			shader.setUniformi("u_diffuseFlag", 1);
		}
		else
		{
			shader.setUniformi("u_diffuseFlag", 0);
		}

		if (_outline)
		{
			shader.setUniformi("u_outlineFlag", 1);
		}
		else
		{
			shader.setUniformi("u_outlineFlag", 0);
		}

		shader.setUniformf(
				"u_shadowDistance",
				Config.getInstance()._renderShadows && _receiveShadows ? ShadowBox.SHADOW_DISTANCE : -1f);
	}

	public boolean isCastShadows()
	{
		return _castShadows;
	}

	public boolean isReceiveShadows()
	{
		return _receiveShadows;
	}
}
