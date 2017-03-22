package com.a2client.g3d;

import java.util.List;
import java.util.Map;

/**
 * описание модельки
 * Created by arksu on 13.03.17.
 */
public class ModelDesc
{
	/**
	 * материал для дефолтной группы мешей
	 */
	Material material;

	/**
	 * описываем группы мешей, чтобы можно было указывать какую именно группу рендеретить
	 * если не указано, все меши из модельки идут в дефолтную группу
	 */
	Map<String, MeshGroup> meshgroups;

	public static class MeshGroup
	{
		/**
		 * имена мешей в файле которые будут добавлены в эту грппу
		 */
		List<String> names;

		/**
		 * материал для этой группы
		 */
		Material material;
	}

	public static class Material
	{
		/**
		 * дифуз мапа
		 */
		String diffuse;

		/**
		 * нормал мапа, если указана в шейдер передается флаг и считаются TBN
		 */
		String normal;

		/**
		 * спекуляр мапа, если указана в шейдер передается флаг
		 */
		String specular;

		/**
		 * фильтрация текстур (linear, nearest)
		 */
		String filter;

		/**
		 * дает ли тени этот меш
		 */
		boolean castShadows = true;

		/**
		 * принимает ли на себя тени этот меш
		 */
		boolean receiveShadows = true;
	}
}
