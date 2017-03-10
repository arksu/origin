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

import com.a2client.utils3d.CharacterActions;

public class Anim
{
	public enum LOOP_MODE
	{
		lmNone, lmLast, lmRepeat, lmPingPong
	}

	public String name;

	/**
	 * имя действия под которым запущена анимация
	 */
	public String ActionName;

	public CharacterActions.AnimAction animAction;

	Skeleton skeleton;
	float BlendWeight;
	float BlendTime;
	boolean is_reverse = false;
	LOOP_MODE LoopMode = LOOP_MODE.lmNone;
	float StartWeight;
	long StartTime;
	public int FramePrev;
	public int FrameStart;
	public int FrameNext;
	float FrameDelta;
	boolean[] state;

	/**
	 * режим смешивания анимации с другими на скелете
	 * т.н. мержинг анимаций (смешивание)
	 */
	public boolean is_merge = false;

	public AnimData data;
	float weight = 0;
	DualQuat[] joint;
	int[] map;
	boolean is_ended = false;

	public Anim(String name, Skeleton skeleton, CharacterActions.AnimAction action)
	{
		this.name = name;
		this.skeleton = skeleton;
		data = AnimData.load(name);
		state = new boolean[data.jcount];
		joint = new DualQuat[data.jcount];
		map = new int[skeleton.data.jcount];
		this.is_merge = action.is_merge;
		this.animAction = action;

		// joint indices remapping
		for (int i = 0; i < skeleton.data.jcount; i++)
		{
			map[i] = -1; // not animated
		}
		for (int i = 0; i < data.jcount; i++)
		{
			int j = skeleton.JointIndex(data.jname[i]);
			if (j > -1)
			{
				if (is_merge)
				{
					int fc = 1;
					DualQuat dq = data.frame[i][0];
					boolean b1 = dq.equals(data.frame[i][fc]);
					while (b1 && fc < data.count)
					{
						fc++;
					}
					if (fc < data.count)
					{
						map[j] = i; // animated joint
					}
//                else
//                    Log.debug("not animated!");
				}
				else
				{
					map[j] = i; // animated joint
				}
			}
		}
	}

	/**
	 * каллбак для конца анимации
	 */
	protected void onEnd() { } // abstract

	private void doEnd()
	{
		if (!is_ended)
		{
			is_ended = true;
			onEnd();
		}
	}

	public void update()
	{
		float dt = (float) (Render.time - StartTime);

		FrameDelta = utils.frac(dt * data.fps / 1000);
		FramePrev = (int) (Render.time - StartTime) * data.fps / 1000;

		if (is_reverse)
		{
			FramePrev = FrameStart - FramePrev;
		}
		else
		{
			FramePrev = FrameStart + FramePrev;
		}

		if (FramePrev >= data.count - 1 || (isBack() && FramePrev <= 0))
		{
			doEnd();
		}

		switch (LoopMode)
		{
			case lmNone:
				if (FramePrev >= data.count - 1)
				{
					FrameNext = 0;
					FrameDelta = 0;
				}
				else
				{
					FrameNext = FramePrev + 1;
				}
				break;
			case lmRepeat:
				if (isBack())
				{
					FrameNext = (FramePrev - 1) % data.count;
				}
				else
				{
					FrameNext = (FramePrev + 1) % data.count;
				}
				break;
			case lmLast:
				FramePrev = utils.min(FramePrev, data.count - 1);
				FrameNext = utils.min(FramePrev + 1, data.count - 1);
				break;
			case lmPingPong:
				if ((FramePrev / (data.count - 1)) % 2 == 0)
				{
					// по нарастающей. от 0 до data.count
					FramePrev = FramePrev % (data.count - 1);
					FrameNext = FramePrev + 1;
				}
				else
				{
					// обратный ход. от data.count до 0
					FramePrev = data.count - ((FramePrev) % (data.count - 1)) - 1;
					FrameNext = FramePrev - 1;
				}
				break;
		}
		FramePrev = FramePrev % data.count;

		FrameNext = utils.max(FrameNext, 0);
		FramePrev = utils.max(FramePrev, 0);

		if ((Math.abs(weight - BlendWeight) > Const.EPS) && (BlendTime > Const.EPS))
		{
			weight = utils.lerp(StartWeight, BlendWeight, utils.min(1f, (dt) * 0.001f / BlendTime));
		}
		else
		{
			weight = BlendWeight;
		}

//        Display.setTitle("prev=" + FramePrev + " next=" + FrameNext);
//        if (name.equals("player/animate_handup"))
//        Log.debug("prev=" + FramePrev + " next=" + FrameNext + " w="+weight+" BlendWeight="+BlendWeight);
	}

	protected boolean isBack()
	{
		return is_reverse;
	}

	public boolean isStopped()
	{
		return (isBack() && FramePrev == 0 && FrameNext == 0);
	}

	public void lerpJoint(int idx)
	{
		if (state[idx] != Render.frame_flag)
		{
			joint[idx] = data.frame[idx][FramePrev].lerp(data.frame[idx][FrameNext], FrameDelta);
			state[idx] = Render.frame_flag;
		}
	}

	public void play(float blendWeight, float blendTime, LOOP_MODE loop_mode)
	{
		this.BlendWeight = blendWeight;
		this.BlendTime = blendTime;
		this.LoopMode = loop_mode;
		this.StartWeight = weight;
		this.StartTime = Render.time;
		this.FrameStart = 0;
		is_reverse = false;
		reset_state();
	}

	public void play_repeat(float blendWeight, float blendTime, LOOP_MODE loop_mode)
	{
		this.BlendWeight = blendWeight;
		this.BlendTime = blendTime;
		this.LoopMode = loop_mode;
		this.StartWeight = weight;
		this.StartTime = Render.time;
		this.FrameStart = FramePrev;
		is_reverse = false;
		reset_state();
	}

	public void stop(float blendTime)
	{
		play(0, blendTime, LoopMode);
		FrameStart = FramePrev;
		is_reverse = true;
	}

	public Anim clone()
	{
		Anim a = new Anim(this.name, this.skeleton, this.animAction);
		return a;
	}

	public void reset_state()
	{
		for (int i = 0; i < data.jcount; i++)
		{
			state[i] = !Render.frame_flag;
		}
	}

}
