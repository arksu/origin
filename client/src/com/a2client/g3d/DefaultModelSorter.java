package com.a2client.g3d;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

/**
 * сортировщик моделей для батчинга
 * see {@link DefaultRenderableSorter}
 * Created by arksu on 15.03.17.
 */
public class DefaultModelSorter implements ModelSorter, Comparator<Model>
{
	private Camera camera;
	private final Vector3 tmpV1 = new Vector3();
	private final Vector3 tmpV2 = new Vector3();

	@Override
	public void sort(Camera camera, Array<Model> renderables)
	{
		this.camera = camera;
		renderables.sort(this);
	}

	private Vector3 getTranslation(Matrix4 worldTransform, Vector3 center, Vector3 output)
	{
		if (center.isZero())
		{
			worldTransform.getTranslation(output);
		}
		else if (!worldTransform.hasRotationOrScaling())
		{
			worldTransform.getTranslation(output).add(center);
		}
		else
		{
			output.set(center).mul(worldTransform);
		}
		return output;
	}

	@Override
	public int compare(Model o1, Model o2)
	{
		// прозрачность
//		final boolean b1 = o1.material.has(BlendingAttribute.Type) && ((BlendingAttribute) o1.material.get(BlendingAttribute.Type)).blended;
//		final boolean b2 = o2.material.has(BlendingAttribute.Type) && ((BlendingAttribute) o2.material.get(BlendingAttribute.Type)).blended;
//		if (b1 != b2) return b1 ? 1 : -1;

		// FIXME implement better sorting algorithm
		// final boolean same = o1.shader == o2.shader && o1.mesh == o2.mesh && (o1.lights == null) == (o2.lights == null) &&
		// o1.material.equals(o2.material);

		// сортируем по данным (меш и материал).
		if (o1.getData() != o2.getData()) return o1.getData().hashCode() > o2.getData().hashCode() ? 1 : -1;

		// TODO по количеству меш групп. то есть вообще есть больше одной или нет
		// модели где нету групп должны идти вместе и уже их сортировать по мешам. чтобы не было переключений vbo
		final boolean d1 = o1.getData().isOneMesh();
		final boolean d2 = o2.getData().isOneMesh();
		if (d1 != d2) return d1 ? 1 : -1;

		// сортировка по расстоянию от камеры
//		getTranslation(o1.worldTransform, o1.meshPart.center, tmpV1);
//		getTranslation(o2.worldTransform, o2.meshPart.center, tmpV2);
//		final float dst = (int) (1000f * camera.position.dst2(tmpV1)) - (int) (1000f * camera.position.dst2(tmpV2));
//		final int result = dst < 0 ? -1 : (dst > 0 ? 1 : 0);
//		return b1 ? -result : result;
		return 0;
	}
}
