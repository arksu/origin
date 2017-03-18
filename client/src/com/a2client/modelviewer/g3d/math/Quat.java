package com.a2client.modelviewer.g3d.math;

import com.a2client.corex.MyInputStream;
import com.a2client.modelviewer.g3d.Const;
import com.badlogic.gdx.math.Quaternion;

import java.io.IOException;

public class Quat
{
	public float x, y, z, w;

	public Quat(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Quat(Quaternion quaternion)
	{
		this.x = quaternion.x;
		this.y = quaternion.y;
		this.z = quaternion.z;
		this.w = quaternion.w;
	}

	public Quat(MyInputStream in)
	{
		try
		{
			this.x = in.readFloat();
			this.y = in.readFloat();
			this.z = in.readFloat();
			this.w = in.readFloat();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Quat clone()
	{
		return new Quat(this.x, this.y, this.z, this.w);
	}

	public boolean equals(Quat q)
	{
		return (
				(Math.abs(this.x - q.x) <= Const.EPS) &&
				(Math.abs(this.y - q.y) <= Const.EPS) &&
				(Math.abs(this.z - q.z) <= Const.EPS) &&
				(Math.abs(this.w - q.w) <= Const.EPS)
		);
	}

	public Quat sub(Quat q)
	{
		return new Quat(
				this.x - q.x,
				this.y - q.y,
				this.z - q.z,
				this.w - q.w
		);
	}

	public Quat add(Quat q)
	{
		return new Quat(
				this.x + q.x,
				this.y + q.y,
				this.z + q.z,
				this.w + q.w
		);
	}

	public Quat mul(Quat q)
	{
		return new Quat(
				this.w * q.x + this.x * q.w + this.y * q.z - this.z * q.y,
				this.w * q.y + this.y * q.w + this.z * q.x - this.x * q.z,
				this.w * q.z + this.z * q.w + this.x * q.y - this.y * q.x,
				this.w * q.w - this.x * q.x - this.y * q.y - this.z * q.z
		);
	}

	public Quat mul(float t)
	{
		return new Quat(this.x * t, this.y * t, this.z * t, this.w * t);
	}

//	public Vec3f mul(Vec3f v)
//	{
//		//with q * Quat(v.x, v.y, v.z, 0) * q.Invert do
//		Quat q = mul(new Quat(v.x, v.y, v.z, 0)).mul(invert());
//		return new Vec3f(q.x, q.y, q.z);
//	}

	public Quat invert()
	{
		return new Quat(-x, -y, -z, w);
	}

	public Quat lerp(Quat q, float t)
	{
		if (dot(q) < 0)
		{
			return sub(add(q).mul(t));
		}
		else
		{
			return add(sub(q).mul(t));
		}
	}

	public float dot(Quat q)
	{
		return x * q.x + y * q.y + z * q.z + w * q.w;
	}

	public Quat normal()
	{
		float Len = (float) Math.sqrt(x * x + y * y + z * z + w * w);
		if (Len > 0)
		{
			Len = 1 / Len;
			return new Quat(x * Len, y * Len, z * Len, w * Len);
		}
		else
		{
			return new Quat(0, 0, 0, 0);
		}
	}

	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
}
