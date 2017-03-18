package com.a2client.modelviewer.g3d;

import com.a2client.corex.MyInputStream;
import com.badlogic.gdx.math.Matrix4;

import java.io.IOException;

/**
 * Created by arksu on 17.03.17.
 */
public class SkeletonLoader
{
	public static Skeleton load(MyInputStream in) throws IOException
	{
		Skeleton skeleton = new Skeleton();

		int bonesCount = in.readWord();
		while (bonesCount > 0)
		{
			// skip flag
			if (in.readByte() == 0) continue;

			String boneName = in.readAnsiString();
			String parentName = in.readAnsiString();
			Matrix4 ml = in.readMatrix();
			Matrix4 mw = in.readMatrix();

			bonesCount--;
		}

		return skeleton;
	}
}
