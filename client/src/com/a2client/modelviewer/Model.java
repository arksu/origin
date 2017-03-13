package com.a2client.modelviewer;

import com.a2client.corex.MyInputStream;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by arksu on 13.03.17.
 */
public class Model
{
	private Mesh[] _meshes;

	public Model(MyInputStream in)
	{
		try
		{
			int meshCount = in.readInt();
			_meshes = new Mesh[meshCount];

			int idx = 0;
			while (meshCount > 0)
			{
				Mesh mesh = MeshLoader.load(in);
				_meshes[idx] = mesh;
				meshCount--;
				idx++;
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void render(ShaderProgram shader, int mode)
	{
		for (Mesh mesh : _meshes)
		{
			mesh.render(shader, mode);
		}
	}
}
