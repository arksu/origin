package com.a2client.render.shadows;

import com.a2client.render.DepthFrameBuffer;
import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.a2client.render.ShadowShaderProvider;
import com.a2client.util.vector.Matrix4f;
import com.a2client.util.vector.Vector2f;
import com.a2client.util.vector.Vector3f;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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

	public static final int SHADOW_MAP_SIZE = 1024;

	public static final String VERTEX = "assets/shaders/shadowVertex.glsl";
	public static final String FRAGMENT = "assets/shaders/shadowFragment.glsl";

	private final DepthFrameBuffer _frameBuffer;

	private final ModelBatch _modelBatch;

	private final ShadowBox _shadowBox;
	private final ShadowBox2 _shadowBox2;

	private Matrix4f lightViewMatrix = new Matrix4f();
	private Matrix4 projectionMatrix = new Matrix4();
	private Matrix4f projectionViewMatrix = new Matrix4f();
	private Matrix4f offset = createOffset();

	private GameCamera _camera;

	private OrthographicCamera _ortoCamera;
	protected float halfDepth;
	protected float halfHeight;
	protected final Vector3 tmpV = new Vector3();
	protected final Vector3 direction = new Vector3(-10, -10, -10);

	public Shadow(GameCamera camera, final ShaderProgram shader)
	{
		_camera = camera;

		_ortoCamera = new OrthographicCamera(SHADOW_MAP_SIZE / 5f, SHADOW_MAP_SIZE / 5f);
		_ortoCamera.near = 1f;
		_ortoCamera.far = 1000f;
		halfHeight = SHADOW_MAP_SIZE * 0.5f;
		halfDepth = _ortoCamera.near + 0.5f * (_ortoCamera.far - _ortoCamera.near);

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
		_shadowBox2 = new ShadowBox2(camera);
	}

	public void update(Camera camera)
	{
		_shadowBox.update();
		_shadowBox2.update();

		_log.debug("box: " + _shadowBox.getWidth() + " " + _shadowBox.getHeight() + " " + _shadowBox.getLength() + " cen: " + _shadowBox.getCenter());

		updateCamera(tmpV.set(camera.direction).scl(halfHeight), camera.direction);

		Vector3f sunPosition = new Vector3f(Render.sunPosition);
		Vector3f lightDirection = new Vector3f(-sunPosition.x, -sunPosition.y, -sunPosition.z);
		prepare(lightDirection);
	}

	public void updateCamera(final Vector3 center, final Vector3 forward)
	{
		_ortoCamera.zoom = 1;
		_ortoCamera.viewportWidth = _shadowBox.getWidth();
		_ortoCamera.viewportHeight = _shadowBox.getHeight();

//		_ortoCamera.near = _shadowBox.getMinZ();
//		_ortoCamera.far = _shadowBox.getMaxZ();

		_ortoCamera.position.set(20, 20, 20);
		Vector3 tmp = _ortoCamera.position.cpy();
		tmp.nor();
		_ortoCamera.direction.set(-tmp.x, -tmp.y, -tmp.z);
//		_ortoCamera.position.set(direction);
//		_ortoCamera.direction.set(0,0,0).nor();

		// cam.position.set(10,10,10);
//		_ortoCamera.position.set(direction).scl(-halfDepth).add(center);
//		_ortoCamera.direction.set(direction).nor();
		// cam.up.set(forward).nor();
		_ortoCamera.normalizeUp();
		_ortoCamera.update();
	}

	private void prepare(Vector3f lightDirection)
	{
		updateOrthoProjectionMatrix();
		Vector3f center = _shadowBox.getCenter();
		Matrix4 view;
		updateLightViewMatrix(lightDirection, center);
//		Matrix4f.mul(projectionMatrix, lightViewMatrix, projectionViewMatrix);

		view = lightViewMatrix.toM4();
//		view = _camera.view;
//		view = _ortoCamera.view;

		Matrix4 projection = projectionMatrix;
//		Matrix4 projection = _camera.projection;
//		Matrix4 projection = _ortoCamera.projection;

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
//		projectionMatrix.setToOrtho(_shadowBox.getMinX(), _shadowBox.getMaxX(), _shadowBox.getMinY(), _shadowBox.getMaxY(),
//									_shadowBox.getMinZ(), _shadowBox.getMaxZ());

		projectionMatrix.val[M00] = 2f / _shadowBox.getWidth();
		projectionMatrix.val[M11] = 2f / _shadowBox.getHeight();
		projectionMatrix.val[M22] = -2f / _shadowBox.getLength();
		projectionMatrix.val[M33] = 1;
	}

	private Matrix4 updateLightViewMatrix(Vector3f direction, Vector3f center)
	{
		direction.normalise();
		center.negate();
		lightViewMatrix.setIdentity();
		float pitch = (float) Math.acos(new Vector2f(direction.x, direction.z).length());
		float yaw = (float) Math.toDegrees(((float) Math.atan(direction.x / direction.z)));
		yaw = direction.z > 0 ? yaw - 180 : yaw;

		Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), lightViewMatrix, lightViewMatrix);
		Matrix4f.rotate((float) -Math.toRadians(yaw), new Vector3f(0, 1, 0), lightViewMatrix, lightViewMatrix);

		Matrix4 tmp = lightViewMatrix.toM4();

		Vector3 vec = new Vector3(center.x, 0, center.z);
//		Vector3 vec = new Vector3(-_camera.position.x, 0, -_camera.position.z);
//		vec.mul(tmp);
//		tmp.setTranslation(vec);
//		lightViewMatrix = Matrix4f.fromM4(tmp);

//		_log.debug("center: " + vec);

		center = new Vector3f(center.x, 0, center.z);
		Matrix4f.translate(center, lightViewMatrix, lightViewMatrix);
//		lightViewMatrix.setTranslate(center);

		tmp = lightViewMatrix.toM4();
		return tmp;
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
