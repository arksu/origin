package com.a2client.modelviewer.g3d;

import com.a2client.corex.MyInputStream;

import java.io.IOException;

/**
 * Created by arksu on 17.03.17.
 */
public class SkeletonLoader
{
	public static Skeleton load(MyInputStream in) throws IOException
	{
		int bonesCount = in.readWord();
		Joint[] joints = new Joint[bonesCount];
		int idx = 0;
		while (bonesCount > 0)
		{
			joints[idx] = new Joint(in);
			bonesCount--;
			idx++;
		}

		for (Joint joint : joints)
		{
			joint.setParentIndex(getJointIndex(joint.getParentName(), joints));
		}

		Skeleton skeleton = new Skeleton(joints);
		return skeleton;
	}

	private static int getJointIndex(String name, Joint[] list)
	{
		for (int i = 0; i < list.length; i++)
		{
			Joint j = list[i];
			if (j.getName().equals(name))
			{
				return i;
			}
		}
		return -1;
	}
}
