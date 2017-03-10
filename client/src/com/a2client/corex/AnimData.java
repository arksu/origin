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

public class AnimData extends ResObject
{
	public DualQuat[][] frame;
	public int count;
	public int fps;
	public int jcount;
	public String[] jname;

	static public AnimData load(String name)
	{
		ResObject r = ResManager.Get(name + Const.EXT_ANIMATION);
		if (r != null && r instanceof AnimData)
		{
			return (AnimData) r;
		}

		AnimData a = new AnimData(name + Const.EXT_ANIMATION);
		ResManager.Add(a);
		return a;
	}

	public AnimData(String name)
	{
		this.name = name;
		try
		{
			MyInputStream in = FileSys.getStream(name);
			jcount = in.readInt();
			count = in.readInt();
			fps = in.readInt();
//                fps = 4;
			jname = new String[jcount];
			frame = new DualQuat[jcount][count];

			for (int i = 0; i < jcount; i++)
			{
				jname[i] = in.readAnsiString();
			}

//                StringBuilder sb = new StringBuilder();

			byte[] flag;
			float[] sdata = new float[count * 6];
			Quat rot;
			Vec3f pos;
			for (int i = 0; i < jcount; i++)
			{
				rot = new Quat(0, 0, 0, 1);
				pos = new Vec3f(0, 0, 0);

				// читаем флаги
				flag = new byte[count];
				for (int fc = 0; fc < count; fc++)
				{
					flag[fc] = in.readByte();
				}

				// читаем данные об анимации с привязкой к флагам
				int scount = in.readInt();
				for (int sc = 0; sc < scount; sc++)
				{
					sdata[sc] = in.readFloat();
				}
				scount = 0;

				for (int j = 0; j < count; j++)
				{
					byte f = flag[j];
					// используются только 6 бит
					if ((f & 0x01) > 0)
					{
						rot.x = sdata[scount++];
					}
					if ((f & (0x01 << 1)) > 0)
					{
						rot.y = sdata[scount++];
					}
					if ((f & (0x01 << 2)) > 0)
					{
						rot.z = sdata[scount++];
					}
					if ((f & (0x01 << 3)) > 0)
					{
						pos.x = sdata[scount++];
					}
					if ((f & (0x01 << 4)) > 0)
					{
						pos.y = sdata[scount++];
					}
					if ((f & (0x01 << 5)) > 0)
					{
						pos.z = sdata[scount++];
					}

					// w reconstruct
					if (((f & 0x01) > 0) || ((f & (0x01 << 1)) > 0) || ((f & (0x01 << 2)) > 0))
					{
						float len = 1 - rot.x * rot.x - rot.y * rot.y - rot.z * rot.z;
						if (len < Const.EPS)
						{
							rot.w = 0;
						}
						else
						{
							rot.w = (float) (Math.sqrt(len));
						}
					}

					frame[i][j] = new DualQuat(rot, pos);
//                        DualQuat dd = frame[i][j];
//                        sb.append(String.format("[%d][%d] flag: %d rot: %.2f %.2f %.2f %.2f pos: %.2f %.2f %.2f", i, j, f,
//                                rot.x, rot.y, rot.z, rot.w,
//                                pos.x, pos.y, pos.z) + '\n');
				}
			}

//                PrintWriter out = new PrintWriter("rotpos.txt");
//                out.print(sb.toString());
//                out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}