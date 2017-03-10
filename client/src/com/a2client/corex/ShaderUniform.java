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

import com.a2client.Log;
import org.lwjgl.opengl.ARBShaderObjects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ShaderUniform
{
	public int ID;
	public Const.SHADER_UNIFORM_TYPE Type;
	public String Name;
	public float[] Value = new float[12];

	public void init(int ShaderID, String name, Const.SHADER_UNIFORM_TYPE uniform_type)
	{
		ID = ARBShaderObjects.glGetUniformLocationARB(ShaderID, name);
		this.Name = name;
		this.Type = uniform_type;
		for (int i = 0; i < Value.length; i++)
		{
			Value[i] = Const.NAN;
		}
	}

	public void setValue(int data)
	{
		if (ID != -1)
		{
			if (Type == Const.SHADER_UNIFORM_TYPE.utInt)
			{
				ARBShaderObjects.glUniform1iARB(ID, data);
			}
			else
			{
				Log.error("shader unifrorm set wrong val" + Type + " must be utInt");
			}
		}
	}

	public void setValue(Vec2f data)
	{
		if (ID != -1)
		{
			if (Type == Const.SHADER_UNIFORM_TYPE.utVec2)
			{
				ARBShaderObjects.glUniform2fARB(ID, data.x, data.y);
			}
			else
			{
				Log.error("shader unifrorm set wrong val" + Type + " must be utVec2");
			}
		}
	}

	public void setValue(Vec3f data)
	{
		if (ID != -1)
		{
			if (Type == Const.SHADER_UNIFORM_TYPE.utVec3)
			{
				ARBShaderObjects.glUniform3fARB(ID, data.x, data.y, data.z);
			}
			else
			{
				Log.error("shader unifrorm set wrong val" + Type + " must be utVec3");
			}
		}
	}

	public void setValue(Vec4f data)
	{
		if (ID != -1)
		{
			if (Type == Const.SHADER_UNIFORM_TYPE.utVec4)
			{
				ARBShaderObjects.glUniform4fARB(ID, data.x, data.y, data.z, data.w);
			}
			else
			{
				Log.error("shader unifrorm set wrong val" + Type + " must be utVec4");
			}
		}
	}

	public void setValue(Mat4f data)
	{
		if (ID != -1)
		{
			if (Type == Const.SHADER_UNIFORM_TYPE.utMat4)
			{
				ARBShaderObjects.glUniformMatrix4ARB(ID, false, data.getBuf());
			}
			else
			{
				Log.error("shader unifrorm set wrong val" + Type + " must be utMat4");
			}
		}
	}

	public void setValue(FloatBuffer data)
	{
		if (ID != -1)
		{
			switch (Type)
			{
				case utVec4:
					ARBShaderObjects.glUniform4ARB(ID, data);
					break;
				case utVec3:
					ARBShaderObjects.glUniform3ARB(ID, data);
					break;
				default:
					Log.error("shader unifrorm set wrong val" + Type);
			}
		}
	}

	public void setValue(DualQuat[] d)
	{
		if (ID != -1)
		{
			ByteBuffer temp = ByteBuffer.allocateDirect(d.length * 8 * 4);
			temp.order(ByteOrder.nativeOrder());
			FloatBuffer fb = temp.asFloatBuffer();

			for (DualQuat dq : d)
			{
				fb.
						  put(dq.real.x).
						  put(dq.real.y).
						  put(dq.real.z).
						  put(dq.real.w).
						  put(dq.dual.x).
						  put(dq.dual.y).
						  put(dq.dual.z).
						  put(dq.dual.w);
			}
			fb.flip();
			setValue(fb);
		}

	}

}
