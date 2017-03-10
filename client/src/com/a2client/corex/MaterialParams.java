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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.a2client.corex.Const.*;

public class MaterialParams
{
	public RENDER_MODE Mode;
	public boolean ReceiveShadow;
	public boolean CastShadow;
	public boolean DepthWrite;
	public int AlphaTest;
	public Const.CULL_FACE CullFace;
	public Const.BLEND_TYPE BlendType;
	public SamplerParams[] Sampler = new SamplerParams[MATERIAL_SAMPLER.values().length];

	public Vec4f Diffuse;
	public Vec3f Emission;
	public float Reflect;
	public Vec3f Specular;
	public float Shininess;

	// size 4f * 3 = 12f * 4 bytes = 48 bytes
	public FloatBuffer Uniform;

	public void load(MyInputStream in)
	{
		try
		{
			// render mode
			byte b = in.readByte();
			for (RENDER_MODE rm : RENDER_MODE.values())
			{
				if (rm.ordinal() == b)
				{
					Mode = rm;
					break;
				}
			}
			ReceiveShadow = in.readByte() == 1;
			CastShadow = in.readByte() == 1;
			DepthWrite = in.readByte() == 1;
			AlphaTest = in.readByte() & 0xff;
			Diffuse = new Vec4f(in);
			Emission = new Vec3f(in);
			Reflect = in.readFloat();
			Specular = new Vec3f(in);
			Shininess = in.readFloat();
			makeUniform();

			// cull face
			b = in.readByte();
			for (CULL_FACE v : CULL_FACE.values())
			{
				if (v.ordinal() == b)
				{
					CullFace = v;
					break;
				}
			}

			// blend type
			b = in.readByte();
			for (BLEND_TYPE v : BLEND_TYPE.values())
			{
				if (v.ordinal() == b)
				{
					BlendType = v;
					break;
				}
			}

			// sampler
			for (int i = 0; i < MATERIAL_SAMPLER.values().length; i++)
			{
				Sampler[i] = new SamplerParams(in);
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * создать буфер для униформа. необходимо его поддерживать в актуальном состоянии. и вызывать этот метод при каждом изменении параметров
	 */
	public void makeUniform()
	{
		ByteBuffer temp = ByteBuffer.allocateDirect(4 * 3 * 4);
		temp.order(ByteOrder.nativeOrder());
		Uniform = temp.asFloatBuffer();
		Uniform.
				       put(Diffuse.x).
				       put(Diffuse.y).
				       put(Diffuse.z).
				       put(Diffuse.w).
				       put(Emission.x).
				       put(Emission.y).
				       put(Emission.z).
				       put(Reflect).
				       put(Specular.x).
				       put(Specular.y).
				       put(Specular.z).
				       put(Shininess).
				       flip();
	}

	public MaterialParams clone()
	{
		MaterialParams m = new MaterialParams();
		m.BlendType = this.BlendType;
		m.CullFace = this.CullFace;
		m.Diffuse = this.Diffuse.clone();
		m.AlphaTest = this.AlphaTest;
		m.CastShadow = this.CastShadow;
		m.DepthWrite = this.DepthWrite;
		m.Emission = this.Emission.clone();
		m.Specular = this.Specular.clone();
		m.Reflect = this.Reflect;
		m.Shininess = this.Shininess;
		m.Mode = this.Mode;
		m.ReceiveShadow = this.ReceiveShadow;

		m.Sampler = new SamplerParams[this.Sampler.length];
		for (int i = 0; i < this.Sampler.length; i++)
		{
			m.Sampler[i] = this.Sampler[i].clone();
		}

		m.makeUniform();
		return m;
	}

	public class SamplerParams
	{
		public Vec2f OffsetUV;
		public Vec2f RepeatUV;
		public float RotateUV;

		public SamplerParams(MyInputStream in)
		{
			try
			{
				OffsetUV = new Vec2f(in);
				RepeatUV = new Vec2f(in);
				RotateUV = in.readFloat();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		public SamplerParams(SamplerParams s)
		{
			this.OffsetUV = s.OffsetUV;
			this.RepeatUV = s.RepeatUV;
			this.RotateUV = s.RotateUV;
		}

		public SamplerParams clone()
		{
			return new SamplerParams(this);
		}
	}
}
