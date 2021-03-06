package com.a2client.g3d;

import com.a2client.Main;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * модель для отображения в 3д мире
 * Created by arksu on 14.03.17.
 */
public class Model
{
	/**
	 * интервал обновления баунд бокса
	 */
	private static final int UPDATE_BBOX_TIMEOUT = 2000;

	private ModelData _data;

	private Skeleton _skeleton;

	/**
	 * текущие анимации которые проигрываются в данный момент
	 */
	private final List<Animation> _animations = new ArrayList<>(4);

	/**
	 * наложенные анимации на список сверху
	 */
	private final List<Animation> _mergeAnimations = new ArrayList<>(4);

	/**
	 * поворот в градусах
	 */
	private float _heading;
	private float _currentHeading;
	private float _headingSpeed = 5f;

	/**
	 * положение в пространстве относителльно родителя
	 */
	private Vector3 _localPosition;

	/**
	 * положение относительно родителя (расчитывается из позиции и вращения)
	 */
	private Matrix4 _localTransform = new Matrix4();

	/**
	 * абсолютное положение в мире, апдейтится на основе иерархии и _localTransform
	 */
	private Matrix4 _worldTransform = new Matrix4();

	/**
	 * баунд бокс (ограничение видимости в камере для модели)
	 */
	private BoundingBox _boundingBox = new BoundingBox();

	/**
	 * время последнего обновления баунд бокса, обновляем с небольшой задержкой.
	 * т.к. операция дорогая - нет смысла каждый кадр обсчитывать.
	 */
	private long _lastTimeUpdateBoundingBox = 0;

	/**
	 * модель родитель к которой привязана эта
	 */
	private Model _parent;

	/**
	 * список моделей привязанных к этой по положению
	 */
	private List<Model> _childs;

	/**
	 * see {@link GL20#GL_TRIANGLES}, {@link GL20#GL_TRIANGLE_STRIP}
	 */
	private int _primitiveType = GL20.GL_TRIANGLES;

	private Object _userData;

	public Model(ModelData data)
	{
		_data = data;
		if (_data.getSkeletonData() != null)
		{
			_skeleton = new Skeleton(_data.getSkeletonData(), this);
		}
	}

	public void render(ModelBatch modelBatch)
	{
		modelBatch.getShader().setUniformMatrix("u_worldTrans", _worldTransform);

		if (_skeleton != null)
		{
			modelBatch.getShader().setUniformi("u_skinFlag", 1);
			_skeleton.bind(modelBatch.getShader());
		}
		else
		{
			modelBatch.getShader().setUniformi("u_skinFlag", 0);
		}

		if (_userData != null && _userData instanceof Boolean)
		{
			boolean userData = (Boolean) _userData;
			modelBatch.getShader().setUniformi("u_selected", userData ? 1 : 0);
		}

		_data.render(modelBatch, _primitiveType);
	}

	public Object getUserData()
	{
		return _userData;
	}

	public void setUserData(Object userData)
	{
		_userData = userData;
	}

	public List<Animation> getAnimations()
	{
		return _animations;
	}

	public List<Animation> getMergeAnimations()
	{
		return _mergeAnimations;
	}

	public ModelData getData()
	{
		return _data;
	}

	private void setParent(Model parent)
	{
		_parent = parent;
		updateWorldTransform();
	}

	public Skeleton getSkeleton()
	{
		return _skeleton;
	}

	public void addChild(Model model)
	{
		if (_childs == null)
		{
			_childs = new LinkedList<>();
		}
		if (_childs.contains(model)) return;
		_childs.add(model);
		model.setParent(this);
	}

	public void removeChild(Model model)
	{
		if (_childs != null)
		{
			if (_childs.remove(model))
			{
				model.setParent(null);
			}
		}
	}

	public List<Model> getChilds()
	{
		return _childs;
	}

//	public void setTransform(Matrix4 transform)
//	{
//		_localTransform = transform.cpy();
//		updateWorldTransform();
//	}

	public Matrix4 getTransform()
	{
		return _localTransform;
	}

	public float getHeading()
	{
		return _heading;
	}

	public void setHeading(float heading, boolean force)
	{
		_heading = heading;
		while (_heading > 180)
		{
			_heading -= 360;
		}
		while (_heading < -180)
		{
			_heading += 360;
		}

		if (force)
		{
			_currentHeading = _heading;
		}
		_localTransform.idt();
		_localTransform.setToRotation(0, 1, 0, _currentHeading);
		_localTransform.setTranslation(_localPosition);
		updateWorldTransform();
	}

	public void setPos(Vector3 position)
	{
		_localPosition = position.cpy();
		_localTransform.idt();
		_localTransform.setToRotation(0, 1, 0, _currentHeading);
		_localTransform.setTranslation(_localPosition);
		updateWorldTransform();
	}

	public void setPos(float x, float y, float z)
	{
		_localPosition = new Vector3(x, y, z);
		_localTransform.idt();
		_localTransform.setToRotation(0, 1, 0, _currentHeading);
		_localTransform.setTranslation(_localPosition);
		updateWorldTransform();
	}

	public BoundingBox getBoundingBox()
	{
		return _boundingBox;
	}

	public void updateWorldTransform()
	{
		if (_parent == null)
		{
			_worldTransform.set(_localTransform);
		}
		else
		{
			_worldTransform.set(_parent._worldTransform).mul(_localTransform);
		}
		updateBoundingBox();

		if (_childs != null)
		{
			for (Model child : _childs)
			{
				child.updateWorldTransform();
			}
		}
	}

	private void updateBoundingBox()
	{
		updateBoundingBox(true);
	}

	public void updateBoundingBox(boolean force)
	{
		long time = System.currentTimeMillis();
		if (time - _lastTimeUpdateBoundingBox > UPDATE_BBOX_TIMEOUT || force)
		{
			_boundingBox.inf();
			_data.extendBoundingBox(_boundingBox, _worldTransform);
			_lastTimeUpdateBoundingBox = time;
		}
	}

	public void update()
	{
		// обновим анимации
		for (Animation animation : _animations)
		{
			animation.update();
		}
		for (Animation animation : _mergeAnimations)
		{
			animation.update();
		}

		if (_currentHeading != _heading)
		{
			float a = _heading;

			if (_currentHeading < -90 && a > 90)
			{
				a -= 360;
			}

			if (_currentHeading > 90 && a < -90)
			{
				a += 360;
			}

			float dx = a - _currentHeading;
			if (Math.abs(dx) < 0.01f)
			{
				_currentHeading = _heading;
			}
			else
			{
				_currentHeading += dx * (Main.deltaTime * _headingSpeed);
			}

			while (_currentHeading > 180)
			{
				_currentHeading -= 360;
			}
			while (_currentHeading < -180)
			{
				_currentHeading += 360;
			}

			_localTransform.idt();
			_localTransform.setToRotation(0, 1, 0, _currentHeading);
			_localTransform.setTranslation(_localPosition);
			updateWorldTransform();
		}

		if (_childs != null)
		{
			for (Model child : _childs)
			{
				child.update();
			}
		}
	}

	public void playAnimation(String name)
	{
		playAnimation(name, 1.0f, 0.3f, Animation.LoopMode.Repeat);
	}

	public void playAnimation(String name, float blendWeight, float blendTime, Animation.LoopMode loopMode)
	{
		AnimationData animationData = _data.getAnimation(name);
		if (animationData != null)
		{
			Animation animation = new Animation(animationData);
			animation.play(blendWeight, blendTime, loopMode);
			_animations.add(0, animation);
		}
	}

	public void playMergeAnimation(String name)
	{
		AnimationData animationData = _data.getAnimation(name);
		if (animationData != null)
		{
			removeMergeAnimation(name);

			Animation animation = new Animation(animationData);
			animation.play(Animation.LoopMode.Last);
			_mergeAnimations.add(0, animation);
		}
	}

	public void removeMergeAnimation(String name)
	{
		Animation found = null;
		for (Animation animation : _mergeAnimations)
		{
			if (animation.getName().equals(name))
			{
				found = animation;
				break;
			}
		}
		if (found != null)
		{
			_mergeAnimations.remove(found);
		}
	}

	public void bindTo(Model other, String boneName)
	{
		other.addChild(this);

		if (_skeleton == null)
		{
			_skeleton = new Skeleton(SkeletonData.defaultEquipBone, this);
		}

		Skeleton otherSkeleton = other.getSkeleton();
		int index = otherSkeleton.getJointIndex(boneName);
		if (index < -1)
		{
			throw new RuntimeException("bone not found: " + boneName);
		}
		_skeleton.setParent(otherSkeleton, index);
		_skeleton.resetState();
	}

	public void unbind()
	{
		if (_parent != null)
		{
			_parent.removeChild(this);
		}
	}
}
