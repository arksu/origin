package com.a2client.render;

import com.a2client.MapCache;
import com.a2client.ObjectCache;
import com.a2client.gui.GUI;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
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
public class GameCamera
{
	private static final Logger _log = LoggerFactory.getLogger(GameCamera.class.getName());

	/**
	 * на сколько передвигаем камеру за 1 тик клавишами (для дебага)
	 */
	static final float MOVE_STEP = 0.2f;

	/**
	 * GDX камера
	 */
	private Camera _camera;

	/**
	 * оступ камеры, двигаем камеру клавишами
	 */
	private Vector2 _cameraOffset = new Vector2(0, 0);

	/**
	 * дистанция от камеры до точки куда смотрим
	 */
	private float _cameraDistance = 20;

	float rotationSpeed = 0.5f;
	boolean rotating = false;

	/**
	 * углы поворота камеры
	 */
	private float _angleY = 1;
	private float _angleX = 0.2f;

	/**
	 * координаты мыши в которых начали вращение камеры
	 */
	private Vec2i _startDrag;

	private float _startAngleY;
	private float _startAngleX;

	/**
	 * плоскость горизонта (тайлов) нужно для поиска позиции мыши на карте
	 */
	private final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);

	public void update()
	{
		if (GUI.getInstance().focused_control == null)
		{
			if (com.a2client.Input.KeyDown(Input.Keys.W))
			{
				_cameraOffset.y -= MOVE_STEP;
			}
			if (com.a2client.Input.KeyDown(Input.Keys.S))
			{
				_cameraOffset.y += MOVE_STEP;
			}
			if (com.a2client.Input.KeyDown(Input.Keys.A))
			{
				_cameraOffset.x -= MOVE_STEP;
			}
			if (com.a2client.Input.KeyDown(Input.Keys.D))
			{
				_cameraOffset.x += MOVE_STEP;
			}
			if (com.a2client.Input.KeyDown(Input.Keys.Q))
			{
				rotating = !rotating;
			}
		}
		if (com.a2client.Input.isWheelUpdated())
		{
			_cameraDistance += (_cameraDistance / 15f) * com.a2client.Input.MouseWheel;
			com.a2client.Input.MouseWheel = 0;
		}
		Vector2 playerPos = Vector2.Zero;
		if (ObjectCache.getInstance().getMe() != null)
		{
			playerPos = new Vector2(ObjectCache.getInstance().getMe().getCoord());
			playerPos = playerPos.scl(1f / MapCache.TILE_SIZE);
		}
		playerPos.add(_cameraOffset);
		if (rotating) rotationSpeed += 0.1f;
		_camera.position.set(new Vector3(
						playerPos.x + _cameraDistance * ((float) Math.sin(_angleY)),
						_cameraDistance * 1f,
						playerPos.y + _cameraDistance * ((float) Math.cos(_angleY))
				)
		);
		_camera.lookAt(new Vector3(playerPos.x, 0, playerPos.y));
//		_camera.invProjectionView.rotate(0, 0.1f, 0, 90);
		_camera.update();

	}

	public void onResize(int width, int height)
	{
		float camWidth = width / 48f;
		float camHeight = camWidth * ((float) height / (float) width);

		_camera = new PerspectiveCamera(30, camWidth, camHeight);
		_camera.near = 1f;
		_camera.far = 1000f;
		_camera.update();
	}

	public Vector2 screen2world(int x, int y)
	{
		Vector3 intersection = new Vector3();
		Ray ray = _camera.getPickRay(x, y);
		Intersector.intersectRayPlane(ray, xzPlane, intersection);
		return new Vector2(intersection.x, intersection.z);
	}

	public Camera getGdxCamera()
	{
		return _camera;
	}

	public void setStartDrag(Vec2i startDrag)
	{
		_startDrag = startDrag;
		_startAngleX = _angleX;
		_startAngleY = _angleY;
	}

	public void updateDrag(Vec2i c)
	{
		if (_startDrag != null)
		{
			_angleY = _startAngleY + (c.sub(_startDrag).x * 0.05f);
		}
	}
}
