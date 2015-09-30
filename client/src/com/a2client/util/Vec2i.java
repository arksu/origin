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

package com.a2client.util;

import com.badlogic.gdx.math.Vector2;

public class Vec2i
{
	public int x, y;

	static public final Vec2i z = new Vec2i();

	public Vec2i()
	{
		this.x = 0;
		this.y = 0;
	}

	public Vec2i(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Vec2i(int a)
	{
		this.x = a;
		this.y = a;
	}

	public Vec2i(Vec2i c)
	{
		this(c.x, c.y);
	}

	public Vec2i clone()
	{
		return new Vec2i(this);
	}

	public static Vec2i sc(double a, double r)
	{
		return (new Vec2i((int) (Math.cos(a) * r), -(int) (Math.sin(a) * r)));
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof Vec2i))
		{
			return (false);
		}
		Vec2i c = (Vec2i) o;
		return ((c.x == x) && (c.y == y));
	}

	public int compareTo(Vec2i c)
	{
		if (c.y != y)
		{
			return (c.y - y);
		}
		if (c.x != x)
		{
			return (c.x - x);
		}
		return (0);
	}

	public Vec2i add(int ax, int ay)
	{
		return (new Vec2i(x + ax, y + ay));
	}

	public Vec2i add(int a)
	{
		return (new Vec2i(x + a, y + a));
	}

	public Vec2i add(Vec2i b)
	{
		return (add(b.x, b.y));
	}

	public Vec2i sub(int ax, int ay)
	{
		return (new Vec2i(x - ax, y - ay));
	}

	public Vec2i sub(int v)
	{
		return (new Vec2i(x - v, y - v));
	}

	public Vec2i sub(Vec2i b)
	{
		return (sub(b.x, b.y));
	}

	public Vec2i mul(int f)
	{
		return (new Vec2i(x * f, y * f));
	}

	public Vec2i mul(double f)
	{
		return (new Vec2i((int) (x * f), (int) (y * f)));
	}

	public Vec2i mul(double fx, double fy)
	{
		return (new Vec2i((int) (x * fx), (int) (y * fy)));
	}

	public Vec2i inverse()
	{
		return (new Vec2i(-x, -y));
	}

	public Vec2i mul(Vec2i f)
	{
		return (new Vec2i(x * f.x, y * f.y));
	}

	public Vec2i div(Vec2i d)
	{
		int v, w;

		v = ((x < 0) ? (x + 1) : x) / d.x;
		w = ((y < 0) ? (y + 1) : y) / d.y;
		if (x < 0)
		{
			v--;
		}
		if (y < 0)
		{
			w--;
		}
		return (new Vec2i(v, w));
	}

	public Vec2i div(int d)
	{
		return (div(new Vec2i(d, d)));
	}

	public Vec2i mod(Vec2i d)
	{
		int v, w;

		v = x % d.x;
		w = y % d.y;
		if (v < 0)
		{
			v += d.x;
		}
		if (w < 0)
		{
			w += d.y;
		}
		return (new Vec2i(v, w));
	}

	public static Vec2i abs(Vec2i c)
	{
		return new Vec2i(Math.abs(c.x), Math.abs(c.y));
	}

	public Vec2i mod(int d)
	{
		int v, w;

		v = x % d;
		w = y % d;
		if (v < 0)
		{
			v += d;
		}
		if (w < 0)
		{
			w += d;
		}
		return (new Vec2i(v, w));
	}

	// находится ли внутри прямоугольника. c - начальные координаты. s - размер
	public boolean in_rect(Vec2i c, Vec2i s)
	{
		return ((x >= c.x) && (y >= c.y) && (x < c.x + s.x) && (y < c.y + s.y));
	}

	public String toString()
	{
		return ("(" + x + ", " + y + ")");
	}

	public int direction(Vec2i to_point)
	{
		Vec2i vector = to_point.sub(this);
		if (vector.x == 0 && vector.y == 0)
		{
			return 0;
		}
		double a = Math.atan2(vector.y, vector.x);
		if (a < 0)
		{
			a = Math.PI + Math.PI + a;
		}
		return (int) Math.round(a / (Math.PI / 4) + 6) % 8;
	}

	public float directionf(Vec2i to_point)
	{
		Vec2i vector = to_point.sub(this);
		if (vector.x == 0 && vector.y == 0)
		{
			return 0;
		}
		return (float) Math.atan2(vector.x, vector.y);
	}

	public double dist(Vec2i o)
	{
		long dx = o.x - x;
		long dy = o.y - y;
		return (Math.sqrt((dx * dx) + (dy * dy)));
	}

	public int area()
	{
		return x * y;
	}

	public Vector2 getVector2()
	{
		return new Vector2(x, y);
	}

}
