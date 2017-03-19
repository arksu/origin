package com.a2client.modelviewer.g3d;

import com.a2client.corex.MyInputStream;
import com.a2client.corex.utils;
import com.a2client.modelviewer.g3d.math.DualQuat;
import com.a2client.modelviewer.g3d.math.Mat4f;
import com.a2client.modelviewer.g3d.math.Quat;
import com.a2client.modelviewer.g3d.math.Vec3f;

import java.io.IOException;

/**
 * Created by arksu on 19.03.17.
 */
public class Animation
{
	private DualQuat[][] _frames;
	private final int _framesCount;
	private final int _fps;

	public DualQuat[] joint;
	public int FramePrev;
	public int FrameStart;
	public int FrameNext;
	public float FrameDelta;
	long StartTime;

	public static boolean LEPR = true;

	public Animation(MyInputStream in, Skeleton skeleton) throws IOException
	{
		skeleton._animation = this;
		_framesCount = in.readWord();
		_fps = in.readWord();
		int jointsCount = skeleton.getJointsCount();
		_frames = new DualQuat[_framesCount][jointsCount];

		joint = new DualQuat[jointsCount];

		for (int i = 0; i < _framesCount; i++)
		{
			for (int j = 0; j < jointsCount; j++)
			{
				int index = in.readWord();
				if (index >= jointsCount)
				{
					throw new RuntimeException("wrong index " + index);
				}
				Mat4f m = in.readMat4f();
				Quat rot = m.getRot();
				if (rot.w < 0)
				{
					rot = rot.mul(-1f);
				}
				Vec3f pos = m.getPos();
				_frames[i][index] = new DualQuat(rot, pos);

//				Matrix4 m = in.readMatrix();
//				m = m.tra();
//				Quaternion q = new Quaternion();
//				Vector3 p = new Vector3();
//				q.setFromMatrix(m);
//				m.getTranslation(p);
//				_frames[i][index] = new DualQuat(new Quat(q), p);
			}
		}
	}

	public void lerpJoint(int idx)
	{
//		if (state[idx] != Render.frame_flag)
//		{
		if (LEPR)
		{
			joint[idx] = _frames[FramePrev][idx].lerp(_frames[FrameNext][idx], FrameDelta);
		}
		else
		{
			joint[idx] = _frames[FramePrev][idx];//.lerp(_frames[FrameNext][idx], FrameDelta);
		}
//			state[idx] = Render.frame_flag;
//		}
	}

	public void update()
	{
		float dt = (float) (System.currentTimeMillis() - StartTime);

		FrameDelta = utils.frac(dt * _fps / 1000);
		FramePrev = (int) (System.currentTimeMillis() - StartTime) * _fps / 1000;

		boolean is_reverse = false;
		if (is_reverse)
		{
			FramePrev = FrameStart - FramePrev;
		}
		else
		{
			FramePrev = FrameStart + FramePrev;
		}

		FrameNext = (FramePrev + 1) % _framesCount;

		FramePrev = FramePrev % _framesCount;

		FrameNext = utils.max(FrameNext, 0);
		FramePrev = utils.max(FramePrev, 0);

	}

	public void play()
	{
		StartTime = System.currentTimeMillis();
		FrameStart = 0;
	}
}
