package com.a2client.render.skybox;

import com.a2client.Input;
import com.a2client.Main;
import com.a2client.render.Render;
import com.a2client.screens.Game;
import com.a2client.util.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 25.07.16.
 */
public class Skybox
{
	private static final Logger _log = LoggerFactory.getLogger(Skybox.class.getName());

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

	private Cubemap _cubemap;
	private Cubemap _cubemapNight;

	private ShaderProgram _shader;

	private Mesh _mesh;

	private float _rotate;

	private float _time = 5000;

	// debug
	private TuningWindow _tuningWindow;

	private final static List<SkyboxParams> skyParams = new ArrayList<>();

	public static SkyboxParams skyboxParams;

	static
	{
		skyParams.add(new SkyboxParams(
				0f,
				new Color(0.067f, 0.078f, 0.145f, 1f),
				new Color(0.11f, 0.13f, 0.175f, 1f),
				28f, 1.34f, 0.65f
		));
		skyParams.add(new SkyboxParams(
				6f,
				new Color(0.067f, 0.078f, 0.145f, 1f),
				new Color(0.11f, 0.13f, 0.175f, 1f),
				27f, 1.48f, 0.85f
		));
		skyParams.add(new SkyboxParams(
				7f,
				new Color(0.62f, 0.361f, 0.349f, 1f),
				new Color(0.62f, 0.361f, 0.349f, 1f),
				24f, 1.41f, 0.86f
		));
		skyParams.add(new SkyboxParams(
				8f,
				new Color(0.62f, 0.361f, 0.349f, 1f),
				new Color(0.62f, 0.361f, 0.349f, 1f),
				25f, 1.52f, 0.85f
		));
		skyParams.add(new SkyboxParams(
				9f,
				new Color(0.4444f, 0.62f, 0.79f, 1f),
				new Color(0.4444f, 0.62f, 0.79f, 1f),
				25f, 1.70f, 0.91f
		));
		skyParams.add(new SkyboxParams(
				9.5f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				23f, 1.9f, 1.05f
		));
		skyParams.add(new SkyboxParams(
				10f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				21f, 2.01f, 1.19f
		));

		// ----------------------
		skyParams.add(new SkyboxParams(
				14f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				15f, 2.5f, 1.72f
		));

		skyParams.add(new SkyboxParams(
				17f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.302f, 0.592f, 1f, 1f),
				16f, 2.09f, 1.50f
		));

		skyParams.add(new SkyboxParams(
				18f,
				new Color(0.302f, 0.592f, 1f, 1f),
				new Color(0.278f, 0.431f, 0.753f, 1f),
				21f, 2.01f, 1.19f
		));
		skyParams.add(new SkyboxParams(
				18.5f,
				new Color(0.278f, 0.431f, 0.753f, 1f),
				new Color(0.278f, 0.431f, 0.753f, 1f),
				23f, 1.9f, 1.05f
		));

		skyParams.add(new SkyboxParams(
				19f,
				new Color(0.494f, 0.475f, 0.42f, 1f),
				new Color(0.4444f, 0.62f, 0.79f, 1f),
				25f, 1.70f, 0.91f
		));

		skyParams.add(new SkyboxParams(
				20f,
				new Color(0.557f, 0.365f, 0.337f, 1f),
				new Color(0.557f, 0.365f, 0.337f, 1f),
				25f, 1.52f, 0.85f
		));

		skyParams.add(new SkyboxParams(
				21f,
				new Color(0.557f, 0.365f, 0.337f, 1f),
				new Color(0.557f, 0.365f, 0.337f, 1f),
				27f, 1.41f, 0.86f
		));

		skyParams.add(new SkyboxParams(
				22f,
				new Color(0.62f, 0.361f, 0.349f, 1f),
				new Color(0.52f, 0.261f, 0.249f, 1f),
				27f, 1.48f, 0.85f
		));

		skyParams.add(new SkyboxParams(
				24.01f,
				new Color(0.067f, 0.078f, 0.145f, 1f),
				new Color(0.11f, 0.13f, 0.175f, 1f),
				28f, 1.34f, 0.65f
		));
	}

	public Skybox()
	{
		_cubemap = new Cubemap(
				Gdx.files.internal("assets/skybox/nightRight.png"),
				Gdx.files.internal("assets/skybox/nightLeft.png"),
				Gdx.files.internal("assets/skybox/nightTop.png"),
				Gdx.files.internal("assets/skybox/nightBottom.png"),
				Gdx.files.internal("assets/skybox/nightBack.png"),
				Gdx.files.internal("assets/skybox/nightFront.png")

//				Gdx.files.internal("assets/skybox/right.png"),
//				Gdx.files.internal("assets/skybox/left.png"),
//				Gdx.files.internal("assets/skybox/top.png"),
//				Gdx.files.internal("assets/skybox/bottom.png"),
//				Gdx.files.internal("assets/skybox/back.png"),
//				Gdx.files.internal("assets/skybox/front.png")
		);
		_cubemap.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		_cubemapNight = new Cubemap(
				Gdx.files.internal("assets/skybox/nightRight.png"),
				Gdx.files.internal("assets/skybox/nightLeft.png"),
				Gdx.files.internal("assets/skybox/nightTop.png"),
				Gdx.files.internal("assets/skybox/nightBottom.png"),
				Gdx.files.internal("assets/skybox/nightBack.png"),
				Gdx.files.internal("assets/skybox/nightFront.png")
		);
		_cubemapNight.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

//		_shader = Render.makeShader("skybox2");
		_shader = Render.makeShader("skybox");

		makeMesh();

		_tuningWindow = new TuningWindow();
	}

	public void clear()
	{
//		_cubemap.dispose();
	}

	public void Render(Camera camera, Environment environment)
	{
		if (_tuningWindow != null) _tuningWindow.update();
		_shader.begin();
		prepareShader(camera, environment);

		_mesh.render(_shader, GL20.GL_TRIANGLES);
//		_mesh.render(_shader, GL20.GL_LINE_STRIP);

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
			SkyboxParams p = skyParams.get(idx);
			SkyboxParams plast = skyParams.get(idx + 1);
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
		_shader.setUniformMatrix("u_projViewTrans", camera.combined);

		final float PI = ((float) Math.PI);
		Matrix4 tmp;
		tmp = camera.view.cpy();
		tmp.translate(camera.position);
//		tmp.rotate(0, 1, 0, _rotate);
		_shader.setUniformMatrix("u_viewTrans", tmp);

		tmp = new Matrix4();
		if (Game.getInstance().getCamera().getChaseObj() != null)
		{
			tmp.translate(Game.getInstance().getCamera().getChaseObj().getWorldCoord().cpy().add(0, 0, 0));
		}
		_shader.setUniformMatrix("u_worldTrans", tmp);

		_shader.setUniformf("u_skyColor", fogColor.r, fogColor.g, fogColor.b);
		_shader.setUniformf("u_density", fogEnabled ? fogDensity : 0f);
		_shader.setUniformf("u_gradient", fogGradient);
//		_shader.setUniformf("u_backColor", 0.216f, 0.373f, 0.741f);

		if (_tuningWindow != null)
		{
			// ATMO
			float r = 1f;//(Math.max(Render.sunPosition.y, 300f) / 900f);
			float radius = _tuningWindow.getRadius();
			float cameraHeight = radius * _tuningWindow.getCameraHeight() * (r);
			float outRadius = _tuningWindow.getOutRadius();
//		float cameraHeight = radius + camera.position.y;

//			Vector3 cpos = camera.position.cpy().add(0, radius, 0);
//		_shader.setUniformf("u_cameraPosition", cpos.x, cpos.y, cpos.z);

			if (!takeFromTuningWindow)
			{
				radius = skyboxParams.radius;
				cameraHeight = radius * skyboxParams.cameraHeight;
				outRadius = skyboxParams.outerRadius;
				_shader.setUniformf("u_backColor", skyboxParams.skyColor);

				_tuningWindow._scrollbarRadius.setValue(Math.round(radius));
				_tuningWindow._scrollbarCameraHeight.setValue(Math.round(skyboxParams.cameraHeight * 100f));
				_tuningWindow._scrollbarOutRadius.setValue(Math.round(skyboxParams.outerRadius * 100f));
			}

			float Kr = 0.0025f;
			float Km = 0.0010f;
			float ESun = _tuningWindow.getEsun();// 15.0f;
			float g = -0.990f;
			float innerRadius = radius;
			float outerRadius = radius * outRadius;
			Vector3 waveLength = new Vector3(0.650f, 0.570f, 0.475f);
			float scaleDepth = 0.25f;
//			float mieScaleDepth = 0.1f;

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
		}

		_shader.setUniformf("v3LightPosition", sunPosition.cpy().nor().scl(1f));
		_shader.setUniformf("u_size", SIZE);

		Cubemap texture1;
		Cubemap texture2;

		float blendValue;

		if (_time >= 0 && _time < 5000)
		{
			texture1 = _cubemapNight;
			texture2 = _cubemapNight;
			blendValue = (_time - 0) / (5000 - 0);
		}
		else if (_time >= 5000 && _time < 8000)
		{
			texture1 = _cubemapNight;
			texture2 = _cubemap;
			blendValue = (_time - 5000) / (8000 - 5000);
		}
		else if (_time >= 8000 && _time < 21000)
		{
			texture1 = _cubemap;
			texture2 = _cubemap;
			blendValue = (_time - 8000) / (21000 - 8000);
		}
		else
		{
			texture1 = _cubemap;
			texture2 = _cubemapNight;
			blendValue = (_time - 21000) / (24000 - 21000);
		}

		_shader.setUniformf("u_blendValue", blendValue);

		texture1 = _cubemap;
		texture2 = _cubemapNight;
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE4);
		texture1.bind();
		_shader.setUniformi("u_texture1", 4);

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE5);
		texture2.bind();
		_shader.setUniformi("u_texture2", 5);
	}

	protected void makeMesh()
	{
//		_mesh = new Mesh(true, VERTICES.length / 3, 0,
//		                 new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position")
//		);
//		_mesh.setVertices(VERTICES);

		Icosahedron icosahedron = new Icosahedron(4, SIZE);
		_mesh = icosahedron.getMesh();
	}

	public void updateDayNight()
	{
		_time += Main.deltaTime * 500f;
		_time %= 24000;
	}

	/**
	 * разрешить движение солнца по небосводу
	 */
	public static boolean sunMoving = false;
	public static boolean takeFromTuningWindow = false;

	/**
	 * время дня в 24 часовом формате (0-24)
	 */
	public static float sunTime = 0f;

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

//		pos.rotate(90f - 5f, 1, 0, 0);
//		pos.rotate(sunAngle, 0, 0, 1);

		sunPosition.set(pos);

//		DirectionalLightsAttribute lights = ((DirectionalLightsAttribute) _environment.get(DirectionalLightsAttribute.Type));
//		lights.lights.get(0).set(0.8f, 0.8f, 0.8f, -sunPosition.x, -sunPosition.y, -sunPosition.z);
	}

}
