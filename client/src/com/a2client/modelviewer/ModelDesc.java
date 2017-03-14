package com.a2client.modelviewer;

import java.util.List;
import java.util.Map;

/**
 * Created by arksu on 13.03.17.
 */
public class ModelDesc
{
	Material material;
	Map<String, MeshGroup> meshgroups;

	public static class MeshGroup
	{
		List<String> names;
		Material material;
	}

	public static class Material
	{
		String diffuse;
		String normal;
		String specular;
		String filter;
		boolean castShadows = true;
		boolean receiveShadows = true;
	}
}
