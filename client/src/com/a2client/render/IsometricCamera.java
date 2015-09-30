package com.a2client.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

public class IsometricCamera extends OrthographicCamera
{

	//	private final Vector3 tmp = new Vector3();

	public IsometricCamera(float camWidth, float camHeight)
	{
		super(camWidth, camHeight);
	}

	@Override
	public void update(boolean updateFrustum)
	{
		projection.setToOrtho(zoom * -viewportWidth / 2, zoom * (viewportWidth / 2), zoom * -(viewportHeight / 2),
							  zoom * viewportHeight / 2, near, far);

		view.idt();
		view.scale((float) (Math.sqrt(2.0) / 2.0), (float) (Math.sqrt(2.0) / 4.0), 1.0f);
		view.rotate(0.0f, 0.0f, 1.0f, -25.0f);
		view.translate(position);

		combined.set(projection);
		Matrix4.mul(combined.val, view.val);

		if (updateFrustum)
		{
			invProjectionView.set(combined);
			Matrix4.inv(invProjectionView.val);
			frustum.update(invProjectionView);
		}
	}

}
