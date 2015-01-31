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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class WeightList<T> implements java.io.Serializable
{
    List<T> c;
    List<Integer> w;
    int tw = 0;

    public WeightList()
    {
        c = new ArrayList<T>();
        w = new ArrayList<Integer>();
    }

    public void add(T c, int w)
    {
        this.c.add(c);
        this.w.add(w);
        tw += w;
    }

    public T pick(int p)
    {
        if (tw == 0)
            return null;
        p %= tw;
        int i = 0;
        while (true)
        {
            if ((p -= w.get(i)) <= 0)
                break;
            i++;
        }
        return (c.get(i));
    }

    public T pick_first()
    {
        return c.get(0);
    }

    public T pick(Random gen)
    {
        return (pick(gen.nextInt(tw)));
    }

    public int size()
    {
        return (c.size());
    }
}
