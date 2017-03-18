package com.a2client.modelviewer.g3d;

import com.a2client.corex.MyInputStream;
import com.a2client.modelviewer.g3d.math.DualQuat;
import com.a2client.modelviewer.g3d.math.Quat;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.io.IOException;

/**
 * Created by arksu on 17.03.17.
 */
public class Joint
{
	private final String _name;
	private final String _parentName;
	private final DualQuat _bind;
	private final DualQuat _frame;

	public Joint(MyInputStream in) throws IOException
	{
		_name = in.readAnsiString();
		_parentName = in.readAnsiString();
		Matrix4 bind = in.readMatrix();
		Matrix4 frame = in.readMatrix();

		Vector3 p = new Vector3();
		Quaternion q = new Quaternion();

		q.setFromMatrix(bind);
		bind.getTranslation(p);
		_bind = new DualQuat(new Quat(q), p);

		q.setFromMatrix(frame);
		frame.getTranslation(p);
		_frame = new DualQuat(new Quat(q), p);
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
