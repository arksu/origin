package com.a2client.g3d;

import com.a2client.g3d.math.DualQuat;
import com.a2client.g3d.math.Mat4f;
import com.a2client.g3d.math.Vec3f;

import java.io.IOException;

/**
 * кость в скелете
 * Created by arksu on 17.03.17.
 */
public class Joint
{
	/**
	 * имя кости
	 */
	private final String _name;

	/**
	 * имя родительской кости
	 */
	private final String _parentName;

	/**
	 * индекс родительской кости или -1 если нет родителя (корневая кость)
	 */
	private int _parentIndex;

	/**
	 * исходная поза
	 */
	private final DualQuat _bind;

	/**
	 * world transform
	 */
	private final DualQuat _frame;

	public Joint(MyInputStream in) throws IOException
	{
		_name = in.readAnsiString();
		_parentName = in.readAnsiString();

		Mat4f bind = in.readBlenderMat4f();
		Mat4f frame = in.readBlenderMat4f();

		Vec3f pos = bind.getPos();
		_bind = new DualQuat(bind.getRot(), pos);
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

	@Override
	public String toString()
	{
		return "(" + _name + " -> " + _parentName + " [" + _parentIndex + "])";
	}
}
