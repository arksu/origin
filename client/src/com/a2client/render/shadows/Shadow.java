package com.a2client.render.shadows;

import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.a2client.render.ShadowShaderProvider;
import com.a2client.render.framebuffer.DepthFrameBuffer;
import com.a2client.render.skybox.Skybox;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL14;

import static com.badlogic.gdx.math.Matrix4.*;

/**
 * Created by arksu on 28.07.16.
 */
public class Shadow
{
	private static final int SHADOW_MAP_SIZE = 2048;

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

		_frameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true, false, true);
		_frameBuffer.setHasDepthTexture(true);
		_frameBuffer.setDepthBufferSize(GL14.GL_DEPTH_COMPONENT16);
		_frameBuffer.setDepthTextureFilter(Texture.TextureFilter.Nearest);
		_frameBuffer.setDepthTextureWrap(Texture.TextureWrap.ClampToEdge);
		_frameBuffer.build();
		_modelBatch = new ModelBatch(new ShadowShaderProvider(render.getShadowShader()));
		_shadowBox = new ShadowBox(camera);
	}

	public void update()
	{
		_shadowBox.update();

		Vector3 lightDirection = new Vector3(-Skybox.sunPosition.x, -Skybox.sunPosition.y, -Skybox.sunPosition.z);
		prepare(lightDirection);
	}

	private void prepare(Vector3 lightDirection)
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

	private Matrix4 updateLightViewMatrix(Vector3 direction, Vector3 center)
	{
		direction.nor();
		center.x = -center.x;
		center.y = -center.y;
		center.z = -center.z;

		double len = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
		float pitch = (float) Math.toDegrees(Math.acos(len));
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;

		Matrix4 view = _shadowBox.getLightViewMatrix();
		view.idt();
		view.rotate(1, 0, 0, pitch);
		view.rotate(0, 1, 0, -yaw);

		Matrix4 t = new Matrix4().setTranslation(new Vector3(center.x, center.y, center.z));

		view.mul(t);

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
