package com.a2client.modelviewer;

import com.a2client.Config;
import com.a2client.corex.MyInputStream;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * данные для визуализации 3д модели
 * Created by arksu on 13.03.17.
 */
public class ModelData
{
	private static final Logger _log = LoggerFactory.getLogger(ModelData.class.getName());

	private static Gson _gson = new Gson();

	/**
	 * дескриптор модели
	 */
	private ModelDesc _desc;

	/**
	 * меши по группам
	 */
	private Map<String, List<Mesh>> _meshGroups = new HashMap<>();
	private Map<String, Material> _meshMaterials = new HashMap<>();

	/**
	 * если в этой модели вообще не прудсмотрено групп - храним массив мешей в явном виде
	 */
	private List<Mesh> _defaultGroup;
	private Material _defaultMaterial;

	public ModelData(String name)
	{
		_log.debug("load model: " + name);
		File file = Gdx.files.internal(Config.MODELS_DIR + name + ".json").file();
		try
		{
			_desc = _gson.fromJson(new FileReader(file), ModelDesc.class);
		}
		catch (FileNotFoundException e)
		{
			_log.debug("desc for model: <" + name + "> not found");
			_desc = new ModelDesc();
		}

		try
		{
			MyInputStream in = MyInputStream.fromFile(name + ".mdl");
			loadMesh(in);

			// TODO
//			loadSkeleton(in);
		}
		catch (Exception e)
		{
			_log.error("failed load model " + name, e);
		}
	}

	private void loadMesh(MyInputStream in) throws IOException
	{
		int meshCount = in.readInt();
		List<Mesh> tmpList = new LinkedList<>();

		// грузим сначала все меши из файла
		Map<String, Mesh> map = new HashMap<>();

		while (meshCount > 0)
		{
			String name = in.readAnsiString().toLowerCase();
			Mesh mesh = MeshLoader.load(in);

			tmpList.add(mesh);
			map.put(name, mesh);

			meshCount--;
		}

		// если группы для этой модели есть. надо их сформировать
		if (_desc.meshgroups != null)
		{
			for (Map.Entry<String, ModelDesc.MeshGroup> entry : _desc.meshgroups.entrySet())
			{
				String groupName = entry.getKey();
				ModelDesc.Material descMaterial = entry.getValue().material;
				if (descMaterial == null)
				{
					throw new RuntimeException("no material in group: " + groupName);
				}
				Material material = new Material(descMaterial);
				List<Mesh> tmpData = new LinkedList<>();
				// выдираем поименно все меши
				for (String name : entry.getValue().names)
				{
					Mesh mesh = map.get(name);
					if (mesh != null)
					{
						tmpData.add(mesh);
					}
				}
				_meshGroups.put(groupName, tmpData);
				_meshMaterials.put(groupName, material);
			}
		}
		else
		{
			// групп в этой модели нет
			_defaultGroup = new LinkedList<>();
			ModelDesc.Material descMaterial = _desc.material;
			if (descMaterial == null)
			{
				throw new RuntimeException("no default material");
			}
			_defaultMaterial = new Material(descMaterial);
			for (Mesh mesh : tmpList)
			{
				_defaultGroup.add(mesh);
			}
		}
	}

	/**
	 * рендер без указания группы. выводим ВСЕ возможное (все группы)
	 */
	public void render(ShaderProgram shader, int mode)
	{
		if (_defaultGroup != null)
		{
			_defaultMaterial.bind(shader);
			for (Mesh mesh : _defaultGroup)
			{
				mesh.render(shader, mode);
			}
		}
		else
		{
			for (Map.Entry<String, List<Mesh>> entry : _meshGroups.entrySet())
			{
				Material material = _meshMaterials.get(entry.getKey());
				material.bind(shader);
				for (Mesh mesh : entry.getValue())
				{
					mesh.render(shader, mode);
				}
			}
		}
	}
}
