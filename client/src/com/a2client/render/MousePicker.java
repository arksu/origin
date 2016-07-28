package com.a2client.render;

import com.a2client.Terrain;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * получаем координаты на земле путем проецирования луча из камеры в землю
 * Created by arksu on 26.07.16.
 */
public class MousePicker
{
	/**
	 * сколько шагов рекурсии допускаем на этапе бинарного поиска
	 */
	private static final int RECURSION_COUNT = 200;

	/**
	 * максимальная глубина поиска от точки камеры
	 */
	private static final float RAY_RANGE = 100;

	/**
	 * на сколько двигаемся за одну итерацию
	 */
	private static final float STEP_LEN = 1.3f;

	/**
	 * найденная точка на ландшафте
	 */
	private Vector3 _currentTerrainPoint;

	public void update(Ray ray)
	{
		float len = 0;

		_currentTerrainPoint = null;
		while (len < RAY_RANGE)
		{
			if (intersectionInRange(len, len + STEP_LEN, ray))
			{
				_currentTerrainPoint = binarySearch(0, len, len + STEP_LEN, ray);
				break;
			}
			len += STEP_LEN;
		}
	}

	private Vector3 getPointOnRay(Ray ray, float distance)
	{
		Vector3 start = ray.origin.cpy();
		Vector3 dir = ray.direction.cpy();
		dir.nor();
		return start.add(dir.x * distance, dir.y * distance, dir.z * distance);
	}

	private Vector3 binarySearch(int count, float start, float finish, Ray ray)
	{
		float half = start + ((finish - start) * 0.5f);
		if (count >= RECURSION_COUNT)
		{
			return getPointOnRay(ray, half);
		}
		if (intersectionInRange(start, half, ray))
		{
			return binarySearch(count + 1, start, half, ray);
		}
		else
		{
			return binarySearch(count + 1, half, finish, ray);
		}
	}

	private boolean intersectionInRange(float start, float finish, Ray ray)
	{
		Vector3 startPoint = getPointOnRay(ray, start);
		Vector3 endPoint = getPointOnRay(ray, finish);
		return !isUnderGround(startPoint) && isUnderGround(endPoint);
	}

	private boolean isUnderGround(Vector3 testPoint)
	{
		float height = Terrain.getHeight(testPoint.x, testPoint.z);
		return testPoint.y < height || height == Terrain.FAKE_HEIGHT;
	}

	public Vector3 getCurrentTerrainPoint()
	{
		return _currentTerrainPoint;
	}
}
