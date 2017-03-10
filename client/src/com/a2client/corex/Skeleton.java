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
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Skeleton
{
	private Skeleton parent;
	private int parent_joint;
	public boolean[] state;
	public SkeletonData data;
	public List<Anim> anims = new ArrayList<Anim>();
	public List<Anim> merge_anims = new ArrayList<Anim>();
	public DualQuat[] joint;

	public Skeleton(String name)
	{
		data = SkeletonData.load(name);
		state = new boolean[data.jcount];
		joint = new DualQuat[data.jcount];
	}

	public void addAnim(Anim anim)
	{
		if (!anim.is_merge)
		{
			anims.add(anim);
		}
		else
		{
			merge_anims.add(anim);
		}
	}

	public void update()
	{
		for (int i = 0; i < anims.size(); i++)
		{
			Anim a = anims.get(i);
			a.update();
		}

		for (int i = 0; i < merge_anims.size(); i++)
		{
			Anim a = merge_anims.get(i);
			a.update();
		}
	}

	public void setParent(Skeleton skeleton, int joint_index)
	{
		parent = skeleton;
		parent_joint = joint_index;
	}

	public void updateJoint(int idx)
	{
		if (state[idx] == Render.frame_flag)
		{
			return;
		}

		float w = 1;
		DualQuat cjoint = data.base[idx].frame;
		// идем с последней добавленной анимации и смотрим на ее вес.
		// с каждой пройденной анимацией вычитаем вес
		int ac = 0;
		for (int i = anims.size() - 1; i >= 0; i--)
		{
			if (w < Const.EPS)
			{
//                удаляем анимации уже отыгравшие свое
				Log.debug("anim remove: " + anims.get(i).name);
				anims.remove(i);
			}
			else
			{
				Anim anim = anims.get(i);
				if ((anim.map[idx] > -1))
				{
					anim.lerpJoint(anim.map[idx]);
					if (anim.joint[anim.map[idx]] != null)
					{
						cjoint = cjoint.lerp(anim.joint[anim.map[idx]], w);
					}
					w -= anim.weight;
				}
//            if (w < Const.EPS) break;
			}
			ac++;
		}
//        if (parent != null) {
//        Log.debug("anim count : "+ac);
//        }

		w = 1;
		for (int i = merge_anims.size() - 1; i >= 0; i--)
		{
			if (w < Const.EPS)
			{// || merge_anims.get(i).isStopped()) {
				// удаляем анимации уже отыгравшие свое
				Anim aa = merge_anims.get(i);
				Log.debug("merge anim remove: " + aa.name + " prev=" + aa.FramePrev + " next=" + aa.FrameNext + " start=" + aa.FrameStart);
				merge_anims.remove(i);
			}
			else
			{
				Anim anim = merge_anims.get(i);
				if ((anim.map[idx] > -1) && (anim.weight > Const.EPS))
				{
					anim.lerpJoint(anim.map[idx]);
					if (anim.joint[anim.map[idx]] != null)
					{
						cjoint = cjoint.lerp(anim.joint[anim.map[idx]], w * anim.weight);
					}
					w -= anim.weight;
				}
			}
		}

		if (data.base[idx].parent > -1)
		{
			updateJoint(data.base[idx].parent);
			joint[idx] = joint[data.base[idx].parent].mul(cjoint);
		}
		else
		{
			if (parent != null)
			{
				parent.updateJoint(parent_joint);
				if (parent.joint[parent_joint] != null)
				{
					joint[idx] = parent.joint[parent_joint].mul(cjoint);
				}
			}
			else
			{
				joint[idx] = cjoint;
			}
		}

		state[idx] = Render.frame_flag;
	}

	public int JointIndex(String JName)
	{
		for (int i = 0; i < data.jcount; i++)
		{
			if (data.jname[i].equals(JName))
			{
				return i;
			}
		}
		return -1;
	}

	public void draw_bones()
	{
		for (int i = 0; i < joint.length; i++)
		{
			updateJoint(i);
			if (data.base[i].parent != -1 && joint[i] != null)
			{
				Vec3f p1 = joint[i].pos();
				Vec3f p2 = joint[data.base[i].parent].pos();

				GL11.glBegin(GL11.GL_LINES);
				GL11.glVertex3f(p1.x, p1.y, p1.z);
				GL11.glVertex3f(p2.x, p2.y, p2.z);
				GL11.glEnd();
			}
		}
	}

	private int get_root()
	{
		for (int i = 0; i < data.base.length; i++)
		{
			if (data.base[i].parent == -1)
			{
				return i;
			}
		}
		return -1;
	}

	public void ResetState()
	{
		for (int i = 0; i < state.length; i++)
		{
			state[i] = !Render.frame_flag;
		}
		if (parent != null)
			parent.ResetState();
	}

}
