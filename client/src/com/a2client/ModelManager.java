package com.a2client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.*;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ModelManager
{
	private static final org.slf4j.Logger _log = LoggerFactory.getLogger(ModelManager.class.getName());

	private static long MODEL_TIMEOUT = 120000; // 2min

	private static ModelManager _instance;

	public static ModelManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ModelManager();
		}

		return _instance;
	}

	private ObjectMap<Model, IntArray> _modelHitList = new ObjectMap<Model, IntArray>();

	private ObjectMap<Integer, ModelMeta> _modelList = new ObjectMap<Integer, ModelMeta>();

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

		if (!meta.loaded)
		{
			load(meta);
		}

		ModelInstance tmp = new ModelInstance(
				_assets.get(meta.res, Model.class));
		meta.last_usage = TimeUtils.millis();
		// tmp.transform = meta.transform;
		return tmp;
	}

	public void updateModelTime(int _typeId)
	{
		ModelMeta meta = _modelList.get(_typeId);

		if (meta == null)
		{
			return;
		}

		meta.last_usage = current_time;
	}

	public void update()
	{
		_log.debug("ModelManager", "Update()");
		current_time = TimeUtils.millis();
		long out_time = TimeUtils.millis() - MODEL_TIMEOUT;

		for (ModelMeta meta : _modelList.values())
		{
			if (meta.loaded && meta.last_usage < out_time)
			{
				unload(meta);
			}
		}
	}

	private void load(ModelMeta meta)
	{
		if (!_assets.isLoaded(meta.res)) {
			_assets.load(meta.res, Model.class);
			_assets.finishLoadingAsset(meta.res);
			_log.debug("ModelManager", "Loaded = " + meta.res);
		}
		Model model = _assets.get(meta.res, Model.class);
		if (!_modelHitList.containsKey(model)) {
			_modelHitList.put(model, new IntArray());
		}
		_modelHitList.get(model).add(meta.typeId);
		meta.loaded = true;
		meta.last_usage = TimeUtils.millis();
	}

	private void unload(ModelMeta meta)
	{
		Model model = _assets.get(meta.res, Model.class);

		IntArray hitArray = _modelHitList.get(model);
		hitArray.removeValue(meta.typeId);
		if (hitArray.size == 0) {
			_log.debug("ModelManager", "UnLoad = " + meta.res);
			_assets.unload(meta.res);
			_modelHitList.remove(model);
		}
		meta.loaded = false;
		meta.last_usage = 0;
	}

	public void loadModelList()
	{
		Json json = new Json();

		@SuppressWarnings("unchecked")
		ArrayList<JsonValue> list = json.fromJson(ArrayList.class,
				Gdx.files.internal("assets/objects.json"));

		for (JsonValue v : list)
		{
			ModelMeta m = json.readValue(ModelMeta.class, v);
			m.last_usage = TimeUtils.millis();
			_modelList.put(m.typeId, m);
		}
	}

	public static class ModelMeta
	{
		public boolean loaded = false;
		public int typeId;
		public String res;
		public long last_usage = 0;
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
