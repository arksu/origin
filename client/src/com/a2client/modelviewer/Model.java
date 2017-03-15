package com.a2client.modelviewer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.LinkedList;
import java.util.List;

/**
 * модель для отображения в 3д мире
 * Created by arksu on 14.03.17.
 */
public class Model
{
	private ModelData _data;

	/**
	 * положение относительно родителя
	 */
	private Matrix4 _localTransform = new Matrix4();

	/**
	 * абсолютное положение в мире, апдейтится на основе иерархии
	 */
	private Matrix4 _worldTransform = new Matrix4();

	/**
	 * модель родитель к которой привязана эта
	 */
	private Model _parent;

	/**
	 * список моделей привязанных к этой по положению
	 */
	private List<Model> _childs;

	/**
	 * see {@link GL20#GL_LINE_STRIP}, {@link GL20#GL_TRIANGLE_STRIP}
	 */
	private int _primitiveType = GL20.GL_TRIANGLES;

	public Model(ModelData data)
	{
		_data = data;
	}

	public void render(ModelBatch modelBatch)
	{
		modelBatch.getShader().setUniformMatrix("u_worldTrans", _worldTransform);

		_data.render(modelBatch, _primitiveType);
	}

	public ModelData getData()
	{
		return _data;
	}

	private void setParent(Model parent)
	{
		_parent = parent;
	}

	public void addChild(Model model)
	{
		if (_childs == null)
		{
			_childs = new LinkedList<>();
		}
		_childs.add(model);
		model.setParent(this);
	}

	public List<Model> getChilds()
	{
		return _childs;
	}

	public void setTransform(Matrix4 transform)
	{
		_localTransform = transform.cpy();
		updateWorldTransform();
	}

	public void setPos(Vector3 position)
	{
		_localTransform.setTranslation(position);
		updateWorldTransform();
	}

	public void setPos(float x, float y, float z)
	{
		_localTransform.setTranslation(x, y, z);
		updateWorldTransform();
	}

	private void updateWorldTransform()
	{
		if (_parent == null)
		{
			_worldTransform.set(_localTransform);
		}
		else
		{
			_worldTransform.set(_parent._worldTransform);
			_worldTransform.mul(_localTransform);
		}

		if (_childs != null)
		{
			for (Model child : _childs)
			{
				child.updateWorldTransform();
			}
		}
	}

}
