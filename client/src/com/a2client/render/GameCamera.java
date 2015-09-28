package com.a2client.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.a2client.model.GameObject;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * простая игровая камера
 * Created by arksu on 25.02.15.
 */
public class GameCamera extends PerspectiveCamera 
{
	private static final Logger _log = LoggerFactory.getLogger(GameCamera.class.getName());

	/**
	 * на сколько передвигаем камеру за 1 тик клавишами (для дебага)
	 */
	static final float MOVE_STEP = 0.2f;


	private Vector3 _cameraOffset = new Vector3(0, 0, 10);

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

	private GameObject chase_obj = null;

	private Vector3 current = new Vector3();
	
	public GameCamera() {
		fieldOfView = 30f;
		viewportWidth = 800;
		viewportHeight = 600;
		near = 1f;
		far = 1000f;
		
		update();
	}
	
	public void update()
	{
		if (chase_obj != null) 
		{
			position.set(chase_obj.getWorldCoord());
			current.rotate(_angleX, 1f, 0f, -1f);
			current.rotate(_angleY, 0f, 1f, 0f);
			current.nor().scl(_cameraDistance);
			position.add(current).add(0, _cameraDistance, 0);
			direction.set(chase_obj.getWorldCoord()).sub(position).nor();
		}
		
		if (com.a2client.Input.isWheelUpdated())
		{
			_cameraDistance += (_cameraDistance / 15f) * com.a2client.Input.MouseWheel;
			com.a2client.Input.MouseWheel = 0;
		}

		super.update();

	}
	
	public void setChaseObject(GameObject obj) {
		chase_obj = obj;
		
		if (chase_obj == null)
			return;
		
		this.position.set(chase_obj.getWorldCoord()).add(_cameraOffset);
		this.direction.set(0, 0, -1);
		
		current.set(position).sub(direction).nor();
		
	}

	public void onResize(int width, int height)
	{
//		float camWidth = width / 48f;
//		float camHeight = camWidth * ((float) height / (float) width);

		viewportWidth = width;
		viewportHeight = height;
		
		update();
	}

	public Vector2 screen2world(int x, int y)
	{
		Vector3 intersection = new Vector3();
		Ray ray = getPickRay(x, y);
		Intersector.intersectRayPlane(ray, xzPlane, intersection);
		return new Vector2(intersection.x, intersection.z);
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
		}
	}
}
