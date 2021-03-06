package com.a2client.g3d;

import com.a2client.Config;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
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

	/**
	 * сколько всего полигонов
	 */
	private int _totalTriCount;

	/**
	 * имя которое указали при загрузке
	 */
	private final String _name;

	private SkeletonData _skeletonData;

	private final Map<String, AnimationData> _animations = new HashMap<>();

	public ModelData(String name)
	{
		_log.debug("loading model: " + name);
		_name = name;
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
			// flag skeleton
			if (in.readByte() > 0)
			{
				loadSkeleton(in);
				// flag anims
				if (in.readByte() > 0)
				{
					int animationsCount = in.readWord();
					while (animationsCount > 0)
					{
						loadAnimation(in);
						animationsCount--;
					}
				}
			}
			// mesh exists anyway
			loadMesh(in);
		}
		catch (Exception e)
		{
			_log.error("failed load model " + name, e);
		}
	}

	private void loadSkeleton(MyInputStream in) throws IOException
	{
		_skeletonData = SkeletonLoader.load(in);
	}

	private void loadAnimation(MyInputStream in) throws IOException
	{
		AnimationData data = AnimationLoader.load(in, _skeletonData);
		_log.debug("loaded anim: " + data.getName() + " frames: " + data.getFramesCount() + " fps: " + data.getFps());
		_animations.put(data.getName(), data);
	}

	/**
	 * загрузить меш из потока
	 */
	private void loadMesh(MyInputStream in) throws IOException
	{
		_totalTriCount = 0;
		int meshCount = in.readInt();
		List<Mesh> tmpList = new LinkedList<>();

		// грузим сначала все меши из файла
		Map<String, Mesh> map = new HashMap<>();

		while (meshCount > 0)
		{
			String name = in.readAnsiString().toLowerCase();
			Mesh mesh = MeshLoader.load(in);
			_totalTriCount += mesh.getNumIndices() / 3;
			// для моделей используем батчинг. поэтому бинд буферов будет только там
			mesh.setAutoBind(false);

			tmpList.add(mesh);
			map.put(name, mesh);

			meshCount--;
		}
		_log.debug("total tri count: " + _totalTriCount + " in " + tmpList.size() + " mesh");

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
				ModelDesc.Material desc = new ModelDesc.Material();
				String fname = _name + ".png";
				if (!MyInputStream.fileExists(fname))
				{
					fname = null;
				}
				desc.diffuse = fname;
				_defaultMaterial = new Material(desc);
			}
			else
			{
				_defaultMaterial = new Material(descMaterial);
			}
			for (Mesh mesh : tmpList)
			{
				_defaultGroup.add(mesh);
			}
		}
	}

	/**
	 * есть только один меш?
	 */
	public boolean isOneMesh()
	{
		return _defaultGroup != null && _defaultGroup.size() == 1;
	}

	/**
	 * рендер без указания группы. выводим ВСЕ возможное (все группы)
	 */
	public void render(ModelBatch modelBatch, int primitiveType)
	{
		if (_defaultGroup != null)
		{
			if (!modelBatch.isShadowMode() || _defaultMaterial.isCastShadows())
			{
				modelBatch.bindMaterial(_defaultMaterial);
				for (Mesh mesh : _defaultGroup)
				{
					// биндим меш через батчер, там проверим текущий меш и только тогда будет бинд если реально нужно
					modelBatch.bindMesh(mesh);
					mesh.render(modelBatch.getShader(), primitiveType);
				}
			}
		}
		else
		{
			for (Map.Entry<String, List<Mesh>> entry : _meshGroups.entrySet())
			{
				Material material = _meshMaterials.get(entry.getKey());
				if (!modelBatch.isShadowMode() || material.isCastShadows())
				{
					modelBatch.bindMaterial(material);
					for (Mesh mesh : entry.getValue())
					{
						// биндим меш через батчер, там проверим текущий меш и только тогда будет бинд если реально нужно
						modelBatch.bindMesh(mesh);
						mesh.render(modelBatch.getShader(), primitiveType);
					}
				}
			}
		}
	}

	/**
	 * обсчитать bound box
	 */
	public void extendBoundingBox(BoundingBox boundingBox, Matrix4 worldTransform)
	{
		if (_defaultGroup != null)
		{
			for (Mesh mesh : _defaultGroup)
			{
				mesh.extendBoundingBox(boundingBox, 0, mesh.getNumIndices(), worldTransform);
			}
		}
		else
		{
			for (Map.Entry<String, List<Mesh>> entry : _meshGroups.entrySet())
			{
				for (Mesh mesh : entry.getValue())
				{
					mesh.extendBoundingBox(boundingBox, 0, mesh.getNumIndices(), worldTransform);
				}
			}
		}
	}

	public SkeletonData getSkeletonData()
	{
		return _skeletonData;
	}

	public AnimationData getAnimation(String name)
	{
		return _animations.get(name);
	}

	public Map<String, AnimationData> getAnimations()
	{
		return _animations;
	}
}
