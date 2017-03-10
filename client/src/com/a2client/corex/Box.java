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

/**
 * bound box
 */
public class Box
{
	public Vec3f min, max;
	static public final Box inf_box = new Box(
			new Vec3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
			new Vec3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY));

	public Box(MyInputStream in)
	{
		this.min = new Vec3f(in);
		this.max = new Vec3f(in);
	}

	public Box(Vec3f min, Vec3f max)
	{
		this.min = min;
		this.max = max;
	}

	public Box clone()
	{
		return new Box(min.clone(), max.clone());
	}
}
