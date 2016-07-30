package com.a2client.render.shadows;

import com.a2client.render.GameCamera;
import com.a2client.util.vector.Matrix4f;
import com.a2client.util.vector.Vector3f;
import com.a2client.util.vector.Vector4f;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.GameCamera.FOV;
import static com.a2client.render.GameCamera.NEAR;

/**
 * кубоид для определения границ вывода карты глубин
 * для дальнейшего обсчета теней
 * Created by arksu on 28.07.16.
 */
public class ShadowBox2
{
	private static final Logger _log = LoggerFactory.getLogger(ShadowBox2.class.getName());

	private static final float OFFSET = 5;
	private static final Vector4f UP = new Vector4f(0, 1, 0, 0);
	private static final Vector4f FORWARD = new Vector4f(0, 0, -1, 0);
	private static float SHADOW_DISTANCE = 20;

	private float minX, maxX;
	private float minY, maxY;
	private float minZ, maxZ;
	private Matrix4 lightViewMatrix;
	private GameCamera cam;

	private float farHeight, farWidth, nearHeight, nearWidth;

	/**
	 * Creates a new shadow box and calculates some initial values relating to
	 * the camera's view frustum, namely the width and height of the near plane
	 * and (possibly adjusted) far plane.
//	 * @param lightViewMatrix - basically the "view matrix" of the light. Can be used to
	 * transform a point from world space into "light" space (i.e.
	 * changes a point's coordinates from being in relation to the
	 * world's axis to being in terms of the light's local axis).
	 * @param camera - the in-game camera.
	 */
	protected ShadowBox2(GameCamera camera)
	{
		lightViewMatrix = new Matrix4();
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
		// TODO
		SHADOW_DISTANCE = cam.getCameraDistance() * 2;
		calculateWidthsAndHeights();

		Matrix4 rot = calculateCameraRotationMatrix();
		Vector3 forw = new Vector3(0,0,-1);
		forw.mul(rot);

		Matrix4f rotation = Matrix4f.fromM4(rot);
		Vector4f f = Matrix4f.transform(rotation, FORWARD, null);
		Vector3f forwardVector = new Vector3f(f);

		Vector3f toFar = new Vector3f(forwardVector);
		toFar.scale(SHADOW_DISTANCE);
		Vector3f toNear = new Vector3f(forwardVector);
		toNear.scale(NEAR);
		Vector3f pos = new Vector3f(cam.position);
		Vector3f centerNear = Vector3f.add(toNear, pos, null);
		Vector3f centerFar = Vector3f.add(toFar, pos, null);

		Vector4f[] points = calculateFrustumVertices(rotation, forwardVector, centerNear, centerFar);

		boolean first = true;
		for (Vector4f point : points)
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
	protected Vector3f getCenter()
	{
		float x = (minX + maxX) / 2f;
		float y = (minY + maxY) / 2f;
		float z = (minZ + maxZ) / 2f;
		Vector4f cen = new Vector4f(x, y, z, 1);
		Matrix4f invertedLight = new Matrix4f();
//		Matrix4f.invert(lightViewMatrix, invertedLight);
		return new Vector3f(Matrix4f.transform(invertedLight, cen, null));
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
	 * @param rotation - camera's rotation.
	 * @param forwardVector - the direction that the camera is aiming, and thus the
	 * direction of the frustum.
	 * @param centerNear - the center point of the frustum's near plane.
	 * @param centerFar - the center point of the frustum's (possibly adjusted) far
	 * plane.
	 * @return The positions of the vertices of the frustum in light space.
	 */
	private Vector4f[] calculateFrustumVertices(Matrix4f rotation, Vector3f forwardVector,
												Vector3f centerNear, Vector3f centerFar)
	{
		Vector3f upVector = new Vector3f(Matrix4f.transform(rotation, UP, null));
		Vector3f rightVector = Vector3f.cross(forwardVector, upVector, null);
		Vector3f downVector = new Vector3f(-upVector.x, -upVector.y, -upVector.z);
		Vector3f leftVector = new Vector3f(-rightVector.x, -rightVector.y, -rightVector.z);
		Vector3f farTop = Vector3f.add(centerFar, new Vector3f(upVector.x * farHeight,
															   upVector.y * farHeight, upVector.z * farHeight), null);
		Vector3f farBottom = Vector3f.add(centerFar, new Vector3f(downVector.x * farHeight,
																  downVector.y * farHeight, downVector.z * farHeight), null);
		Vector3f nearTop = Vector3f.add(centerNear, new Vector3f(upVector.x * nearHeight,
																 upVector.y * nearHeight, upVector.z * nearHeight), null);
		Vector3f nearBottom = Vector3f.add(centerNear, new Vector3f(downVector.x * nearHeight,
																	downVector.y * nearHeight, downVector.z * nearHeight), null);
		Vector4f[] points = new Vector4f[8];
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
	private Vector4f calculateLightSpaceFrustumCorner(Vector3f startPoint, Vector3f direction,
													  float width)
	{
		Vector3f point = Vector3f.add(startPoint,
									  new Vector3f(direction.x * width, direction.y * width, direction.z * width), null);
		Vector4f point4f = new Vector4f(point.x, point.y, point.z, 1f);
//		Matrix4f.transform(lightViewMatrix, point4f, point4f);
		return point4f;
	}

	/**
	 * @return The rotation of the camera represented as a matrix.
	 */
	private Matrix4 calculateCameraRotationMatrix()
	{
		Matrix4 tmp = new Matrix4();
		float ay = cam.getAngleY();
		float ax = cam.getAngleX();

		tmp.rotate(Vector3.Y, ay);
		tmp.rotate(Vector3.X, ax);

		return tmp;
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

	public float getMinX()
	{
		return minX;
	}

	public float getMaxX()
	{
		return maxX;
	}

	public float getMinY()
	{
		return minY;
	}

	public float getMaxY()
	{
		return maxY;
	}

	public float getMinZ()
	{
		return minZ;
	}

	public float getMaxZ()
	{
		return maxZ;
	}
}
