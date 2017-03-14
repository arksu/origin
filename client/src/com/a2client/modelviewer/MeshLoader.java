package com.a2client.modelviewer;

import com.a2client.corex.MyInputStream;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * загрузка мешей из файла в память и создание необходимых буферов в gpu
 * Created by arksu on 12.03.17.
 */
public class MeshLoader
{
	private static final int FLOAT_SIZE = 4;
	private static final int INDEX_SIZE = 2;

	/**
	 * сколько чисел хранится на одну вершину? coord, norm, uv; 3 + 3 + 2 = 8
	 */
	private static final int ELEMENTS_COUNT = 8;

	public static Mesh load(MyInputStream in) throws IOException
	{
		int vertCount = in.readInt();
		int total = vertCount * ELEMENTS_COUNT * FLOAT_SIZE;
		float[] vertices = new float[vertCount * ELEMENTS_COUNT];

		byte[] bytes = new byte[total];
		int readed = in.read(bytes);
		if (readed != total)
		{
			throw new RuntimeException("MeshLoader load wrong bytes, total=" + total + " readed=" + readed);
		}
		ByteBuffer buf = ByteBuffer.allocate(total);
		buf.clear();
		buf.put(bytes);
		buf.rewind();
		buf.asFloatBuffer().get(vertices);

		int indexCount = in.readInt();
		short[] index = new short[indexCount * 3];
		total = indexCount * 3 * INDEX_SIZE;
		bytes = new byte[total];
		readed = in.read(bytes);
		if (readed != total)
		{
			throw new RuntimeException("MeshLoader load wrong bytes, total=" + total + " readed=" + readed);
		}
		buf = ByteBuffer.allocate(total);
		buf.clear();
		buf.put(bytes);
		buf.rewind();
		buf.asShortBuffer().get(index);

		Mesh mesh = new Mesh(
				true, vertCount,
				index.length,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
		);
		mesh.setVertices(vertices);
		mesh.setIndices(index);
		return mesh;
	}
}
