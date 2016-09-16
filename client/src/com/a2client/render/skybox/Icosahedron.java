package com.a2client.render.skybox;

import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 16.09.16.
 */
public class Icosahedron
{
	private static final Logger _log = LoggerFactory.getLogger(Icosahedron.class.getName());

	private static final float X = 0.525731112119133606f;

	private static final float Z = 0.850650808352039932f;

	private static final float[][] vdata = {
			{-X, 0f, Z}, {X, 0f, Z}, {-X, 0f, -Z}, {X, 0f, -Z},
			{0f, Z, X}, {0f, Z, -X}, {0f, -Z, X}, {0f, -Z, -X},
			{Z, X, 0f}, {-Z, X, 0f}, {Z, -X, 0f}, {-Z, -X, 0f}
	};

	private static final int[][] indices = {
			{0, 4, 1}, {0, 9, 4}, {9, 5, 4}, {4, 5, 8}, {4, 8, 1},
			{8, 10, 1}, {8, 3, 10}, {5, 3, 8}, {5, 2, 3}, {2, 7, 3},
			{7, 10, 3}, {7, 6, 10}, {7, 11, 6}, {11, 0, 6}, {0, 1, 6},
			{6, 1, 10}, {9, 0, 11}, {9, 11, 2}, {9, 2, 5}, {7, 2, 11}
	};

	public static Mesh getMesh(float size)
	{
		Mesh mesh = new Mesh(true, 36, 60, new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));

		int ii = 0;
		float[] vert = new float[vdata.length * 3];
		for (int i = 0; i < vdata.length; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				vert[ii] = vdata[i][j] * size;
				ii++;
			}
		}

		short[] idx = new short[indices.length * 3];
		ii = 0;
		for (int i = 0; i < indices.length; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				idx[ii] = (short) indices[i][j];
				ii++;
			}
		}

		mesh.setVertices(vert);
		mesh.setIndices(idx);

		return mesh;
	}

	public static Mesh mesh;

	public static ShaderProgram shader;

	public static void init() {
		mesh= getMesh(2.1f);
		shader = Render.makeShader("simple", "simple");
	}

	public static void render(GameCamera camera)
	{
		shader.begin();

		shader.setUniformMatrix("u_projTrans", camera.projection);
		shader.setUniformMatrix("u_projViewTrans", camera.combined);

		Matrix4 tmp;
		tmp = camera.view.cpy();
		tmp.translate(camera.position);
//		tmp.rotate(0, 1, 0, _rotate);
		shader.setUniformMatrix("u_viewTrans", tmp);

		tmp = new Matrix4();
		if (camera.getChaseObj() != null)
		{
			tmp.translate(camera.getChaseObj().getWorldCoord().cpy().add(0, 0, 0));
		}
		shader.setUniformMatrix("u_worldTrans", tmp);

		mesh.render(shader, GL20.GL_LINE_STRIP);

		shader.end();
	}

}
