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

package com.a2client.modelviewer.g3d.math;

import com.a2client.corex.Const;
import com.a2client.corex.MyInputStream;
import com.a2client.corex.utils;

import java.io.IOException;

public class Vec3f
{
	public float x, y, z;

	public static final Vec3f zero = new Vec3f();

	public Vec3f()
	{
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Vec3f(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3f clone()
	{
		return new Vec3f(this.x, this.y, this.z);
	}

	public Vec3f(MyInputStream in)
	{
		try
		{
			this.x = in.readFloat();
			this.y = in.readFloat();
			this.z = in.readFloat();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.x = 0;
			this.y = 0;
			this.z = 0;
		}
	}

	public boolean equals(Vec3f v)
	{
		return (
				(Math.abs(this.x - v.x) <= Const.EPS) &&
				(Math.abs(this.y - v.y) <= Const.EPS) &&
				(Math.abs(this.y - v.z) <= Const.EPS)
		);
	}

	public Vec3f add(Vec3f v)
	{
		return new Vec3f(this.x + v.x, this.y + v.y, this.z + v.z);
	}

	public Vec3f sub(Vec3f v)
	{
		return new Vec3f(this.x - v.x, this.y - v.y, this.z - v.z);
	}

	public Vec3f mul(Vec3f v)
	{
		return new Vec3f(this.x * v.x, this.y * v.y, this.z * v.z);
	}

	public Vec3f mul(float t)
	{
		return new Vec3f(this.x * t, this.y * t, this.z * t);
	}

	public float dot(Vec3f v)
	{
		return this.x * v.x + this.y * v.y + this.z * v.z;
	}

	public Vec3f cross(Vec3f v)
	{
		return new Vec3f(
				this.y * v.z - this.z * v.y,
				this.z * v.x - this.x * v.z,
				this.x * v.y - this.y * v.x
		);
	}

	public Vec3f reflect(Vec3f n)
	{
		return sub(n.mul(dot(n) * 2));
	}

	public float length()
	{
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public float lengthq()
	{
		return x * x + y * y + z * z;
	}

	public Vec3f normal()
	{
		float len = length();
		if (len < Const.EPS)
		{
			return new Vec3f(0, 0, 0);
		}
		else
		{
			return mul(1 / len);
		}
	}

	public float dist(Vec3f v)
	{
		return v.sub(this).length();
	}

	public float distq(Vec3f v)
	{
		return v.sub(this).lengthq();
	}

	public Vec3f min(Vec3f v)
	{
		return new Vec3f(utils.min(x, v.x), utils.min(y, v.y), utils.min(z, v.z));
	}

	public Vec3f max(Vec3f v)
	{
		return new Vec3f(utils.max(x, v.x), utils.max(y, v.y), utils.max(z, v.z));
	}

	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ")";
	}
}
