package com.a2client.render.skybox;

import com.badlogic.gdx.graphics.Color;

/**
 * параметры для тонкой настройки скайбокса (хардкод и костыль)
 * Created by arksu on 05.03.17.
 */
public class SkyboxParams
{
	public float sunTime;
	public Color skyColor;
	public Color fogColor;

	public float radius;
	public float outerRadius;
	public float cameraHeight;

	public SkyboxParams(float sunTime, Color skyColor, Color fogColor, float radius, float outerRadius, float cameraHeight)
	{
		this.sunTime = sunTime;
		this.skyColor = skyColor;
		this.fogColor = fogColor;
		this.radius = radius;
		this.outerRadius = outerRadius;
		this.cameraHeight = cameraHeight;
	}

	public SkyboxParams(SkyboxParams p1, SkyboxParams p2)
	{
		float len = p2.sunTime - p1.sunTime;
		float k = (Skybox.sunTime - p1.sunTime) / len;

		radius = (p2.radius - p1.radius) * k + p1.radius;
		outerRadius = (p2.outerRadius - p1.outerRadius) * k + p1.outerRadius;
		cameraHeight = (p2.cameraHeight - p1.cameraHeight) * k + p1.cameraHeight;

		float r = (p2.skyColor.r - p1.skyColor.r) * k + p1.skyColor.r;
		float g = (p2.skyColor.g - p1.skyColor.g) * k + p1.skyColor.g;
		float b = (p2.skyColor.b - p1.skyColor.b) * k + p1.skyColor.b;
		skyColor = new Color(r, g, b, 1f);

		r = (p2.fogColor.r - p1.fogColor.r) * k + p1.fogColor.r;
		g = (p2.fogColor.g - p1.fogColor.g) * k + p1.fogColor.g;
		b = (p2.fogColor.b - p1.fogColor.b) * k + p1.fogColor.b;
		fogColor = new Color(r, g, b, 1f);
	}
}
