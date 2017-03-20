package com.a2client.modelviewer.g3d;

import com.a2client.modelviewer.g3d.math.DualQuat;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by arksu on 17.03.17.
 */
public class Skeleton
{
	private final SkeletonData _data;

	private Skeleton _parent;
	private int _parent_joint;

	public Animation _animation;

	private DualQuat[] _joint;
	private DualQuat[] _jquat;

	public Skeleton(SkeletonData data)
	{
		_data = data;
		_jquat = new DualQuat[_data.getJointsCount()];
		_joint = new DualQuat[_data.getJointsCount()];
	}

	public void bind(ShaderProgram shader)
	{
		DualQuat[] jquat = new DualQuat[_data.getJointsCount()];

		for (int i = 0; i < _data.getJointsCount(); i++)
		{
			updateJoint(i);
			jquat[i] = _joint[i].mul(_data.getJoints()[i].getBind());
		}

		FloatBuffer jquatFloatBuffer = getFloatBuffer(jquat);
		float[] floatArray = getFloatArray(jquat);

		String name = "u_joints";
		int location = shader.fetchUniformLocation(name, true);
		shader.setUniform4fv(name, floatArray, 0, floatArray.length);

//		shader.setUniform4fv("u_joints[0]", new float[]{1, 0.5f, 0, 0}, 0, 4);
//		shader.setUniform4fv("u_joints[1]", new float[]{1, 0.5f, 0, 1}, 0, 4);

//		ARBShaderObjects.glUniform3ARB(location, jquatFloatBuffer);

//		Gdx.gl20.glUniform4fv(location, 0, jquatFloatBuffer);
//		Gdx.gl20.glUniform3fv(location, jquat.length * 8, floatArray, 0);
	}

	public void updateJoint(int idx)
	{
		float w = 1f;
		DualQuat cjoint = _data.getJoints()[idx].getFrame();
		// идем с последней добавленной анимации и смотрим на ее вес.
		// с каждой пройденной анимацией вычитаем вес
		int ac = 0;
		/*for (int i = anims.size() - 1; i >= 0; i--)
		{
			if (w < Const.EPS)
			{
//                удаляем анимации уже отыгравшие свое
				Log.debug("anim remove: " + anims.get(i).name);
				anims.remove(i);
			}
			else
			{
				Anim anim = anims.get(i);
				if ((anim.map[idx] > -1))
				{
					anim.lerpJoint(anim.map[idx]);
					if (anim.joint[anim.map[idx]] != null)
						cjoint = cjoint.lerp(anim.joint[anim.map[idx]], w);
					w -= anim.weight;
				}
//            if (w < Const.EPS) break;
			}
			ac++;
		}*/

		if (idx > -1 && _animation != null)
		{
			_animation.lerpJoint(idx);
			if (_animation.joint[idx] != null)
			{
				cjoint = cjoint.lerp(_animation.joint[idx], w);
			}
		}

//        if (parent != null) {
//        Log.debug("anim count : "+ac);
//        }

		/*w = 1;
		for (int i = merge_anims.size() - 1; i >= 0; i--)
		{
			if (w < Const.EPS)
			{// || merge_anims.get(i).isStopped()) {
				// удаляем анимации уже отыгравшие свое
				Anim aa = merge_anims.get(i);
				Log.debug("merge anim remove: " + aa.name + " prev=" + aa.FramePrev + " next=" + aa.FrameNext + " start=" + aa.FrameStart);
				merge_anims.remove(i);
			}
			else
			{
				Anim anim = merge_anims.get(i);
				if ((anim.map[idx] > -1) && (anim.weight > Const.EPS))
				{
					anim.lerpJoint(anim.map[idx]);
					if (anim.joint[anim.map[idx]] != null)
						cjoint = cjoint.lerp(anim.joint[anim.map[idx]], w * anim.weight);
					w -= anim.weight;
				}
			}
		}
*/

//		cjoint =  cjoint.mul(new DualQuat(new Quat(-10, -10, -10, -2), new Quat(-10, -10, -4, -3)));

		if (_data.getJoints()[idx].getParentIndex() > -1)
		{
			updateJoint(_data.getJoints()[idx].getParentIndex());
			_joint[idx] = _joint[_data.getJoints()[idx].getParentIndex()].mul(cjoint);
		}
		else
		{
//			if (_parent != null)
//			{
//				_parent.updateJoint(_parent_joint);
//				if (_parent._joint[_parent_joint] != null)
//				{
//					_joint[idx] = _parent._joint[_parent_joint].mul(cjoint);
//				}
//			}
//			else
//			{
			_joint[idx] = cjoint;
//			}
		}

	}

	public FloatBuffer getFloatBuffer(DualQuat[] d)
	{
		ByteBuffer temp = ByteBuffer.allocateDirect(d.length * 8 * 4);
		temp.order(ByteOrder.nativeOrder());
		FloatBuffer fb = temp.asFloatBuffer();

		for (DualQuat dq : d)
		{
			fb.
					  put(dq.real.x).
					  put(dq.real.y).
					  put(dq.real.z).
					  put(dq.real.w).

					  put(dq.dual.x).
					  put(dq.dual.y).
					  put(dq.dual.z).
					  put(dq.dual.w);
		}
		fb.flip();
//		fb.position(0);
		return fb;
	}

	public float[] getFloatArray(DualQuat[] d)
	{
		float[] tmp = new float[d.length * 8];

		int idx = 0;
		for (DualQuat dq : d)
		{
			tmp[idx++] = dq.real.x;
			tmp[idx++] = dq.real.y;
			tmp[idx++] = dq.real.z;
			tmp[idx++] = dq.real.w;
			tmp[idx++] = dq.dual.x;
			tmp[idx++] = dq.dual.y;
			tmp[idx++] = dq.dual.z;
			tmp[idx++] = dq.dual.w;
		}
		return tmp;
	}

	public int getJointsCount()
	{
		return _data.getJointsCount();
	}
}
