package com.a2client.modelviewer.g3d;

/**
 * Created by arksu on 20.03.17.
 */
public class SkeletonData
{
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
