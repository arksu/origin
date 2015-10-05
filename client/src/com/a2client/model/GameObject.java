package com.a2client.model;

import com.a2client.MapCache;
import com.a2client.ModelManager;
import com.a2client.network.game.serverpackets.ObjectAdd;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * базовый игровой объект
 * Created by arksu on 06.02.15.
 */
public class GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(GameObject.class.getName());

	private int _objectId;
	private int _typeId;
	private Vector2 _coord;
	private String _name;
	private String _title;
	private boolean _interactive;
	private Mover _mover = null;
	private BoundingBox _modelBoundingBox;
	private BoundingBox _boundingBox;

	private ModelInstance _model = null;

	private AnimationController _animation = null;

	private boolean _needUpdate = true;

	private Vector3 _worldCoord;

	public GameObject(ObjectAdd pkt)
	{
		_name = pkt._name;
		_title = pkt._title;
		_coord = new Vector2(pkt._x, pkt._y);
		_worldCoord = new Vector3(_coord.x / MapCache.TILE_SIZE, 0.5f, _coord.y / MapCache.TILE_SIZE);
		_objectId = pkt._objectId;
		_typeId = pkt._typeId;
		_interactive = false;
		_modelBoundingBox = new BoundingBox(new Vector3(-1, 0, -1),
											new Vector3(+1, 1, +1));
		_boundingBox = new BoundingBox();
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getTypeId()
	{
		return _typeId;
	}

	public Vector2 getCoord()
	{
		return _coord;
	}

	public Vector3 getWorldCoord()
	{
		return _worldCoord;
	}

	public String getName()
	{
		return _name;
	}

	public BoundingBox getBoundingBox()
	{
		return _boundingBox;
	}

	public ModelInstance getModel()
	{
		return _model;
	}

	public void setCoord(int x, int y)
	{
		_coord.x = x;
		_coord.y = y;
	}

	public void setCoord(Vec2i c)
	{
		_coord.x = c.x;
		_coord.y = c.y;
	}

	public void setCoord(Vector2 c)
	{
		_coord.x = c.x;
		_coord.y = c.y;
	}

	public void setInteractive(boolean interactive)
	{
		_interactive = interactive;
	}
	
	public void setAnimation(String id) 
	{
		setAnimation(id, -1);
	}
	
	public void setAnimation(String id, int loop) 
	{
		if (_animation != null)
			_animation.setAnimation(id, loop);
	}

	public boolean isInteractive()
	{
		return _interactive;
	}

	/**
	 * сервер сообщает о движении объекта
	 * @param cx текущие координаты
	 * @param cy текущие координаты
	 * @param vx вектор движения
	 * @param vy вектор движения
	 */
	public void Move(int cx, int cy, int vx, int vy, int speed)
	{
		if (_mover != null)
		{
			_mover.NewMove(cx, cy, vx, vy, speed);
		}
		else
		{
			_mover = new Mover(this, cx, cy, vx, vy, speed);
			setAnimation(_model.animations.first().id, -1);
		}
	}

	public void StopMove()
	{
		_mover = null;
	}

	public boolean isMoving()
	{
		return _mover != null;
	}

	public void Update()
	{
		if (_needUpdate)
		{
			_needUpdate = false;
			InitModel();

		}

		ModelManager.getInstance().updateModelTime(_typeId);
		
		if (_animation != null)
			_animation.update(Gdx.graphics.getDeltaTime());
		

		if (_mover != null)
		{
			_mover.Update();
			if (_mover._arrived)
			{
				_mover = null;
				setAnimation(null);
			}
		}
	}

	private void InitModel()
	{
		_model = ModelManager.getInstance().getModelByType(_typeId);
		if (_model != null)
		{
			UpdateCoordandBB();

			if (_model.animations.size > 0)
				_animation = new AnimationController(_model);

		}
	}

	public void UpdateCoordandBB()
	{
		_worldCoord = new Vector3(_coord.x / MapCache.TILE_SIZE, 0.5f, _coord.y / MapCache.TILE_SIZE);
		_model.transform.setTranslation(_worldCoord);
		_boundingBox.min.set(_worldCoord).add(_modelBoundingBox.min);
		_boundingBox.max.set(_worldCoord).add(_modelBoundingBox.max);
	}

	@Override
	public String toString()
	{
		return "(" + _name + " " + _objectId + " type=" + _typeId + ")";
	}
}
