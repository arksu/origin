package com.a2client.render.shadows;

import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.a2client.render.ShadowShaderProvider;
import com.a2client.render.framebuffer.DepthFrameBuffer;
import com.a2client.util.vector.Vector2f;
import com.a2client.util.vector.Vector3f;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.math.Matrix4.*;

/**
 * Created by arksu on 28.07.16.
 */
public class Shadow
{
	private static final Logger _log = LoggerFactory.getLogger(Shadow.class.getName());

	public static final int SHADOW_MAP_SIZE = 2048;

	public static final String VERTEX = "shadow";
	public static final String FRAGMENT = "shadow";

	private final DepthFrameBuffer _frameBuffer;

	private final ModelBatch _modelBatch;

	private final ShadowBox _shadowBox;

	private Matrix4 _projectionMatrix = new Matrix4();
	private Matrix4 _combined = new Matrix4();
	private final Matrix4 offset = createOffset();

	private GameCamera _camera;

	public Shadow(GameCamera camera, Render render)
	{
		_camera = camera;

		_frameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true);
		_frameBuffer.createDepthTextre(Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge);
		_modelBatch = new ModelBatch(new ShadowShaderProvider(render.getShadowShader()));
		_shadowBox = new ShadowBox(camera);
	}

	public void update(Camera camera)
	{
		_shadowBox.update();

		Vector3f sunPosition = new Vector3f(Render.sunPosition);
		Vector3f lightDirection = new Vector3f(-sunPosition.x, -sunPosition.y, -sunPosition.z);
		prepare(lightDirection);
	}

	private void prepare(Vector3f lightDirection)
	{
		updateOrthoProjectionMatrix();
		Vector3 center = _shadowBox.getCenter();
		Matrix4 view = updateLightViewMatrix(lightDirection, center);

		_camera.projection.set(_projectionMatrix);
		_camera.view.set(view);
		_combined = new Matrix4();
		_combined.set(_projectionMatrix);
		Matrix4.mul(_combined.val, view.val);
		_camera.combined.set(_combined);

	}

	/**
	 * Creates the orthographic projection matrix. This projection matrix
	 * basically sets the width, length and height of the "view cuboid", based
	 * on the values that were calculated in the {@link ShadowBox} class.
	 */
	private void updateOrthoProjectionMatrix()
	{
		_projectionMatrix.idt();

		_projectionMatrix.val[M00] = 2f / _shadowBox.getWidth();
		_projectionMatrix.val[M11] = 2f / _shadowBox.getHeight();
		_projectionMatrix.val[M22] = -2f / _shadowBox.getLength();
		_projectionMatrix.val[M33] = 1;
	}

	private Matrix4 updateLightViewMatrix(Vector3f direction, Vector3 center)
	{
		direction.normalise();
		center.x = -center.x;
		center.y = -center.y;
		center.z = -center.z;

		float pitch = (float) Math.toDegrees(Math.acos(new Vector2f(direction.x, direction.z).length()));
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;

//		Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix, lightViewMatrix);
//		Matrix4f.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0), lightViewMatrix, lightViewMatrix);

		Matrix4 view = _shadowBox.getLightViewMatrix();
		view.idt();
		view.rotate(1, 0, 0, pitch);
		view.rotate(0, 1, 0, -yaw);

		Matrix4 t = new Matrix4().setTranslation(new Vector3(center.x, center.y, center.z));

		view.mul(t);

//		lightViewMatrix= Matrix4f.fromM4(view);
		return view;
	}

	public Matrix4 getToShadowMapSpaceMatrix()
	{
		return offset.cpy().mul(_combined);
	}

	/**
	 * Create the offset for part of the conversion to shadow map space. This
	 * conversion is necessary to convert from one coordinate system to the
	 * coordinate system that we can use to sample to shadow map.
	 * @return The offset as a matrix (so that it's easy to apply to other matrices).
	 */
	private static Matrix4 createOffset()
	{
		Matrix4 offset = new Matrix4();

		offset.setTranslation(0.5f, 0.5f, 0.5f);
		offset.scale(0.5f, 0.5f, 0.5f);
		return offset;
	}

	public DepthFrameBuffer getFrameBuffer()
	{
		return _frameBuffer;
	}

	public ModelBatch getModelBatch()
	{
		return _modelBatch;
	}
}
