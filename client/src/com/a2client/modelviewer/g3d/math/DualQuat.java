package com.a2client.modelviewer.g3d.math;

import com.a2client.corex.MyInputStream;
import com.badlogic.gdx.math.Vector3;

public class DualQuat
{
	public Quat real, dual;

	public DualQuat(Quat r, Quat d)
	{
		this.real = r;
		this.dual = d;
	}

	public DualQuat(MyInputStream in)
	{
		this.real = new Quat(in);
		this.dual = new Quat(in);
	}

	public DualQuat(Quat qrot, Vector3 qpos)
	{
		this.real = qrot.clone();
		this.dual = new Quat(qpos.x, qpos.y, qpos.z, 0).mul(qrot.mul(0.5f));
	}

	public DualQuat lerp(DualQuat dq, float t)
	{
//        if (real == null || dq == null || dq.real == null) {
//            System.out.print("222");
//        }
		if (real.dot(dq.real) < 0)
		{
			return new DualQuat(real.sub(dq.real.add(real).mul(t)), dual.sub(dq.dual.add(dual).mul(t)));
		}
		else
		{
			return new DualQuat(real.add(dq.real.sub(real).mul(t)), dual.add(dq.dual.sub(dual).mul(t)));
		}
	}

	public Vector3 pos()
	{
		return new Vector3(
				2 * (dual.x * real.w - real.x * dual.w + real.y * dual.z - real.z * dual.y),
				2 * (dual.y * real.w - real.y * dual.w + real.z * dual.x - real.x * dual.z),
				2 * (dual.z * real.w - real.z * dual.w + real.x * dual.y - real.y * dual.x)
		);
	}

	public DualQuat mul(DualQuat q)
	{
		return new DualQuat(this.real.mul(q.real), this.real.mul(q.dual).add(this.dual.mul(q.real)));
	}

	public String toString()
	{
		return "(" + real.toString() + ", " + dual.toString() + ")";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof DualQuat)
		{
			DualQuat dq = (DualQuat) obj;
			boolean b1 = this.real.equals(dq.real);
			boolean b2 = this.dual.equals(dq.dual);
			return b1 && b2;
		}
		return false;
	}
}
