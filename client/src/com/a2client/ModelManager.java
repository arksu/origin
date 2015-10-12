package com.a2client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.*;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ModelManager
{
	//TODO: Вые... арка за это!
	//П.С Смотри стек вызова функции .debug
	private static final org.slf4j.Logger _log = LoggerFactory.getLogger(ModelManager.class.getName());

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

	private ObjectMap<Model, IntArray> _modelHitList = new ObjectMap<>();

	private ObjectMap<Integer, ModelMeta> _modelList = new ObjectMap<>();

	private AssetManager _assets;

	private long current_time = TimeUtils.millis();

	public ModelManager()
	{
		_assets = Main.getAssetManager();
		loadModelList();
		Timer.schedule(new UpdateTimer(this), 0, 30);
	}

	public ModelInstance getModelByType(int _typeId)
	{

		ModelMeta meta = _modelList.get(_typeId);

		if (meta == null)
		{
			throw new GdxRuntimeException(
					"[ModelManager] no model by typeId = " + _typeId);
		}

		if (!meta._loaded)
		{
			load(meta);
		}

		ModelInstance tmp = new ModelInstance(
				_assets.get(meta.res, Model.class));
		meta._lastUsage = TimeUtils.millis();
		tmp.transform.scale(meta.scaleX, meta.scaleY, meta.scaleZ);
		return tmp;
	}

	public void updateModelTime(int _typeId)
	{
		ModelMeta meta = _modelList.get(_typeId);

		if (meta == null)
		{
			return;
		}

		meta._lastUsage = current_time;
	}

	public void update()
	{
		_log.debug("ModelManager", "Update()");
		current_time = TimeUtils.millis();
		long out_time = TimeUtils.millis() - MODEL_TIMEOUT;

		for (ModelMeta meta : _modelList.values())
		{
			if (meta._loaded && meta._lastUsage < out_time)
			{
				unload(meta);
			}
		}
	}

	private void load(ModelMeta meta)
	{
		if (!_assets.isLoaded(meta.res))
		{
			_assets.load(meta.res, Model.class);
			_assets.finishLoadingAsset(meta.res);
			_log.debug("ModelManager", "Loaded = " + meta.res);
		}
		Model model = _assets.get(meta.res, Model.class);
		if (!_modelHitList.containsKey(model))
		{
			_modelHitList.put(model, new IntArray());
		}
		_modelHitList.get(model).add(meta.typeId);
		meta._loaded = true;
		meta._lastUsage = TimeUtils.millis();
	}

	private void unload(ModelMeta meta)
	{
		Model model = _assets.get(meta.res, Model.class);

		IntArray hitArray = _modelHitList.get(model);
		hitArray.removeValue(meta.typeId);
		if (hitArray.size == 0)
		{
			_log.debug("ModelManager", "UnLoad = " + meta.res);
			_assets.unload(meta.res);
			_modelHitList.remove(model);
		}
		meta._loaded = false;
		meta._lastUsage = 0;
	}

	public void loadModelList()
	{
		File file = Gdx.files.internal("assets/objects.json").file();
		try
		{
			ModelMeta[] list = _gson.fromJson(new FileReader(file), ModelMeta[].class);
			for (ModelMeta item : list)
			{
				item._lastUsage = TimeUtils.millis();
				_modelList.put(item.typeId, item);
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

	}

	public static class ModelMeta
	{
		public transient boolean _loaded = false;
		public transient long _lastUsage = 0;

		public int typeId;
		public String res;

		public float scaleX = 1f;
		public float scaleY = 1f;
		public float scaleZ = 1f;
	}

	class UpdateTimer extends Timer.Task
	{

		private ModelManager _manager;

		public UpdateTimer(ModelManager manager)
		{
			_manager = manager;
		}

		@Override
		public void run()
		{
			_manager.update();
		}

	}
}
