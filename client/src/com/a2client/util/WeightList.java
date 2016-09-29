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
	private final List<T> list;
	private final List<Integer> weights;
	private int totalWeight = 0;

	public WeightList()
	{
		list = new ArrayList<>();
		weights = new ArrayList<>();
	}

	public void add(T c, int w)
	{
		this.list.add(c);
		this.weights.add(w);
		totalWeight += w;
	}

	private T pick(int p)
	{
		if (totalWeight == 0)
		{
			return null;
		}
		p %= totalWeight;
		int i = 0;
		while (true)
		{
			if ((p -= weights.get(i)) <= 0)
			{
				break;
			}
			i++;
		}
		return (list.get(i));
	}

	public T pick_first()
	{
		return list.get(0);
	}

	public T pick(Random gen)
	{
		return (pick(gen.nextInt(totalWeight)));
	}

	public int size()
	{
		return (list.size());
	}
}
