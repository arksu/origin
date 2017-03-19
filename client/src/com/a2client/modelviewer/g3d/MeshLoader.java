package com.a2client.modelviewer.g3d;

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
	private static final int ELEMENTS_BINORMAL_COUNT = 12;

	public static Mesh load(MyInputStream in) throws IOException
	{
		// flags
		boolean useBinormal = in.readByte() == 1;
		boolean useWeights = in.readByte() == 1;

		int vertCount = in.readInt();
		int elemCount = useBinormal ? ELEMENTS_BINORMAL_COUNT : ELEMENTS_COUNT;

		// если есть веса для костей - добавим еще элементов
		elemCount += useWeights ? Const.MAX_WEIGHTS * 2 : 0;

		int total = vertCount * elemCount * FLOAT_SIZE;
		float[] vertices = new float[vertCount * elemCount];

		// читаем все вершины в буфер
		byte[] bytes = new byte[total];
		int totalReaded = 0;
		do
		{
			int r = in.read(bytes, totalReaded, total - totalReaded);
			if (r < 0)
			{
				throw new RuntimeException("error read vertex buffer");
			}
			totalReaded += r;
		}
		while (totalReaded < total);

		ByteBuffer buf = ByteBuffer.allocate(total);
		buf.clear();
		buf.put(bytes);
		buf.rewind();
		buf.asFloatBuffer().get(vertices);

		// читаем все индексы в буфер
		int indexCount = in.readInt();
		short[] index = new short[indexCount * 3];
		total = indexCount * 3 * INDEX_SIZE;
		bytes = new byte[total];
		totalReaded = 0;
		do
		{
			int r = in.read(bytes, totalReaded, total - totalReaded);
			if (r < 0)
			{
				throw new RuntimeException("error read index buffer");
			}
			totalReaded += r;
		}
		while (totalReaded < total);

		buf = ByteBuffer.allocate(total);
		buf.clear();
		buf.put(bytes);
		buf.rewind();
		buf.asShortBuffer().get(index);

		// создаем меш - передаем туда буферы
		Mesh mesh;
		if (useBinormal)
		{
			if (useWeights)
			{
				mesh = new Mesh(
						true, vertCount,
						index.length,
						new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.BiNormal, 4, ShaderProgram.BINORMAL_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
						new VertexAttribute(VertexAttributes.Usage.BoneWeight, 2, "a_bone0"),
						new VertexAttribute(VertexAttributes.Usage.BoneWeight, 2, "a_bone1")
				);
			}
			else
			{
				mesh = new Mesh(
						true, vertCount,
						index.length,
						new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.BiNormal, 4, ShaderProgram.BINORMAL_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
				);
			}
		}
		else
		{
			if (useWeights)
			{
				mesh = new Mesh(
						true, vertCount,
						index.length,
						new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
						new VertexAttribute(VertexAttributes.Usage.BoneWeight, 2, "a_bone0"),
						new VertexAttribute(VertexAttributes.Usage.BoneWeight, 2, "a_bone1")
				);
			}
			else
			{
				mesh = new Mesh(
						true, vertCount,
						index.length,
						new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
						new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
				);
			}
		}
		mesh.setVertices(vertices);
		mesh.setIndices(index);
		return mesh;
	}
}
