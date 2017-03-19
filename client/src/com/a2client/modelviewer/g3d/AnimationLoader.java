package com.a2client.modelviewer.g3d;

import com.a2client.corex.MyInputStream;
import com.a2client.modelviewer.g3d.math.DualQuat;
import com.a2client.modelviewer.g3d.math.Mat4f;
import com.a2client.modelviewer.g3d.math.Quat;
import com.a2client.modelviewer.g3d.math.Vec3f;

import java.io.IOException;

/**
 * загрузка анимации из потока
 * Created by arksu on 19.03.17.
 */
public class AnimationLoader
{
	public static AnimationData load(MyInputStream in, Skeleton skeleton) throws IOException
	{
		int jointsCount = skeleton.getJointsCount();
		int framesCount = in.readWord();
		int fps = in.readWord();
		DualQuat[][] frames = new DualQuat[framesCount][jointsCount];

		for (int i = 0; i < framesCount; i++)
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
				frames[i][index] = new DualQuat(rot, pos);
			}
		}
		return new AnimationData(frames, framesCount, fps, skeleton);
	}
}
