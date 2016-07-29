package com.a2client.render.shadows;

import com.a2client.render.GameCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * кубоид для определения границ вывода карты глубин
 * для дальнейшего обсчета теней
 * Created by arksu on 28.07.16.
 */
public class ShadowBox
{
	private static final Logger _log = LoggerFactory.getLogger(ShadowBox.class.getName());

	private static final float OFFSET = 10;
	private static final Vector3 UP = new Vector3(0, 1, 0);
	private static final Vector3 FORWARD = new Vector3(0, 0, -1);
	private static final float SHADOW_DISTANCE = 100f;

	private float minX, maxX;
	private float minY, maxY;
	private float minZ, maxZ;
	private Matrix4 lightViewMatrix;
	private Camera cam;

	private float farHeight, farWidth, nearHeight, nearWidth;

	public ShadowBox(Matrix4 lightViewMatrix, Camera cam)
	{
		this.lightViewMatrix = lightViewMatrix;
		this.cam = cam;
		calculateWidthsAndHeights();
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
		farWidth = (float) (SHADOW_DISTANCE * Math.tan(Math.toRadians(GameCamera.FOV)));
		nearWidth = (float) (GameCamera.NEAR
							 * Math.tan(Math.toRadians(GameCamera.FOV)));
		farHeight = farWidth / getAspectRatio();
		nearHeight = nearWidth / getAspectRatio();
	}

	/**
	 * @return The aspect ratio of the display (width:height ratio).
	 */
	private float getAspectRatio()
	{
		return (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
	}


	/**
	 * Updates the bounds of the shadow box based on the light direction and the
	 * camera's view frustum, to make sure that the box covers the smallest area
	 * possible while still ensuring that everything inside the camera's view
	 * (within a certain range) will cast shadows.
	 */
	public void update()
	{
		/*Matrix4 rotation = calculateCameraRotationMatrix();
		Vector3 forwardVector = new Vector3(Matrix4.transform(rotation, FORWARD, null));

		Vector3 toFar = new Vector3(forwardVector);
		toFar.scl(SHADOW_DISTANCE);
		Vector3 toNear = new Vector3(forwardVector);
		toNear.scl(GameCamera.NEAR);
		Vector3 centerNear = toNear.add(cam.position);
		Vector3 centerFar = toFar.add(cam.position);

		Vector3[] points = calculateFrustumVertices(rotation, forwardVector, centerNear,
													centerFar);

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
		maxZ += OFFSET;*/
	}

	/**
	 * Calculates the center of the "view cuboid" in light space first, and then
	 * converts this to world space using the inverse light's view matrix.
	 * @return The center of the "view cuboid" in world space.
	 */
	/*protected Vector3 getCenter()
	{
		float x = (minX + maxX) / 2f;
		float y = (minY + maxY) / 2f;
		float z = (minZ + maxZ) / 2f;
		Vector3 cen = new Vector3(x, y, z);
		Matrix4 invertedLight = lightViewMatrix.cpy().inv();
		return new Vector3(Matrix4.transform(invertedLight, cen, null));
	}*/

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
	 * @param rotation - camera's rotation.
	 * @param forwardVector - the direction that the camera is aiming, and thus the
	 * direction of the frustum.
	 * @param centerNear - the center point of the frustum's near plane.
	 * @param centerFar - the center point of the frustum's (possibly adjusted) far
	 * plane.
	 * @return The positions of the vertices of the frustum in light space.
	 */
	private Vector3[] calculateFrustumVertices(Matrix4 rotation, Vector3 forwardVector,
												Vector3 centerNear, Vector3 centerFar)
	{
/*
		Vector3 upVector = new Vector3(Matrix4.transform(rotation, UP, null));
		Vector3 rightVector = Vector3.cross(forwardVector, upVector, null);
		Vector3 downVector = new Vector3(-upVector.x, -upVector.y, -upVector.z);
		Vector3 leftVector = new Vector3(-rightVector.x, -rightVector.y, -rightVector.z);
		Vector3 farTop = Vector3.add(centerFar, new Vector3(upVector.x * farHeight,
															   upVector.y * farHeight, upVector.z * farHeight), null);
		Vector3 farBottom = Vector3.add(centerFar, new Vector3(downVector.x * farHeight,
																  downVector.y * farHeight, downVector.z * farHeight), null);
		Vector3 nearTop = Vector3.add(centerNear, new Vector3(upVector.x * nearHeight,
																 upVector.y * nearHeight, upVector.z * nearHeight), null);
		Vector3 nearBottom = Vector3.add(centerNear, new Vector3(downVector.x * nearHeight,
																	downVector.y * nearHeight, downVector.z * nearHeight), null);
*/
		Vector3[] points = new Vector3[8];
/*
		points[0] = calculateLightSpaceFrustumCorner(farTop, rightVector, farWidth);
		points[1] = calculateLightSpaceFrustumCorner(farTop, leftVector, farWidth);
		points[2] = calculateLightSpaceFrustumCorner(farBottom, rightVector, farWidth);
		points[3] = calculateLightSpaceFrustumCorner(farBottom, leftVector, farWidth);
		points[4] = calculateLightSpaceFrustumCorner(nearTop, rightVector, nearWidth);
		points[5] = calculateLightSpaceFrustumCorner(nearTop, leftVector, nearWidth);
		points[6] = calculateLightSpaceFrustumCorner(nearBottom, rightVector, nearWidth);
		points[7] = calculateLightSpaceFrustumCorner(nearBottom, leftVector, nearWidth);
*/
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
/*
	private Vector3 calculateLightSpaceFrustumCorner(Vector3 startPoint, Vector3 direction,
													  float width)
	{
		Vector3 point = Vector3.add(startPoint,
									  new Vector3(direction.x * width, direction.y * width, direction.z * width), null);
		Vector3 point4f = new Vector3(point.x, point.y, point.z, 1f);
		Matrix4.transform(lightViewMatrix, point4f, point4f);
		return point4f;
	}
*/

	/**
	 * @return The rotation of the camera represented as a matrix.
	 */
/*
	private Matrix4 calculateCameraRotationMatrix()
	{
		Matrix4 rotation = new Matrix4();
		rotation.rotate((float) Math.toRadians(-cam.getYaw()), new Vector3(0, 1, 0));
		rotation.rotate((float) Math.toRadians(-cam.getPitch()), new Vector3(1, 0, 0));
		return rotation;
	}
*/

}
