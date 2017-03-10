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

public class Vec4f
{
	public float x;
	public float y;
	public float z;
	public float w;

	public Vec4f()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.w = 0;
	}

	public Vec4f(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vec4f(MyInputStream in)
	{
		try
		{
			this.x = in.readFloat();
			this.y = in.readFloat();
			this.z = in.readFloat();
			this.w = in.readFloat();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.x = 0;
			this.y = 0;
			this.z = 0;
			this.w = 0;
		}
	}

	public Vec4f clone()
	{
		return new Vec4f(this.x, this.y, this.z, this.w);
	}

	public float dot(Vec3f v)
	{
		return x * v.x + y * v.y + z * v.z + w;
	}
}
