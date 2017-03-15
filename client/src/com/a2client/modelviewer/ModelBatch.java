package com.a2client.modelviewer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * свеой велосипедик для батчинга моделей
 * один единый шейдер для всех моделей со статическим батчингом (кастомные условия для моделей передаем через униформ)
 * see {@link com.badlogic.gdx.graphics.g3d.ModelBatch}
 * Created by arksu on 15.03.17.
 */
public class ModelBatch
{
	private final Array<Model> _renderables = new Array<>();

	private ShaderProgram _shader;

	protected final ModelSorter _sorter;

	protected Camera _camera;

	protected Material _currentMaterial;

	protected Mesh _currentMesh;

	public ModelBatch()
	{
		_sorter = new DefaultModelSorter();
	}

	public ShaderProgram getShader()
	{
		return _shader;
	}

	public void setShader(ShaderProgram shader)
	{
		_shader = shader;
	}

	public Material getCurrentMaterial()
	{
		return _currentMaterial;
	}

	public void setCurrentMaterial(Material currentMaterial)
	{
		_currentMaterial = currentMaterial;
	}

	public Mesh getCurrentMesh()
	{
		return _currentMesh;
	}

	public void setCurrentMesh(Mesh currentMesh)
	{
		_currentMesh = currentMesh;
	}

	public void begin(final Camera cam, ShaderProgram shader)
	{
		if (_camera != null) throw new GdxRuntimeException("Call end() first.");
		_camera = cam;
		_shader = shader;
		_shader.begin();
	}

	public void render(Model model)
	{
		_renderables.add(model);
	}

	public void end()
	{
		flush();
		_camera = null;
		_shader.end();
	}

	private void flush()
	{
		_sorter.sort(_camera, _renderables);
		_currentMaterial = null;
		_currentMesh = null;

		for (int i = 0; i < _renderables.size; i++)
		{
			final Model model = _renderables.get(i);
			model.render(this);
		}

		if (_currentMesh != null)
		{
			_currentMesh.unbind(_shader);
		}

		_currentMaterial = null;
		_currentMesh = null;
		_renderables.clear();
	}

	public void bindMesh(Mesh mesh)
	{
		if (_currentMesh != mesh)
		{
			mesh.bind(_shader);
			_currentMesh = mesh;
		}
	}

	public void bindMaterial(Material material)
	{
		if (_currentMaterial != material)
		{
			// TODO if null?
			if (material != null)
			{
				material.bind(_shader);
			}
			_currentMaterial = material;
		}
	}
}
