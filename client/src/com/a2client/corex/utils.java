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

public class utils
{

    public static float lerp(float x, float y, float t)
    {
        return x + (y - x) * t;
    }

    public static float min(float x, float y)
    {
        return (x < y) ? x : y;
    }

    public static int min(int x, int y)
    {
        return (x < y) ? x : y;
    }

    public static int max(int x, int y)
    {
        return (x > y) ? x : y;
    }

    public static float max(float x, float y)
    {
        return (x > y) ? x : y;
    }

    public static float frac(float v)
    {
        return v - ((int) v);
    }
}
