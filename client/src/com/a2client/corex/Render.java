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

import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.a2client.corex.Const.MAX_LIGHTS;

public class Render
{
	public static boolean use_draw_elements = true;

	static public Const.RENDER_MODE Mode = Const.RENDER_MODE.rmOpaque;
	static public Const.CULL_FACE cull_face = Const.CULL_FACE.cfNone;
	static public boolean depth_write = false;
	static public boolean depth_test = false;
	static public int alpha_test = 0;
	static public Const.BLEND_TYPE blend_type = Const.BLEND_TYPE.btNone;
	static public Mat4f ModelMatrix = new Mat4f();
	static public Light[] lights = new Light[Const.MAX_LIGHTS];
	static public Vec3f view_pos = Vec3f.zero;
	static public Vec3f ambient = Vec3f.zero;
	static public Vec2f tex_offset = Vec2f.zero;
	static public boolean frame_flag = false;
	static public Camera camera;
	static private Rect viewport = new Rect(0, 0, 0, 0);
	static public Vec3f fog = Vec3f.zero.clone();

	/**
	 * поддерживается ли мультитекстурирование
	 */
	static public boolean multi_texture;

	/**
	 * массив для передачи в униформ шейдера
	 */
	static public FloatBuffer um_light_pos; // vec3

	/**
	 * массив для передачи в униформ шейдера
	 */
	static public FloatBuffer um_light_param; // vec4
	public static long time = System.currentTimeMillis();
	static public long dt;

	static
	{
		ContextCapabilities caps = GLContext.getCapabilities();
		multi_texture = caps.GL_ARB_multitexture;

		for (int i = 0; i < MAX_LIGHTS; i++)
		{
			lights[i] = new Light();
		}

		ModelMatrix.identity();
	}

	/**
	 * обновить данные для униформа шейдеров
	 */
	static public void updateLights()
	{
		ByteBuffer temp = ByteBuffer.allocateDirect(MAX_LIGHTS * 3 * 4);
		temp.order(ByteOrder.nativeOrder());
		um_light_pos = temp.asFloatBuffer();

		temp = ByteBuffer.allocateDirect(MAX_LIGHTS * 4 * 4);
		temp.order(ByteOrder.nativeOrder());
		um_light_param = temp.asFloatBuffer();

		for (int i = 0; i < Const.MAX_LIGHTS; i++)
		{
			if (Render.lights[i] != null)
			{
				um_light_pos.put(lights[i].pos.x).put(lights[i].pos.y).put(lights[i].pos.z);

				um_light_param.
						              put(lights[i].color.x).
						              put(lights[i].color.y).
						              put(lights[i].color.z).
						              put((float) Math.pow(lights[i].radius, 2));

			}
		}

		um_light_pos.flip();
		um_light_param.flip();
	}

	static public void setBlendType(Const.BLEND_TYPE v)
	{
//        GL11.glDisable(GL11.GL_BLEND);
		if (blend_type != v)
		{
			if (blend_type == Const.BLEND_TYPE.btNone)
			{
				GL11.glEnable(GL11.GL_BLEND);
			}
			blend_type = v;
			switch (v)
			{
				case btNormal:
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					break;
				case btAdd:
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
					break;
				case btMult:
					GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
					break;
				default:
					GL11.glDisable(GL11.GL_BLEND);
			}
		}
	}

	static public void setAlphaTest(int v)
	{
		if (alpha_test != v)
		{
			alpha_test = v;
			if (v > 0)
			{
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glAlphaFunc(GL11.GL_GREATER, (float) v / 255);
			}
			else
			{
				GL11.glDisable(GL11.GL_ALPHA_TEST);
			}
		}
	}

	static public void setDepthTest(boolean v)
	{
		if (depth_test != v)
		{
			depth_test = v;
			if (v)
			{
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			else
			{
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			}
		}
	}

	static public void setDepthWrite(boolean v)
	{
		if (depth_write != v)
		{
			depth_write = v;
			GL11.glDepthMask(v);
		}
	}

	static public void setCullFace(Const.CULL_FACE v)
	{
		if (cull_face != v)
		{
			if (cull_face == Const.CULL_FACE.cfNone)
			{
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
			cull_face = v;
			switch (v)
			{
				case cfFront:
					GL11.glCullFace(GL11.GL_FRONT);
					break;
				case cfBack:
					GL11.glCullFace(GL11.GL_BACK);
					break;
				default:
					GL11.glDisable(GL11.GL_CULL_FACE);
			}
		}
	}

	static public void update()
	{
		frame_flag = !frame_flag;
		time = System.currentTimeMillis();
	}

	static public void setViewport(Rect vp)
	{
		viewport = vp;
		GL11.glViewport(vp.left, vp.top, vp.right - vp.left, vp.bottom - vp.top);
	}

	static public Rect getViewport()
	{
		return viewport.clone();
	}

	static public void Clear(boolean color, boolean depth)
	{
		setDepthWrite(true);
		int mask = 0;
		if (color) mask += GL11.GL_COLOR_BUFFER_BIT;
		if (depth) mask += GL11.GL_DEPTH_BUFFER_BIT;
		GL11.glClear(mask);
	}

	static public void ResetBind()
	{
		ResManager.Active.clear();

		if (!multi_texture)
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			// GL11.glBindTexture(, 0); // cube map
		}
		else
		{
			for (int i = 0; i < 16; i++)
			{
				ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB + i);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
				// GL11.glBindTexture(, 0); // cube map
			}
			ARBMultitexture.glActiveTextureARB(ARBMultitexture.GL_TEXTURE0_ARB);
		}

		ARBShaderObjects.glUseProgramObjectARB(0);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
		ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);

		// slick unbind textures
		// TODO  TextureImpl.unbind
//		TextureImpl.unbind();
	}

	static public void set2D(int width, int height)
	{
		ResetBind();
		Clear(false, true);
		setBlendType(Const.BLEND_TYPE.btNormal);
		setDepthTest(false);
		setCullFace(Const.CULL_FACE.cfNone);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);

		setDepthTest(false);

		GL11.glViewport(0, 0, width, height);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, height, 0, -1, 1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
}
