package com.a2client.render.skybox;

import com.a2client.Input;
import com.a2client.Main;
import com.a2client.render.Render;
import com.a2client.util.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 25.07.16.
 */
public class Skybox
{
	private static final float SIZE = 100;

	public static float fogDensity = 0.005f;

	public static float fogGradient = 2.5f;

	public static Color skyColor = new Color(0.4444f, 0.62f, 0.79f, 1f);
	public static Color fogColor = new Color(0.9444f, 0.62f, 0.79f, 1f);

	public static boolean fogEnabled = true;

	/**
	 * позиция солнца, учитывается при всем освещении
	 * также передается в шейдеры
	 */
	public static Vector3 sunPosition = new Vector3(10000, 20000, 10000);

	/**
	 * разрешить движение солнца по небосводу
	 */
	public static boolean sunMoving = false;

	/**
	 * время дня в 24 часовом формате (0-24)
	 */
	public static float sunTime = 13f;

	private ShaderProgram _shader;

	private Mesh _mesh;

	// debug
	private TuningWindow _tuningWindow;
	// debug
	public static boolean takeFromTuningWindow = false;

	private final static List<SkyboxParams> skyboxParamsList = new ArrayList<>();
	public static SkyboxParams skyboxParams;

	static
	{
		skyboxParamsList.add(new SkyboxParams(
				0f,
				new Color(0.067f, 0.078f, 0.145f, 1f),
				new Color(0.11f, 0.13f, 0.175f, 1f),
				28f, 1.34f, 0.65f
		));
		skyboxParamsList.add(new SkyboxParams(
				6f,
				new Color(0.067f, 0.078f, 0.145f, 1f),
				new Color(0.11f, 0.13f, 0.175f, 1f),
				27f, 1.48f, 0.85f
		));
		skyboxParamsList.add(new SkyboxParams(
				7f,
				new Color(0.62f, 0.361f, 0.349f, 1f),
				new Color(0.62f, 0.361f, 0.349f, 1f),
				24f, 1.41f, 0.86f
		));
		skyboxParamsList.add(new SkyboxParams(
				8f,
				new Color(0.62f, 0.361f, 0.349f, 1f),
				new Color(0.62f, 0.361f, 0.349f, 1f),
				25f, 1.52f, 0.85f
		));
		skyboxParamsList.add(new SkyboxParams(
				9f,
				new Color(0.4444f, 0.62f, 0.79f, 1f),
				new Color(0.4444f, 0.62f, 0.79f, 1f),
				25f, 1.70f, 0.91f
		));
		skyboxParamsList.add(new SkyboxParams(
				9.5f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				23f, 1.9f, 1.05f
		));
		skyboxParamsList.add(new SkyboxParams(
				10f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				21f, 2.01f, 1.19f
		));

		// ----------------------
		skyboxParamsList.add(new SkyboxParams(
				14f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				15f, 2.5f, 1.72f
		));

		skyboxParamsList.add(new SkyboxParams(
				17f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				16f, 2.09f, 1.50f
		));

		skyboxParamsList.add(new SkyboxParams(
				18f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.278f, 0.431f, 0.753f, 1f),
				21f, 2.01f, 1.19f
		));
		skyboxParamsList.add(new SkyboxParams(
				18.5f,
				new Color(0.278f, 0.431f, 0.753f, 1f),
				new Color(0.278f, 0.431f, 0.753f, 1f),
				23f, 1.9f, 1.05f
		));

		skyboxParamsList.add(new SkyboxParams(
				19f,
				new Color(0.494f, 0.475f, 0.42f, 1f),
				new Color(0.4444f, 0.62f, 0.79f, 1f),
				25f, 1.70f, 0.91f
		));

		skyboxParamsList.add(new SkyboxParams(
				20f,
				new Color(0.557f, 0.365f, 0.337f, 1f),
				new Color(0.557f, 0.365f, 0.337f, 1f),
				25f, 1.52f, 0.85f
		));

		skyboxParamsList.add(new SkyboxParams(
				21f,
				new Color(0.557f, 0.365f, 0.337f, 1f),
				new Color(0.557f, 0.365f, 0.337f, 1f),
				27f, 1.41f, 0.86f
		));

		skyboxParamsList.add(new SkyboxParams(
				22f,
				new Color(0.62f, 0.361f, 0.349f, 1f),
				new Color(0.52f, 0.261f, 0.249f, 1f),
				27f, 1.48f, 0.85f
		));

		skyboxParamsList.add(new SkyboxParams(
				24.01f,
				new Color(0.067f, 0.078f, 0.145f, 1f),
				new Color(0.11f, 0.13f, 0.175f, 1f),
				28f, 1.34f, 0.65f
		));
	}

	public Skybox()
	{
		_shader = Render.makeShader("skybox");

		makeMesh();

		// debug
//		_tuningWindow = new TuningWindow();
	}

	public void Render(Camera camera, Environment environment)
	{
		if (_tuningWindow != null) _tuningWindow.update();
		_shader.begin();
		prepareShader(camera, environment);

		_mesh.render(_shader, GL20.GL_TRIANGLES);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);

		_shader.end();
	}

	public void updateSkyParams()
	{
		SkyboxParams p1;
		SkyboxParams p2;
		int idx = 0;
		while (true)
		{
			SkyboxParams p = skyboxParamsList.get(idx);
			SkyboxParams plast = skyboxParamsList.get(idx + 1);
			if (Skybox.sunTime >= p.sunTime && Skybox.sunTime <= plast.sunTime)
			{
				p1 = p;
				p2 = plast;
				break;
			}
			idx++;
		}
		skyboxParams = new SkyboxParams(p1, p2);
		fogColor = skyboxParams.fogColor;
	}

	protected void prepareShader(Camera camera, Environment environment)
	{
		_shader.setUniformMatrix("u_projTrans", camera.projection);

		final float PI = ((float) Math.PI);
		Matrix4 tmp;
		tmp = camera.view.cpy();
		tmp.translate(camera.position);
		_shader.setUniformMatrix("u_viewTrans", tmp);

		_shader.setUniformf("u_skyColor", fogColor.r, fogColor.g, fogColor.b);
		_shader.setUniformf("u_density", fogEnabled ? fogDensity : 0f);
		_shader.setUniformf("u_gradient", fogGradient);

		// ATMO
		float radius = 1;
		float cameraHeight = 1;
		float outRadius = 1;
		if (_tuningWindow != null)
		{
			radius = _tuningWindow.getRadius();
			cameraHeight = radius * _tuningWindow.getCameraHeight();
			outRadius = _tuningWindow.getOutRadius();
		}

		if (!takeFromTuningWindow || _tuningWindow == null)
		{
			radius = skyboxParams.radius;
			cameraHeight = radius * skyboxParams.cameraHeight;
			outRadius = skyboxParams.outerRadius;
			_shader.setUniformf("u_backColor", skyboxParams.skyColor);

			if (_tuningWindow != null)
			{
				_tuningWindow._scrollbarRadius.setValue(Math.round(radius));
				_tuningWindow._scrollbarCameraHeight.setValue(Math.round(skyboxParams.cameraHeight * 100f));
				_tuningWindow._scrollbarOutRadius.setValue(Math.round(skyboxParams.outerRadius * 100f));
			}
		}

		float Kr = 0.0025f;
		float Km = 0.0010f;
		float ESun = 15.0f; // _tuningWindow.getEsun();
		float g = -0.990f;
		float innerRadius = radius;
		float outerRadius = radius * outRadius;
		Vector3 waveLength = new Vector3(0.650f, 0.570f, 0.475f);
		float scaleDepth = 0.25f;

		_shader.setUniformf("u_cameraPosition", -0, 0, 0);

		_shader.setUniformf("v3InvWavelength", new Vector3(
				1f / ((float) Math.pow(waveLength.x, 4)),
				1f / ((float) Math.pow(waveLength.y, 4)),
				1f / ((float) Math.pow(waveLength.z, 4))
		));
		_shader.setUniformf("fCameraHeight", cameraHeight);
		_shader.setUniformf("fCameraHeight2", cameraHeight * cameraHeight);
		_shader.setUniformf("fInnerRadius", innerRadius);
		_shader.setUniformf("fInnerRadius2", innerRadius * innerRadius);
		_shader.setUniformf("fOuterRadius", outerRadius);
		_shader.setUniformf("fOuterRadius2", outerRadius * outerRadius);
		_shader.setUniformf("fKrESun", Kr * ESun);
		_shader.setUniformf("fKmESun", Km * ESun);
		_shader.setUniformf("fKr4PI", Kr * 4.0f * PI);
		_shader.setUniformf("fKm4PI", Km * 4.0f * PI);
		_shader.setUniformf("fScale", 1f / (outerRadius - innerRadius));
		_shader.setUniformf("fScaleDepth", scaleDepth);
		_shader.setUniformf("fScaleOverScaleDepth", 1f / (outerRadius - innerRadius) / scaleDepth);
		_shader.setUniformf("g", g);
		_shader.setUniformf("g2", g * g);

		_shader.setUniformf("v3LightPosition", sunPosition.cpy().nor().scl(1f));
	}

	protected void makeMesh()
	{
		Icosahedron icosahedron = new Icosahedron(4, SIZE);
		_mesh = icosahedron.getMesh();
	}

	public static void updateSunPos()
	{
		final float sunDistance = 1000;

		if (Input.KeyHit(Keys.SPACE)) sunMoving = !sunMoving;
		if (Input.KeyHit(Keys.G)) takeFromTuningWindow = !takeFromTuningWindow;

		if (sunMoving)
		{
			sunTime += Main.deltaTime * 0.6f;
		}
		while (sunTime > 24f)
		{
			sunTime -= 24f;
		}

		// задали вектор на плоскости земли
		Vector3 pos = new Vector3(sunDistance, 0, 0);

		// повернем на угол в часах (24)
		pos.rotate(((sunTime - 14f) / 24f) * 360f, 0, -1, 0);

		// сместим в соответствии с широтой местности
		pos.add(0, 200f, 0);

		// и еще раз повернем на 23.5 градуса (угол наклона оси вращения планеты)
		pos.rotate(23.5f, 0, 0, 1);

		sunPosition.set(pos);
	}
}
