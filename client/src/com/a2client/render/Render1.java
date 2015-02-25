package com.a2client.render;

import com.a2client.MapCache;
import com.a2client.ObjectCache;
import com.a2client.Terrain;
import com.a2client.model.GameObject;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * примитивный рендер, пока один. может еще добавим других
 * Created by arksu on 25.02.15.
 */
public class Render1
{
    private static final Logger _log = LoggerFactory.getLogger(Render1.class.getName());

    private Game _game;

    //
    private Model _model;
    private ModelInstance _modelInstance;
    private ModelBatch _modelBatch;

    //
    Terrain _terrain;

    //
    private Environment _environment;

    private GameObject _selected;

    private float _selectedDist;
    private int _renderedObjects;


    public Render1(Game game)
    {
        ShaderProgram.pedantic = false;
        _game = game;

        _environment = new Environment();
        _environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        _environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        _modelBatch = new ModelBatch();
        ModelLoader loader = new ObjLoader();
        _model = loader.loadModel(Gdx.files.internal("assets/ship.obj"));
        _modelInstance = new ModelInstance(_model);
        _terrain = new Terrain();
    }

    public void render(Camera camera)
    {
        //
        _terrain.Render(camera, _environment);

        //
        _renderedObjects = 0;
        if (ObjectCache.getInstance() != null)
        {
            _selected = null;
            _selectedDist = 100500;
            Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
            _modelBatch.begin(camera);
            for (GameObject o : ObjectCache.getInstance().getObjects())
            {
                BoundingBox boundingBox = new BoundingBox(o.getBoundingBox());
                Vector2 oc = new Vector2(o.getCoord()).scl(1f / MapCache.TILE_SIZE);
                Vector3 add = new Vector3(oc.x, 0, oc.y);
                boundingBox.min.add(add);
                boundingBox.max.add(add);
                if (camera.frustum.boundsInFrustum(boundingBox))
                {
                    _renderedObjects++;
                    _modelInstance.transform.setToTranslation(oc.x, 0.5f, oc.y);
                    _modelBatch.render(_modelInstance, _environment);
                    Vector3 intersection = new Vector3();
                    if (Intersector.intersectRayBounds(ray, boundingBox, intersection))
                    {
                        float dist = intersection.dst(camera.position);
                        if (dist < _selectedDist)
                        {
                            _selected = o;
                            _selectedDist = dist;
                        }
                    }
                }
            }
            _modelBatch.end();
        }
    }

    public int getChunksRendered()
    {
        return _terrain.getChunksRendered();
    }

    public GameObject getSelected()
    {
        return _selected;
    }

    public int getRenderedObjects()
    {
        return _renderedObjects;
    }
}
