package com.a2client.render.shadows;

import com.a2client.render.GameCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import static com.a2client.render.GameCamera.FOV;
import static com.a2client.render.GameCamera.NEAR;

/**
 * кубоид для определения границ вывода карты глубин
 * для дальнейшего обсчета теней
 * Created by arksu on 28.07.16.
 */
public class ShadowBox
{
	/**
	 * отступ от края области до объектов. чем больше - тем больше вероятность
	 * что в куоид попадет объект которого не видно, но тень от него должна быть в области камеры
	 */
	private static final float OFFSET = 5;

	/**
	 * максимальная дистанция на которой будут видны тени
	 * рассчитываем автоматически на основе отдаления камеры
	 */
	public static float SHADOW_DISTANCE = 30;

	private float minX, maxX;
	private float minY, maxY;
	private float minZ, maxZ;

	/**
	 * видовая матрица
	 */
	private final Matrix4 lightViewMatrix = new Matrix4();

	private GameCamera cam;

	/**
	 * размеры плоскостей отсечения в системе координат основной камеры
	 */
	private float farHeight, farWidth, nearHeight, nearWidth;

	/**
	 * Creates a new shadow box and calculates some initial values relating to
	 * the camera's view frustum, namely the width and height of the near plane
	 * and (possibly adjusted) far plane.
	 * //	 * @param lightViewMatrix - basically the "view matrix" of the light. Can be used to
	 * transform a point from world space into "light" space (i.e.
	 * changes a point's coordinates from being in relation to the
	 * world's axis to being in terms of the light's local axis).
	 * @param camera - the in-game camera.
	 */
	ShadowBox(GameCamera camera)
	{
		this.cam = camera;
		calculateWidthsAndHeights();
	}

	/**
	 * Updates the bounds of the shadow box based on the light direction and the
	 * camera's view frustum, to make sure that the box covers the smallest area
	 * possible while still ensuring that everything inside the camera's view
	 * (within a certain range) will cast shadows.
	 */
	protected void update()
	{
		SHADOW_DISTANCE = cam.getCameraDistance() * 1.2f + 10f;
		calculateWidthsAndHeights();

		Vector3 forward = cam.direction;
		Vector3 up = cam.up;

		// получим центры плоскостей near & far для обсчета frustum
		Vector3 centerFar = forward.cpy().scl(SHADOW_DISTANCE).add(cam.position);
		Vector3 centerNear = forward.cpy().scl(NEAR).add(cam.position);

		Vector3[] points = calculateFrustumVertices(forward, up, centerNear, centerFar);

		boolean first = true;
		for (Vector3 point : points)
		{
			if (first)
			{
				minX = point.x;
				maxX = point.x;
				minY = point.y;
				maxY = point.y;
				minZ = point.z;
				maxZ = point.z;
				first = false;
				continue;
			}
			if (point.x > maxX)
			{
				maxX = point.x;
			}
			else if (point.x < minX)
			{
				minX = point.x;
			}
			if (point.y > maxY)
			{
				maxY = point.y;
			}
			else if (point.y < minY)
			{
				minY = point.y;
			}
			if (point.z > maxZ)
			{
				maxZ = point.z;
			}
			else if (point.z < minZ)
			{
				minZ = point.z;
			}
		}
		maxZ += OFFSET;

	}

	/**
	 * Calculates the center of the "view cuboid" in light space first, and then
	 * converts this to world space using the inverse light's view matrix.
	 * @return The center of the "view cuboid" in world space.
	 */
	Vector3 getCenter()
	{
		float x = (minX + maxX) / 2f;
		float y = (minY + maxY) / 2f;
		float z = (minZ + maxZ) / 2f;
		Vector3 center = new Vector3(x, y, z);

		Matrix4 inv = lightViewMatrix.cpy().inv();

		return center.prj(inv);
	}

	/**
	 * @return The width of the "view cuboid" (orthographic projection area).
	 */
	protected float getWidth()
	{
		return maxX - minX;
	}

	/**
	 * @return The height of the "view cuboid" (orthographic projection area).
	 */
	protected float getHeight()
	{
		return maxY - minY;
	}

	/**
	 * @return The length of the "view cuboid" (orthographic projection area).
	 */
	protected float getLength()
	{
		return maxZ - minZ;
	}

	/**
	 * Calculates the position of the vertex at each corner of the view frustum
	 * in light space (8 vertices in total, so this returns 8 positions).
	 * @param forwardVector - the direction that the camera is aiming, and thus the
	 * direction of the frustum.
	 * @param centerNear - the center point of the frustum's near plane.
	 * @param centerFar - the center point of the frustum's (possibly adjusted) far
	 * plane.
	 * @return The positions of the vertices of the frustum in light space.
	 */
	private Vector3[] calculateFrustumVertices(Vector3 forwardVector, Vector3 upVector,
											   Vector3 centerNear, Vector3 centerFar)
	{
		final Vector3 rightVector = forwardVector.cpy().crs(upVector);
		final Vector3 downVector = new Vector3(-upVector.x, -upVector.y, -upVector.z);
		final Vector3 leftVector = new Vector3(-rightVector.x, -rightVector.y, -rightVector.z);

		final Vector3 farTop = centerFar.cpy().add(
				upVector.x * farHeight, upVector.y * farHeight, upVector.z * farHeight);

		final Vector3 farBottom = centerFar.cpy().add(
				downVector.x * farHeight, downVector.y * farHeight, downVector.z * farHeight);

		final Vector3 nearTop = centerNear.cpy().add(
				upVector.x * nearHeight, upVector.y * nearHeight, upVector.z * nearHeight);

		final Vector3 nearBottom = centerNear.cpy().add(
				downVector.x * nearHeight, downVector.y * nearHeight, downVector.z * nearHeight);

		Vector3[] points = new Vector3[8];
		points[0] = calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth);
		points[1] = calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth);
		points[2] = calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth);
		points[3] = calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth);
		points[4] = calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth);
		points[5] = calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth);
		points[6] = calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth);
		points[7] = calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth);
		return points;
	}

	/**
	 * Calculates one of the corner vertices of the view frustum in world space
	 * and converts it to light space.
	 * @param startPoint - the starting center point on the view frustum.
	 * @param direction - the direction of the corner from the start point.
	 * @param width - the distance of the corner from the start point.
	 * @return - The relevant corner vertex of the view frustum in light space.
	 */
	private Vector3 calculateLightSpaceFrustumCorner(Vector3 startPoint, Vector3 direction, float width)
	{
		Vector3 point = direction.cpy().scl(width).add(startPoint);
		return point.prj(lightViewMatrix);
	}

	/**
	 * Calculates the width and height of the near and far planes of the
	 * camera's view frustum. However, this doesn't have to use the "actual" far
	 * plane of the view frustum. It can use a shortened view frustum if desired
	 * by bringing the far-plane closer, which would increase shadow resolution
	 * but means that distant objects wouldn't cast shadows.
	 */
	private void calculateWidthsAndHeights()
	{
		farWidth = (float) (SHADOW_DISTANCE * Math.tan(Math.toRadians(FOV)));
		nearWidth = (float) (NEAR * Math.tan(Math.toRadians(FOV)));

		final float aspectRatio = getAspectRatio();
		farHeight = farWidth / aspectRatio;
		nearHeight = nearWidth / aspectRatio;
	}

	/**
	 * @return The aspect ratio of the display (width:height ratio).
	 */
	private float getAspectRatio()
	{
		return (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
	}

	Matrix4 getLightViewMatrix()
	{
		return lightViewMatrix;
	}
}
