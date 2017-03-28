package com.a2client.g3d;

import com.a2client.g3d.math.DualQuat;
import com.a2client.g3d.math.Quat;

/**
 * Created by arksu on 20.03.17.
 */
public class SkeletonData
{
	/**
	 * дефолтная кость для эквипа без скелета (единичная матрица)
	 */
	public static final SkeletonData defaultEquipBone;

	static
	{
		Joint[] joints = new Joint[1];
		joints[0] = new Joint(
				"root",
				new DualQuat(new Quat(0.70710677f, 0, 0, 0.70710677f), new Quat(0, 0, 0, 0)),
				new DualQuat(new Quat(-0.70710677f, 0, 0, 0.70710677f), new Quat(0, 0, 0, 0))
		);
		defaultEquipBone = new SkeletonData(joints);
	}

	/**
	 * кости описывающие скелет (бинд позу)
	 */
	private final Joint[] _joints;

	public SkeletonData(Joint[] joints)
	{
		_joints = joints;
	}

	public Joint[] getJoints()
	{
		return _joints;
	}

	public int getJointsCount()
	{
		return _joints.length;
	}
}
