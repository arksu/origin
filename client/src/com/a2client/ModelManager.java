package com.a2client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.*;
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

	private ObjectMap<Model, IntArray> _modelHitList = new ObjectMap<>();

	private ObjectMap<Integer, ModelDesc> _modelList = new ObjectMap<>();

	private AssetManager _assets;

	private long current_time = TimeUtils.millis();

	public ModelManager()
	{
		_assets = Main.getAssetManager();
		loadModelList();
		Timer.schedule(new UpdateTimer(this), 0, 30);
	}

	public ModelInstance getModelByType(int typeId)
	{

		ModelDesc desc = _modelList.get(typeId);

		if (desc == null)
		{
			throw new GdxRuntimeException(
					"no model by typeId: " + typeId);
		}

		if (!desc._loaded)
		{
			load(desc);
		}

		ModelInstance tmp = new ModelInstance(_assets.get(desc._resource, Model.class));
		desc._lastUsage = TimeUtils.millis();
		tmp.transform.scale(desc._scale, desc._scale, desc._scale);
		return tmp;
	}

	public ModelDesc getDescByType(int typeId)
	{
		return _modelList.get(typeId);
	}

	public void updateModelTime(int typeId)
	{
		ModelDesc meta = _modelList.get(typeId);

		if (meta == null)
		{
			return;
		}

		meta._lastUsage = current_time;
	}

	public void update()
	{
		current_time = TimeUtils.millis();
		long out_time = TimeUtils.millis() - MODEL_TIMEOUT;

//		for (ModelDesc meta : _modelList.values())
//		{
//			if (meta._loaded && meta._lastUsage < out_time)
//			{
//				unload(meta);
//			}
//		}
	}

	private void load(ModelDesc desc)
	{
		if (!_assets.isLoaded(desc._resource))
		{
			_assets.load(desc._resource, Model.class);
			_assets.finishLoadingAsset(desc._resource);
			_log.debug("loaded model: " + desc._resource);
		}
		Model model = _assets.get(desc._resource, Model.class);
		if (!_modelHitList.containsKey(model))
		{
			_modelHitList.put(model, new IntArray());
		}
//		model.
		_modelHitList.get(model).add(desc._typeId);
		desc._loaded = true;
		desc._lastUsage = TimeUtils.millis();
	}

	private void unload(ModelDesc desc)
	{
		Model model = _assets.get(desc._resource, Model.class);

		IntArray hitArray = _modelHitList.get(model);
		hitArray.removeValue(desc._typeId);
		if (hitArray.size == 0)
		{
			_log.debug("unLoad model: " + desc._resource);
			_assets.unload(desc._resource);
			_modelHitList.remove(model);
		}
		desc._loaded = false;
		desc._lastUsage = 0;
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
			e.printStackTrace();
		}

	}

	public static class ModelDesc
	{
		public transient boolean _loaded = false;
		public transient long _lastUsage = 0;

		@SerializedName("typeId")
		public int _typeId;

		@SerializedName("res")
		public String _resource;

		@SerializedName("scale")
		public float _scale = 1f;
	}

	private class UpdateTimer extends Timer.Task
	{
		private ModelManager _manager;

		UpdateTimer(ModelManager manager)
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
