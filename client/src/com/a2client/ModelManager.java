package com.a2client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public class ModelManager {

	private static ModelManager _instance;

	public static ModelManager getInstance() {
		if (_instance == null)
			_instance = new ModelManager();
		
		return _instance;
	}

	private Model _model;
	
	private ObjectMap<Integer,Model> _modelList = new ObjectMap<Integer,Model>();
	
	public ModelManager() {
		ModelLoader loader = new ObjLoader();
		_model = loader.loadModel(Gdx.files.internal("assets/debug/invader.obj"));
	}

	public ModelInstance getByType(int _typeId) {
		return new ModelInstance(_model);
	}
	
	public void loadModelList(){
		Json json = new Json();
//		json.
		
	}
	
	class ModelMeta {
		int typeId;
		String model_res;
	}

}
