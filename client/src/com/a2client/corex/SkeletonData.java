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

public class SkeletonData extends ResObject
{
	public int jcount;
	public String[] jname;
	public Joint[] base;

	static public SkeletonData load(String name)
	{
		ResObject r = ResManager.Get(name + Const.EXT_SKELETON);
		if (r != null && r instanceof SkeletonData)
		{
			return (SkeletonData) r;
		}

		SkeletonData a = new SkeletonData(name + Const.EXT_SKELETON);
		ResManager.Add(a);
		return a;
	}

	public SkeletonData(String name)
	{
		this.name = name;
		try
		{
			MyInputStream in = FileSys.getStream(name);
			jcount = in.readWord();
			jname = new String[jcount];
			base = new Joint[jcount];

			for (int i = 0; i < jcount; i++)
			{
				jname[i] = in.readAnsiString();
			}
			for (int i = 0; i < jcount; i++)
			{
				base[i] = new Joint(in);
			}
			Log.debug("load skeleton: " + name);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public int getIdx(String aname)
	{
		for (int i = 0; i < jname.length; i++)
		{
			if (jname[i].equals(aname))
			{
				return i;
			}
		}
		return -1;
	}
}
