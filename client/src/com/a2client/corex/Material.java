/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2013 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.a2client.corex;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.a2client.corex.Const.*;

public class Material extends ResObject
{
	public List<Const.MATERIAL_SAMPLER> Samplers = new ArrayList<Const.MATERIAL_SAMPLER>();
	public Map<MATERIAL_ATTRIB, ShaderAttrib> Attrib = new EnumMap<MATERIAL_ATTRIB, ShaderAttrib>(MATERIAL_ATTRIB.class);
	public ShaderUniform[] uniform = new ShaderUniform[MATERIAL_UNIFORM.values().length];
	public Shader shader;
	public MaterialParams params;
	public Map<Const.RENDER_MODE, Material> ModeMat = new EnumMap<Const.RENDER_MODE, Material>(Const.RENDER_MODE.class);
	public Texture[] textures = new Texture[16];

	static public Material load(String name)
	{
		ResObject r = ResManager.Get(name + Const.EXT_MATERIAL);
		if (r != null && r instanceof Material)
		{
			return (Material) r;
		}

		Material a = new Material(name + Const.EXT_MATERIAL);
		ResManager.Add(a);
		return a;
	}

	public Material()
	{

	}

	public Material(String name)
	{
		this.name = name;
		String prefix = GetPrefix();
		MyInputStream in = FileSys.getStream(name);
		try
		{
			// read params
			params = new MaterialParams();
			params.load(in);

			String shader_name = in.readAnsiString();
			List<String> defines = new ArrayList<String>();
			int dcount = in.readWord();
			for (int i = 0; i < dcount; i++)
			{
				defines.add(in.readAnsiString());
			}
			defines.add("MODE_NORMAL");

			if (defines.contains("FX_SKIN"))
			{
				defines.remove(defines.indexOf("FX_SKIN"));
			}

			shader = Shader.load(shader_name, defines);

			// read samplers
			String sampler_name;
			for (int i = 0; i < MATERIAL_SAMPLER.values().length; i++)
			{
				sampler_name = in.readAnsiString();
				if (!sampler_name.isEmpty())
				{
					textures[i] = Texture.load(prefix + sampler_name);
					Samplers.add(MATERIAL_SAMPLER.values()[i]);
				}
			}

			if (params.ReceiveShadow)
			{
				Samplers.add(Const.MATERIAL_SAMPLER.msShadow);
			}

			// normal mode
			ModeMat.put(Const.RENDER_MODE.rmOpaque, this);
			// TODO: материал для прозрачности использовать ли?
//            ModeMat.put(Const.RENDER_MODE.rmOpacity, this);

			// shadow mode
			if (params.CastShadow)
			{
				defines.remove(defines.size() - 1);
				defines.add("MODE_SHADOW");
				Material m = new Material();
				m.Samplers = Samplers;
				m.params = params;
				m.params.Mode = Const.RENDER_MODE.rmShadow;
				m.shader = Shader.load(shader_name, defines);
				m.textures[msDiffuse_idx] = textures[msDiffuse_idx];
				ModeMat.put(Const.RENDER_MODE.rmShadow, m);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		for (Const.RENDER_MODE rm : Const.RENDER_MODE.values())
		{
			Material mat = ModeMat.get(rm);
			if (mat != null)
			{
				mat.shader.bind();

				for (Const.MATERIAL_SAMPLER ms : mat.Samplers)
				{
					Const.SamplerIDObject mso = SamplerID[ms.ordinal()];
					mat.shader.getUniform(Const.SHADER_UNIFORM_TYPE.utInt, mso.Name).setValue(mso.ID);
				}

				for (MATERIAL_ATTRIB ma : MATERIAL_ATTRIB.values())
				{
					Const.AttribIDObject mao = Const.AttribID[ma.ordinal()];
					mat.Attrib.put(ma, mat.shader.getAttrib(mao.Type, mao.Name, mao.Norm));
				}

				for (MATERIAL_UNIFORM mu : MATERIAL_UNIFORM.values())
				{
					Const.UniformIDObject muo = Const.UniformID[mu.ordinal()];
					mat.uniform[mu.ordinal()] = mat.shader.getUniform(muo.UType, muo.Name);
				}
			}
		}

	}

	public void bind()
	{
		Material mat = ModeMat.get(Render.Mode);
		if (mat != null)
		{
			Render.setCullFace(params.CullFace);
			Render.setBlendType(params.BlendType);
			Render.setDepthWrite(params.DepthWrite);
			Render.setAlphaTest(params.AlphaTest);

			if (shader != null)
			{
				shader.bind();
				uniform[muModelMatrix_idx].setValue(Render.ModelMatrix);

				if (Render.Mode == RENDER_MODE.rmOpaque || Render.Mode == RENDER_MODE.rmOpacity)
				{
					GL13.glActiveTexture(GL13.GL_TEXTURE0);
					GL11.glMatrixMode(GL11.GL_TEXTURE);
					GL11.glLoadIdentity();

					GL11.glMatrixMode(GL11.GL_MODELVIEW);

					// в рендере всегда храним актуальный массив флоатов для передачи в униформ шейдера
					// обновляем при каждой операции над источниками света
					uniform[muLightPos_idx].setValue(Render.um_light_pos);
					uniform[muLightParam_idx].setValue(Render.um_light_param);
					uniform[muViewPos_idx].setValue(Render.view_pos);
					uniform[muAmbient_idx].setValue(Render.ambient);
					uniform[muTexOffset_idx].setValue(Render.tex_offset);
					uniform[muFog_idx].setValue(Render.fog);

					uniform[muMaterial_idx].setValue(params.Uniform);

					if (params.ReceiveShadow)
					{
						uniform[muLightMatrix_idx].setValue(Render.lights[0].matrix);
						textures[SamplerID[MATERIAL_SAMPLER.msShadow.ordinal()].ID] = Render.lights[0].shadow_map;
					}

				}
				else if (Render.Mode == RENDER_MODE.rmShadow)
				{
					uniform[muLightPos_idx].setValue(Render.lights[0].pos);
					// TODO : shadow mode
					// ???? Uniform[muMaterial].Value(Params.Uniform, 1); в униформ пишется только 4f а это diffuse
					uniform[muMaterial_idx].setValue(params.Diffuse);
				}
			}

			for (int i = 0; i < textures.length; i++)
			{
				if (textures[i] != null)
				{
					textures[i].bind(i);
				}
			}
		}
	}

	/**
	 * поулчить префикс для загрузки материала
	 */
	protected String GetPrefix()
	{
		String n = name;
		int i = n.lastIndexOf("/");
		if (i > 0)
		{
			i++;
			n = n.substring(0, i);
			return n;
		}
		return "";
	}
}
