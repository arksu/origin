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

public class Vec2f
{
	public float x;
	public float y;

	public static final Vec2f zero = new Vec2f(0, 0);

	public Vec2f(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public Vec2f(MyInputStream in)
	{
		try
		{
			this.x = in.readFloat();
			this.y = in.readFloat();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.x = 0;
			this.y = 0;
		}
	}
}
