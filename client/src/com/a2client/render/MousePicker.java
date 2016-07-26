package com.a2client.render;

import com.a2client.Terrain;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.07.16.
 */
public class MousePicker
{
	private static final Logger _log = LoggerFactory.getLogger(MousePicker.class.getName());

	private static final int RECURSION_COUNT = 300;
	private static final float RAY_RANGE = 100;

	private static final float STEP_LEN = 1.3f;

	private Vector3 _currentTerrainPoint;

	public void update(Ray ray, Camera camera)
	{
		long time = System.currentTimeMillis();
		float len = 0;

		_currentTerrainPoint = null;
//		int steps = 0;
//		float f1 = 0, f2 = 0;
		while (len < RAY_RANGE)
		{
			if (intersectionInRange(len, len + STEP_LEN, ray))
			{
//				f1 = len;
//				f2 = len + STEP_LEN;
				_currentTerrainPoint = binarySearch(0, len, len + STEP_LEN, ray);
				break;
			}
			len += STEP_LEN;
//			steps++;
		}
//		_log.debug(
//				"steps: " + steps +
//				" " + (_currentTerrainPoint != null ? "Found" : "NULL") +
//				" " + f1 + " " + f2 +
//				" in " + (System.currentTimeMillis() - time) + " ms");
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
