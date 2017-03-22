package com.a2client.g3d;

import java.io.IOException;

/**
 * Created by arksu on 17.03.17.
 */
public class SkeletonLoader
{
	public static SkeletonData load(MyInputStream in) throws IOException
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

		return new SkeletonData(joints);
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
