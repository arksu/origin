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

public class Rect
{
    public int x;
    public int y;
    public int w;
    public int h;

    public Rect(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Rect(Vec2i ul, Vec2i sz)
    {
        this.x = ul.x;
        this.y = ul.y;
        this.w = sz.x;
        this.h = sz.y;
    }

    public Rect(int x, int y, Vec2i size)
    {
        this.x = x;
        this.y = y;
        this.w = size.x;
        this.h = size.y;
    }

    public Rect(Rect r)
    {
        this.x = r.x;
        this.y = r.y;
        this.w = r.w;
        this.h = r.h;
    }

    public Rect Union(Vec2i ul, Vec2i sz)
    {
        int nx = Math.min(x, ul.x);
        int ny = Math.min(y, ul.y);
        return new Rect(nx, ny, Math.max(x + w, ul.x + sz.x) - nx, Math.max(y + h, ul.y + sz.y) - ny);
    }

    public boolean is_intersect(Rect r)
    {
        return ((((x > r.x) && (x <= r.x + r.w)) || ((x + w > r.x) && (x + w <= r.x + r.w)) ||
                ((r.x > x) && (r.x <= x + w)) || ((r.x + r.w > x) && (r.x + r.w <= x + w))) && (((y > r.y) && (y <= r.y + r.h)) || ((y + h > r.y) && (y + h <= r.y + r.h)) ||
                ((r.y > y) && (r.y <= y + h)) || ((r.y + r.h > y) && (r.y + r.h <= y + h))));
    }

    public boolean is_inside(Rect r)
    {
        return ((((x >= r.x) && (x <= r.x + r.w)) || ((x + w >= r.x) && (x + w <= r.x + r.w)) ||
                ((r.x >= x) && (r.x <= x + w)) || ((r.x + r.w >= x) && (r.x + r.w <= x + w))) && (((y >= r.y) && (y <= r.y + r.h)) || ((y + h >= r.y) && (y + h <= r.y + r.h)) ||
                ((r.y >= y) && (r.y <= y + h)) || ((r.y + r.h >= y) && (r.y + r.h <= y + h))));
    }

    public boolean is_intersect(int rx, int ry, int rw, int rh)
    {
        return ((((x > rx) && (x <= rx + rw)) || ((x + w > rx) && (x + w <= rx + rw)) ||
                ((rx > x) && (rx <= x + w)) || ((rx + rw > x) && (rx + rw <= x + w))) && (((y > ry) && (y <= ry + rh)) || ((y + h > ry) && (y + h <= ry + rh)) ||
                ((ry > y) && (ry <= y + h)) || ((ry + rh > y) && (ry + rh <= y + h))));
    }


    public String toString()
    {
        return "x=" + x + " y=" + y + " w=" + w + " h=" + h;
    }

    public boolean PointInRect(Vec2i p)
    {
        return (p.x >= x && p.x < x + w && p.y >= y && p.y < y + h);
    }

    public int Right()
    {
        return x + w;
    }

    public int Bottom()
    {
        return y + h;
    }

    public void SetRight(int a)
    {
        w = a - x;
    }

    public void SetBottom(int a)
    {
        h = a - y;
    }

}
