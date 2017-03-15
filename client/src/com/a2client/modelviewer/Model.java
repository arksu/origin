package com.a2client.modelviewer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;

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
	private Matrix4 _localTransform;

	/**
	 * абсолютное положение в мире, апдейтится на основе иерархии
	 */
	private Matrix4 _worldTransform;

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
		_data.render(modelBatch, _primitiveType);
	}

	public ModelData getData()
	{
		return _data;
	}

}
