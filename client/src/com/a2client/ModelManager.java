package com.a2client;

import com.a2client.modelviewer.g3d.Model;
import com.a2client.modelviewer.g3d.ModelData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.TimeUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ModelManager
{
	private static final Logger _log = LoggerFactory.getLogger(ModelManager.class.getName());

	private static long MODEL_TIMEOUT = 120000; // 2min

	private static ModelManager _instance;

	private static Gson _gson = new Gson();

	public static ModelManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ModelManager();
		}

		return _instance;
	}

//	private ObjectMap<ModelData, IntArray> _modelHitList = new ObjectMap<>();

	private ObjectMap<Integer, ModelDesc> _modelList = new ObjectMap<>();

	private long current_time = TimeUtils.millis();

	public ModelManager()
	{
		loadModelList();
	}

	public Model getModelByType(int typeId)
	{

		ModelDesc desc = _modelList.get(typeId);

		if (desc == null)
		{
			desc = _modelList.get(0);
//			throw new GdxRuntimeException("no model by typeId: " + typeId);
		}

		if (!desc._loaded)
		{
			load(desc);
		}

		return new Model(desc._modelData);
	}

	public ModelDesc getDescByType(int typeId)
	{
		ModelDesc desc = _modelList.get(typeId);
		if (desc == null)
		{
			desc = _modelList.get(0);
		}
		return desc;
	}

	private void load(ModelDesc desc)
	{
		if (desc._modelData == null)
		{
			desc._modelData = new ModelData(desc._model);
			_log.debug("loaded model: " + desc._model);
		}

		desc._loaded = true;
		desc._lastUsage = TimeUtils.millis();
	}

	public void loadModelList()
	{
		File file = Gdx.files.internal("assets/objects.json").file();
		try
		{
			ModelDesc[] list = _gson.fromJson(new FileReader(file), ModelDesc[].class);
			for (ModelDesc item : list)
			{
				item._lastUsage = TimeUtils.millis();
				_modelList.put(item._typeId, item);
			}
		}
		catch (FileNotFoundException e)
		{
			_log.error("objects config not found", e);
			System.exit(-1);
		}
	}

	public static class ModelDesc
	{
		public transient boolean _loaded = false;
		public transient long _lastUsage = 0;

		@SerializedName("typeId")
		public int _typeId;

		@SerializedName("model")
		public String _model;

		@SerializedName("scale")
		public float _scale = 1f;

		private transient ModelData _modelData;
	}
}
