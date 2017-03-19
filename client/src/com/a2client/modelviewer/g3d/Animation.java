package com.a2client.modelviewer.g3d;

import com.a2client.modelviewer.g3d.math.DualQuat;
import com.a2client.util.Utils;

import java.io.IOException;

/**
 * анимации скелета
 * Created by arksu on 19.03.17.
 */
public class Animation
{
	/**
	 * кадры анимации и другое
	 */
	private final AnimationData _data;

	/**
	 * расчитанные позиции костей для текущего кадра анимации (в настоящий момент времени)
	 */
	public DualQuat[] joint;

	public int FrameStart;

	/**
	 * предыщущий кадр
	 */
	public int FramePrev;

	/**
	 * следующий кадр
	 */
	public int FrameNext;

	/**
	 * сколько прошло между предыдущим кадром и следующим (интерполяция кадров)
	 */
	public float FrameDelta;

	/**
	 * время начала анимации (тик)
	 */
	long StartTime;

	public static boolean LERP = true;

	public Animation(AnimationData data) throws IOException
	{
		_data = data;
		joint = new DualQuat[_data.getSkeleton().getJointsCount()];
	}

	public void lerpJoint(int idx)
	{
//		if (state[idx] != Render.frame_flag)
//		{
		if (LERP)
		{
			if (_data.getFrames()[FramePrev][idx] != null && _data.getFrames()[FrameNext][idx] != null)
			{
				joint[idx] = _data.getFrames()[FramePrev][idx].lerp(_data.getFrames()[FrameNext][idx], FrameDelta);
			}
			else
			{
				joint[idx] = null;
			}
		}
		else
		{
			joint[idx] = _data.getFrames()[FramePrev][idx];//.lerp(_data.getFrames()[FrameNext][idx], FrameDelta);
		}
//			state[idx] = Render.frame_flag;
//		}
	}

	public void update()
	{
		float dt = (float) (System.currentTimeMillis() - StartTime);

		FrameDelta = Utils.frac(dt * _data.getFps() / 1000);
		FramePrev = (int) (System.currentTimeMillis() - StartTime) * _data.getFps() / 1000;

		boolean is_reverse = false;
		if (is_reverse)
		{
			FramePrev = FrameStart - FramePrev;
		}
		else
		{
			FramePrev = FrameStart + FramePrev;
		}

		FrameNext = (FramePrev + 1) % _data.getFramesCount();

		FramePrev = FramePrev % _data.getFramesCount();

		FrameNext = Utils.max(FrameNext, 0);
		FramePrev = Utils.max(FramePrev, 0);

	}

	public void play()
	{
		StartTime = System.currentTimeMillis();
		FrameStart = 0;
	}
}
