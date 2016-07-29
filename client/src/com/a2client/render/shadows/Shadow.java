package com.a2client.render.shadows;

import com.a2client.render.DepthFrameBuffer;
import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.a2client.render.ShadowShaderProvider;
import com.a2client.util.vector.Matrix4f;
import com.a2client.util.vector.Vector2f;
import com.a2client.util.vector.Vector3f;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.07.16.
 */
public class Shadow
{
	private static final Logger _log = LoggerFactory.getLogger(Shadow.class.getName());

	public static final int SHADOW_MAP_SIZE = 1024;

	public static final String VERTEX = "assets/shaders/shadowVertex.glsl";
	public static final String FRAGMENT = "assets/shaders/shadowFragment.glsl";

	private final DepthFrameBuffer _frameBuffer;

	private final ModelBatch _modelBatch;

	private final ShadowBox _shadowBox;

	private Matrix4f lightViewMatrix = new Matrix4f();
	private Matrix4 projectionMatrix = new Matrix4();
	private Matrix4f projectionViewMatrix = new Matrix4f();
	private Matrix4f offset = createOffset();

	private GameCamera _camera;

	public Shadow(GameCamera camera, final ShaderProgram shader)
	{
		_camera = camera;
		_frameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true);
		_frameBuffer.createDepthTextre(Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge);
		ShadowShaderProvider shaderProvider = new ShadowShaderProvider();
		_modelBatch = new ModelBatch(shaderProvider);
//		_modelBatch = new ModelBatch(new DefaultShaderProvider() {
//			@Override
//			protected Shader createShader(Renderable renderable)
//			{
//				return new ShadowShader(renderable, shader);
//			}
//		});
		_shadowBox = new ShadowBox(lightViewMatrix, camera);
	}

	public void update()
	{
		_shadowBox.update();
		Vector3f sunPosition = new Vector3f(Render.sunPosition);
		Vector3f lightDirection = new Vector3f(-sunPosition.x, -sunPosition.y, -sunPosition.z);
		prepare(lightDirection);
	}

	private void prepare(Vector3f lightDirection)
	{
		updateOrthoProjectionMatrix();
		Vector3f center = _shadowBox.getCenter();
		updateLightViewMatrix(lightDirection, center);
//		Matrix4f.mul(projectionMatrix, lightViewMatrix, projectionViewMatrix);

		Matrix4 view = lightViewMatrix.toM4();
//		Matrix4 view = _camera.view;

		Matrix4 projection = projectionMatrix;
//		Matrix4 projection = _camera.projection;


		_camera.projection.set(projection);
		_camera.view.set(view);
		Matrix4 combined = new Matrix4();
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);
		_camera.combined.set(combined);

	}

	/**
	 * Creates the orthographic projection matrix. This projection matrix
	 * basically sets the width, length and height of the "view cuboid", based
	 * on the values that were calculated in the {@link ShadowBox} class.
	 */
	private void updateOrthoProjectionMatrix()
	{
		projectionMatrix.idt();
		projectionMatrix.setToOrtho(_shadowBox.getMinX(),_shadowBox.getMaxX(), _shadowBox.getMinY(), _shadowBox.getMaxY(),
									_shadowBox.getMinZ(), _shadowBox.getMaxZ());



//		projectionMatrix.m00 = 2f / width;
//		projectionMatrix.m11 = 2f / height;
//		projectionMatrix.m22 = -2f / length;
//		projectionMatrix.m33 = 1;
	}

	private void updateLightViewMatrix(Vector3f direction, Vector3f center)
	{
		direction.normalise();
		center.negate();
		lightViewMatrix.setIdentity();
		float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;

		Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix, lightViewMatrix);
		Matrix4f.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0), lightViewMatrix, lightViewMatrix);

//		Matrix4f.translate(center, lightViewMatrix, lightViewMatrix);
	}

	public Matrix4f getToShadowMapSpaceMatrix()
	{
		return Matrix4f.mul(offset, projectionViewMatrix, null);
	}

	/**
	 * Create the offset for part of the conversion to shadow map space. This
	 * conversion is necessary to convert from one coordinate system to the
	 * coordinate system that we can use to sample to shadow map.
	 * @return The offset as a matrix (so that it's easy to apply to other matrices).
	 */
	private static Matrix4f createOffset()
	{
		Matrix4f offset = new Matrix4f();
		offset.translate(new Vector3f(0.5f, 0.5f, 0.5f));
		offset.scale(new Vector3f(0.5f, 0.5f, 0.5f));
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
