/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2013 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.a2client.corex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mat4f
{
	public float
			e00, e10, e20, e30,
			e01, e11, e21, e31,
			e02, e12, e22, e32,
			e03, e13, e23, e33;

	public FloatBuffer getBuf()
	{
		ByteBuffer temp = ByteBuffer.allocateDirect(64);
		temp.order(ByteOrder.nativeOrder());
		FloatBuffer buf = temp.asFloatBuffer();
		buf.
				   put(e00).put(e10).put(e20).put(e30).
				   put(e01).put(e11).put(e21).put(e31).
				   put(e02).put(e12).put(e22).put(e32).
				   put(e03).put(e13).put(e23).put(e33).
				   flip();
		return buf;
	}

	public Mat4f() { }

	public Mat4f(Mat4f m)
	{
		this.e00 = m.e00;
		this.e10 = m.e10;
		this.e20 = m.e20;
		this.e30 = m.e30;

		this.e01 = m.e01;
		this.e11 = m.e11;
		this.e21 = m.e21;
		this.e31 = m.e31;

		this.e02 = m.e02;
		this.e12 = m.e12;
		this.e22 = m.e22;
		this.e32 = m.e32;

		this.e03 = m.e03;
		this.e13 = m.e13;
		this.e23 = m.e23;
		this.e33 = m.e33;
	}

	public Mat4f(float angle, Vec3f axis)
	{
		float s = (float) Math.sin(angle);
		float c = (float) Math.cos(angle);
		float ic = 1 - c;

		float xy = axis.x * axis.y;
		float yz = axis.y * axis.z;
		float zx = axis.z * axis.x;
		float xs = axis.x * s;
		float ys = axis.y * s;
		float zs = axis.z * s;
		float icxy = ic * xy;
		float icyz = ic * yz;
		float iczx = ic * zx;

		e00 = ic * axis.x * axis.x + c;
		e01 = icxy - zs;
		e02 = iczx + ys;
		e03 = 0.0f;
		e10 = icxy + zs;
		e11 = ic * axis.y * axis.y + c;
		e12 = icyz - xs;
		e13 = 0.0f;
		e20 = iczx - ys;
		e21 = icyz + xs;
		e22 = ic * axis.z * axis.z + c;
		e23 = 0.0f;
		e30 = 0.0f;
		e31 = 0.0f;
		e32 = 0.0f;
		e33 = 1.0f;
	}

	public Mat4f add(Mat4f m)
	{
		Mat4f a = new Mat4f(this);
		a.e00 += m.e00;
		a.e10 += m.e10;
		a.e20 += m.e20;
		a.e30 += m.e30;

		a.e01 += m.e01;
		a.e11 += m.e11;
		a.e21 += m.e21;
		a.e31 += m.e31;

		a.e02 += m.e02;
		a.e12 += m.e12;
		a.e22 += m.e22;
		a.e32 += m.e32;

		a.e03 += m.e03;
		a.e13 += m.e13;
		a.e23 += m.e23;
		a.e33 += m.e33;

		return a;
	}

	public Vec3f getPos()
	{
		return new Vec3f(e03, e13, e23);
	}

	public void setPos(Vec3f v)
	{
		this.e03 = v.x;
		this.e13 = v.y;
		this.e23 = v.z;
	}

	public Mat4f mul(Mat4f b)
	{
		Mat4f a = new Mat4f();
		a.e00 = e00 * b.e00 + e01 * b.e10 + e02 * b.e20 + e03 * b.e30;
		a.e10 = e10 * b.e00 + e11 * b.e10 + e12 * b.e20 + e13 * b.e30;
		a.e20 = e20 * b.e00 + e21 * b.e10 + e22 * b.e20 + e23 * b.e30;
		a.e30 = e30 * b.e00 + e31 * b.e10 + e32 * b.e20 + e33 * b.e30;
		a.e01 = e00 * b.e01 + e01 * b.e11 + e02 * b.e21 + e03 * b.e31;
		a.e11 = e10 * b.e01 + e11 * b.e11 + e12 * b.e21 + e13 * b.e31;
		a.e21 = e20 * b.e01 + e21 * b.e11 + e22 * b.e21 + e23 * b.e31;
		a.e31 = e30 * b.e01 + e31 * b.e11 + e32 * b.e21 + e33 * b.e31;
		a.e02 = e00 * b.e02 + e01 * b.e12 + e02 * b.e22 + e03 * b.e32;
		a.e12 = e10 * b.e02 + e11 * b.e12 + e12 * b.e22 + e13 * b.e32;
		a.e22 = e20 * b.e02 + e21 * b.e12 + e22 * b.e22 + e23 * b.e32;
		a.e32 = e30 * b.e02 + e31 * b.e12 + e32 * b.e22 + e33 * b.e32;
		a.e03 = e00 * b.e03 + e01 * b.e13 + e02 * b.e23 + e03 * b.e33;
		a.e13 = e10 * b.e03 + e11 * b.e13 + e12 * b.e23 + e13 * b.e33;
		a.e23 = e20 * b.e03 + e21 * b.e13 + e22 * b.e23 + e23 * b.e33;
		a.e33 = e30 * b.e03 + e31 * b.e13 + e32 * b.e23 + e33 * b.e33;
		return a;
	}

	public Vec3f mul(Vec3f v)
	{
		return new Vec3f(
				e00 * v.x + e01 * v.y + e02 * v.z + e03,
				e10 * v.x + e11 * v.y + e12 * v.z + e13,
				e20 * v.x + e21 * v.y + e22 * v.z + e23
		);
	}

	public Vec4f mul(Vec4f v)
	{
		return new Vec4f(
				e00 * v.x + e01 * v.y + e02 * v.z + e03 * v.w,
				e10 * v.x + e11 * v.y + e12 * v.z + e13 * v.w,
				e20 * v.x + e21 * v.y + e22 * v.z + e23 * v.w,
				e30 * v.x + e31 * v.y + e32 * v.z + e33 * v.w
		);
	}

	public Mat4f mul(float x)
	{
		Mat4f m = new Mat4f();
		m.e00 = e00 * x;
		m.e10 = e10 * x;
		m.e20 = e20 * x;
		m.e30 = e30 * x;
		m.e01 = e01 * x;
		m.e11 = e11 * x;
		m.e21 = e21 * x;
		m.e31 = e31 * x;
		m.e02 = e02 * x;
		m.e12 = e12 * x;
		m.e22 = e22 * x;
		m.e32 = e32 * x;
		m.e03 = e03 * x;
		m.e13 = e13 * x;
		m.e23 = e23 * x;
		m.e33 = e33 * x;
		return m;
	}

	public Mat4f identity()
	{
		this.e00 = 1;
		this.e10 = 0;
		this.e20 = 0;
		this.e30 = 0;
		this.e01 = 0;
		this.e11 = 1;
		this.e21 = 0;
		this.e31 = 0;
		this.e02 = 0;
		this.e12 = 0;
		this.e22 = 1;
		this.e32 = 0;
		this.e03 = 0;
		this.e13 = 0;
		this.e23 = 0;
		this.e33 = 1;
		return this;
	}

	public float det()
	{
		return e00 * (e11 * (e22 * e33 - e32 * e23) - e21 * (e12 * e33 - e32 * e13) + e31 * (e12 * e23 - e22 * e13)) -
		       e10 * (e01 * (e22 * e33 - e32 * e23) - e21 * (e02 * e33 - e32 * e03) + e31 * (e02 * e23 - e22 * e03)) +
		       e20 * (e01 * (e12 * e33 - e32 * e13) - e11 * (e02 * e33 - e32 * e03) + e31 * (e02 * e13 - e12 * e03)) -
		       e30 * (e01 * (e12 * e23 - e22 * e13) - e11 * (e02 * e23 - e22 * e03) + e21 * (e02 * e13 - e12 * e03));
	}

	public Mat4f inverse()
	{
		float D = 1 / this.det();
		Mat4f m = new Mat4f();
		m.e00 = (e11 * (e22 * e33 - e32 * e23) - e21 * (e12 * e33 - e32 * e13) + e31 * (e12 * e23 - e22 * e13)) * D;
		m.e01 = -(e01 * (e22 * e33 - e32 * e23) - e21 * (e02 * e33 - e32 * e03) + e31 * (e02 * e23 - e22 * e03)) * D;
		m.e02 = (e01 * (e12 * e33 - e32 * e13) - e11 * (e02 * e33 - e32 * e03) + e31 * (e02 * e13 - e12 * e03)) * D;
		m.e03 = -(e01 * (e12 * e23 - e22 * e13) - e11 * (e02 * e23 - e22 * e03) + e21 * (e02 * e13 - e12 * e03)) * D;
		m.e10 = -(e10 * (e22 * e33 - e32 * e23) - e20 * (e12 * e33 - e32 * e13) + e30 * (e12 * e23 - e22 * e13)) * D;
		m.e11 = (e00 * (e22 * e33 - e32 * e23) - e20 * (e02 * e33 - e32 * e03) + e30 * (e02 * e23 - e22 * e03)) * D;
		m.e12 = -(e00 * (e12 * e33 - e32 * e13) - e10 * (e02 * e33 - e32 * e03) + e30 * (e02 * e13 - e12 * e03)) * D;
		m.e13 = (e00 * (e12 * e23 - e22 * e13) - e10 * (e02 * e23 - e22 * e03) + e20 * (e02 * e13 - e12 * e03)) * D;
		m.e20 = (e10 * (e21 * e33 - e31 * e23) - e20 * (e11 * e33 - e31 * e13) + e30 * (e11 * e23 - e21 * e13)) * D;
		m.e21 = -(e00 * (e21 * e33 - e31 * e23) - e20 * (e01 * e33 - e31 * e03) + e30 * (e01 * e23 - e21 * e03)) * D;
		m.e22 = (e00 * (e11 * e33 - e31 * e13) - e10 * (e01 * e33 - e31 * e03) + e30 * (e01 * e13 - e11 * e03)) * D;
		m.e23 = -(e00 * (e11 * e23 - e21 * e13) - e10 * (e01 * e23 - e21 * e03) + e20 * (e01 * e13 - e11 * e03)) * D;
		m.e30 = -(e10 * (e21 * e32 - e31 * e22) - e20 * (e11 * e32 - e31 * e12) + e30 * (e11 * e22 - e21 * e12)) * D;
		m.e31 = (e00 * (e21 * e32 - e31 * e22) - e20 * (e01 * e32 - e31 * e02) + e30 * (e01 * e22 - e21 * e02)) * D;
		m.e32 = -(e00 * (e11 * e32 - e31 * e12) - e10 * (e01 * e32 - e31 * e02) + e30 * (e01 * e12 - e11 * e02)) * D;
		m.e33 = (e00 * (e11 * e22 - e21 * e12) - e10 * (e01 * e22 - e21 * e02) + e20 * (e01 * e12 - e11 * e02)) * D;
		return m;
	}

	public Mat4f transpose()
	{
		Mat4f m = new Mat4f();
		m.e00 = e00;
		m.e10 = e01;
		m.e20 = e02;
		m.e30 = e03;
		m.e01 = e10;
		m.e11 = e11;
		m.e21 = e12;
		m.e31 = e13;
		m.e02 = e20;
		m.e12 = e21;
		m.e22 = e22;
		m.e32 = e23;
		m.e03 = e30;
		m.e13 = e31;
		m.e23 = e32;
		m.e33 = e33;
		return m;
	}

	public Mat4f translate(Vec3f v)
	{
		Mat4f m = new Mat4f();
		m.identity();
		m.setPos(v);
		return this.mul(m);
	}

	public Mat4f rotate(float angle, Vec3f axis)
	{
		Mat4f m = new Mat4f(angle, axis);
		return this.mul(m);
	}

	public Mat4f scale(Vec3f v)
	{
		Mat4f m = new Mat4f();
		m.identity();
		m.e00 = v.x;
		m.e11 = v.y;
		m.e22 = v.z;
		return this.mul(m);
	}

	public void lookat(Vec3f pos, Vec3f target, Vec3f up)
	{
		Vec3f D = pos.sub(target);
		D = D.normal();
		Vec3f R = up.cross(D);
		R = R.normal();
		Vec3f U = D.cross(R);

		e00 = R.x;
		e01 = R.y;
		e02 = R.z;
		e03 = -pos.dot(R);
		e10 = U.x;
		e11 = U.y;
		e12 = U.z;
		e13 = -pos.dot(U);
		e20 = D.x;
		e21 = D.y;
		e22 = D.z;
		e23 = -pos.dot(D);
		e30 = 0;
		e31 = 0;
		e32 = 0;
		e33 = 1;
	}

	public void ortho(float left, float right, float bottom, float top, float znear, float zfar)
	{
		e00 = 2 / (right - left);
		e10 = 0;
		e20 = 0;
		e30 = 0;

		e01 = 0;
		e11 = 2 / (top - bottom);
		e21 = 0;
		e31 = 0;

		e02 = 0;
		e12 = 0;
		e22 = -2 / (zfar - znear);
		e32 = 0;

		e03 = -(right + left) / (right - left);
		e13 = -(top + bottom) / (top - bottom);
		e23 = -(zfar + znear) / (zfar - znear);
		e33 = 1;
	}

	public void frustum(float left, float right, float bottom, float top, float znear, float zfar)
	{
		e00 = 2 * znear / (right - left);
		e10 = 0;
		e20 = 0;
		e30 = 0;

		e01 = 0;
		e11 = 2 * znear / (top - bottom);
		e21 = 0;
		e31 = 0;

		e02 = (right + left) / (right - left);
		e12 = (top + bottom) / (top - bottom);
		e22 = -(zfar + znear) / (zfar - znear);
		e32 = -1;

		e03 = 0;
		e13 = 0;
		e23 = -2 * zfar * znear / (zfar - znear);
		e33 = 0;
	}

	public void perspective(float fov, float aspect, float znear, float zfar)
	{
		float y = znear * (float) Math.tan(fov * 0.5 * com.a2client.corex.Const.deg2rad);
		float x = y * aspect;
		frustum(-x, x, -y, y, znear, zfar);
	}

	public void perspective2(float fov, float left, float right, float top, float bottom, float znear, float zfar, float dx, float dy)
	{
		float aspect = (right - left) / (bottom - top);

		float y = znear * (float) Math.tan(fov * 0.5 * com.a2client.corex.Const.deg2rad);
		float x = y * (aspect);
		float w = (right - left) / 2;
		float h = (bottom - top) / 2;
		float fx = x * (dx - w) / w;
		float fy = y * (dy - h) / h;
		frustum(-x - fx, x - fx, -y + fy, y + fy, znear, zfar);
	}

	public com.a2client.corex.Quat getRot()
	{
		float t = e00 + e11 + e22 + 1;
		if (t > com.a2client.corex.Const.EPS)
		{
			float s = (float) (0.5 / Math.sqrt(t));
			return new com.a2client.corex.Quat(
					(e21 - e12) * s,
					(e02 - e20) * s,
					(e10 - e01) * s,
					0.25f / s
			);
		}
		else
		{
			if (e00 > e11 && e00 > e22)
			{
				float s = (float) (2 * Math.sqrt(1 + e00 - e11 - e22));
				return new com.a2client.corex.Quat(
						0.25f * s,
						(e01 + e10) / s,
						(e02 + e20) / s,
						(e21 - e12) / s
				);
			}
			else
			{
				if (e11 > e22)
				{
					float s = (float) (2 * Math.sqrt(1 + e11 - e00 - e22));
					return new com.a2client.corex.Quat(
							(e01 + e10) / s,
							0.25f * s,
							(e12 + e21) / s,
							(e02 - e20) / s
					);
				}
				else
				{
					float s = (float) (2 * Math.sqrt(1 + e22 - e00 - e11));
					return new com.a2client.corex.Quat(
							(e02 + e20) / s,
							(e12 + e21) / s,
							0.25f * s,
							(e10 - e01) / s
					);
				}
			}
		}
	}

	public Mat4f setRot(com.a2client.corex.Quat q)
	{
		Mat4f m = new Mat4f(this);
		float sqw = q.w * q.w;
		float sqx = q.x * q.x;
		float sqy = q.y * q.y;
		float sqz = q.z * q.z;

		float invs = 1 / (sqx + sqy + sqz + sqw);
		m.e00 = (sqx - sqy - sqz + sqw) * invs;
		m.e11 = (-sqx + sqy - sqz + sqw) * invs;
		m.e22 = (-sqx - sqy + sqz + sqw) * invs;

		float tmp1 = q.x * q.y;
		float tmp2 = q.z * q.w;
		m.e10 = 2 * (tmp1 + tmp2) * invs;
		m.e01 = 2 * (tmp1 - tmp2) * invs;

		tmp1 = q.x * q.z;
		tmp2 = q.y * q.w;
		m.e20 = 2 * (tmp1 - tmp2) * invs;
		m.e02 = 2 * (tmp1 + tmp2) * invs;

		tmp1 = q.y * q.z;
		tmp2 = q.x * q.w;
		m.e21 = 2 * (tmp1 + tmp2) * invs;
		m.e12 = 2 * (tmp1 - tmp2) * invs;
		return m;
	}

}
