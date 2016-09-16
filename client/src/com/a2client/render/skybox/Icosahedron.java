package com.a2client.render.skybox;

import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

import java.util.ArrayList;
import java.util.List;

/**
 * икосаэдр для создания сфер
 * Created by arksu on 16.09.16.
 */
public class Icosahedron
{
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

	private List<Float> _vertices = new ArrayList<>();
	private List<Float> _normals = new ArrayList<>();

	private Mesh _mesh;
	private float _mult;

	/**
	 * создать икосаэдр
	 * @param depth глубина для деления треугольников
	 * @param mult на это умножаем кажду вершину (радиус)
	 */
	public Icosahedron(int depth, float mult)
	{
		_mult = mult;
		for (int i = 0; i < indices.length; i++)
		{
			subdivide(
					vdata[indices[i][0]],
					vdata[indices[i][1]],
					vdata[indices[i][2]],
					depth
			);
		}
		_mesh = new Mesh(
				true, _vertices.size(), 0,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE)
//				new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)
		);

		float[] v = new float[_vertices.size()];
		for (int i = 0; i < v.length; i++)
		{
			v[i] = _vertices.get(i);
		}
		_mesh.setVertices(v);
	}

	private void subdivide(float v1[], float v2[], float v3[], int depth)
	{
		if (depth == 0)
		{
			add(v1);
			add(v2);
			add(v3);
			return;
		}

		float v12[] = new float[3];
		float v23[] = new float[3];
		float v31[] = new float[3];

		for (int i = 0; i < 3; ++i)
		{
			v12[i] = (v1[i] + v2[i]) / 2f;
			v23[i] = (v2[i] + v3[i]) / 2f;
			v31[i] = (v3[i] + v1[i]) / 2f;
		}

		norm(v12);
		norm(v23);
		norm(v31);

		subdivide(v1, v12, v31, depth - 1);
		subdivide(v2, v23, v12, depth - 1);
		subdivide(v3, v31, v23, depth - 1);
		subdivide(v12, v23, v31, depth - 1);
	}

	private void norm(float v[])
	{

		float len = 0;

		for (int i = 0; i < 3; ++i)
		{
			len += v[i] * v[i];
		}

		len = (float) Math.sqrt(len);

		for (int i = 0; i < 3; ++i)
		{
			v[i] /= len;
		}
	}

	private void add(float v[])
	{
		for (int k = 0; k < 3; ++k)
		{
			_vertices.add(v[k] * _mult);
			_normals.add(v[k]);
		}
	}

	public Mesh getMesh()
	{
		return _mesh;
	}

	// DEBUG PART !! ====================================================
	public static Mesh getMesh(float size)
	{
		Icosahedron icosahedron = new Icosahedron(4, 1f);

		return icosahedron.getMesh();
	}

	public static Mesh mesh;

	public static ShaderProgram shader;

	public static void init()
	{
		mesh = getMesh(2.1f);
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
