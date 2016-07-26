package com.a2client.render;

import com.a2client.model.GameObject;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * простая игровая камера
 * Created by arksu on 25.02.15.
 */
public class GameCamera extends PerspectiveCamera
{
	private static final Logger _log = LoggerFactory.getLogger(GameCamera.class.getName());

	public static final float OFFSET_SPEED = 8f;

	/**
	 * дистанция от камеры до точки куда смотрим
	 */
	private float _cameraDistance = 20;

	/**
	 * углы поворота камеры
	 */
	private float _angleY = 45f;
	private float _angleX = 72f;

	/**
	 * координаты мыши в которых начали вращение камеры
	 */
	private Vec2i _startDrag;

	private float _startAngleY;
	private float _startAngleX;

	private GameObject _chaseObj = null;

	private final Vector3 _offset = new Vector3(0, 0, 0);

	private Ray _ray;

	private MousePicker _mousePicker;

	public GameCamera()
	{
		fieldOfView = 30f;
		viewportWidth = 800;
		viewportHeight = 600;
		near = 1f;
		far = 1000f;

		_mousePicker = new MousePicker();

		update();
	}

	public void update()
	{
		_ray = getPickRay(Gdx.input.getX(), Gdx.input.getY());
		_mousePicker.update(_ray, this);
		_log.debug("terrain point: " + _mousePicker.getCurrentTerrainPoint());

		if (_chaseObj != null)
		{
			// установим верх
			up.set(0, 1, 0);

			// обнулим позицию
			position.setZero();
			// установим камеру на нужную нам дистанцию
			position.add(0, _cameraDistance, 0);
			// а теперь повернем как надо
			position.rotate(Vector3.X, _angleX);
			position.rotate(Vector3.Y, _angleY);

			// и сместим до положения игрока
			position.add(_chaseObj.getWorldCoord());

			// скажем смотреть на игрока
			lookAt(_chaseObj.getWorldCoord());

			position.add(_offset);
		}

		if (com.a2client.Input.isWheelUpdated())
		{
			_cameraDistance += (_cameraDistance / 15f) * com.a2client.Input.MouseWheel;
			com.a2client.Input.MouseWheel = 0;
		}

		super.update();

	}

	public void setChaseObject(GameObject obj)
	{
		_chaseObj = obj;

		if (_chaseObj == null)
		{
			return;
		}

		direction.set(0, 0, -1);

//		_current.set(position).sub(direction).nor();

	}

	public void onResize(int width, int height)
	{
//		float camWidth = width / 48f;
//		float camHeight = camWidth * ((float) height / (float) width);

		viewportWidth = width;
		viewportHeight = height;

		update();
	}

	public void startDrag(Vec2i startDrag)
	{
		_startDrag = startDrag;
		_startAngleX = _angleX;
		_startAngleY = _angleY;
	}

	public void updateDrag(Vec2i c)
	{
		if (_startDrag != null)
		{
			_angleY = _startAngleY - (c.sub(_startDrag).x * 0.3f);
			_angleX = _startAngleX - (c.sub(_startDrag).y * 0.3f);

			// ограничим вертикальный угол
			_angleX = Math.min(_angleX, 110f);
			_angleX = Math.max(_angleX, 1f);

//			_log.debug("ay: " + _angleY + " ax:" + _angleX);
		}
	}

	public Vector3 getOffset()
	{
		return _offset;
	}

	public Ray getRay()
	{
		return _ray;
	}

	public MousePicker getMousePicker()
	{
		return _mousePicker;
	}
}
