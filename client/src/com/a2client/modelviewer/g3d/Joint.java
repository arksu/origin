package com.a2client.modelviewer.g3d;

import com.a2client.corex.MyInputStream;
import com.a2client.modelviewer.g3d.math.DualQuat;
import com.a2client.modelviewer.g3d.math.Mat4f;
import com.a2client.modelviewer.g3d.math.Vec3f;

import java.io.IOException;

/**
 * Created by arksu on 17.03.17.
 */
public class Joint
{
	private final String _name;
	private final String _parentName;
	private int _parentIndex;
	private final DualQuat _bind;
	private final DualQuat _frame;

	public Joint(MyInputStream in) throws IOException
	{
		_name = in.readAnsiString();
		_parentName = in.readAnsiString();

		Mat4f bind = in.readMat4f();
		Mat4f frame = in.readMat4f();

		bind = bind.transpose();
		frame = frame.transpose();


//		Matrix4 bind = in.readMatrix();
//		Matrix4 frame = in.readMatrix();

//		bind.tra();
//		frame.tra();

//		Vector3 p = new Vector3();
//		Quaternion q = new Quaternion();

//		q.setFromMatrix(true, bind);
//		bind.getTranslation(p);
		Vec3f pos = bind.getPos();
		_bind = new DualQuat(bind.getRot(), pos);

//		q.setFromMatrix(true, frame);
//		frame.getTranslation(p);
		Vec3f pos1 = frame.getPos();
		_frame = new DualQuat(frame.getRot(), pos1);
	}

	public int getParentIndex()
	{
		return _parentIndex;
	}

	public void setParentIndex(int parentIndex)
	{
		_parentIndex = parentIndex;
	}

	public String getParentName()
	{
		return _parentName;
	}

	public String getName()
	{
		return _name;
	}

	public DualQuat getBind()
	{
		return _bind;
	}

	public DualQuat getFrame()
	{
		return _frame;
	}
}
