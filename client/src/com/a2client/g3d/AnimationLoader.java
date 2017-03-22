package com.a2client.g3d;

import com.a2client.g3d.math.DualQuat;
import com.a2client.g3d.math.Mat4f;
import com.a2client.g3d.math.Quat;
import com.a2client.g3d.math.Vec3f;

import java.io.IOException;

/**
 * загрузка анимации из потока
 * Created by arksu on 19.03.17.
 */
public class AnimationLoader
{
	public static AnimationData load(MyInputStream in, SkeletonData skeleton) throws IOException
	{
		String name = in.readAnsiString();
		int jointsCount = skeleton.getJointsCount();
		int framesCount = in.readWord();
		int fps = in.readWord();
		DualQuat[][] frames = new DualQuat[framesCount][jointsCount];

		for (int i = 0; i < framesCount; i++)
		{
			int jointDiffCounter = 0;
			for (int j = 0; j < jointsCount; j++)
			{
				// flag no diff in transform
				if (in.readByte() == 0)
				{
//					frames[i][j] = new DualQuat(new Quat(0, 0, 0, 1f), new Vec3f(0, 0, 0));
					continue;
				}
				jointDiffCounter++;
				int index = in.readWord();
				if (index >= jointsCount)
				{
					throw new RuntimeException("wrong index " + index);
				}
				Mat4f m = in.readBlenderMat4f();
				Quat rot = m.getRot();
				if (rot.w < 0)
				{
					rot = rot.mul(-1f);
				}
				Vec3f pos = m.getPos();
				frames[i][j] = new DualQuat(rot, pos);
			}
//			System.out.println("frame [" + i + "] jointDiffCounter = " + jointDiffCounter);
		}
		return new AnimationData(frames, framesCount, fps, name, skeleton);
	}
}
