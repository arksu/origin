package com.a2client.render;

import com.a2client.MapCache;
import com.a2client.ObjectCache;
import com.a2client.gui.GUI;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 25.02.15.
 */
public class GameCamera
{
    private static final Logger _log = LoggerFactory.getLogger(GameCamera.class.getName());

    static final float MOVE_STEP = 0.2f;

    public Camera _camera;

    public Vector2 _camera_offset = new Vector2(0, 0);
    public Vector2 _cameraPos = new Vector2(0, 0);
    public float _cameraDistance = 20;
    private final Plane xzPlane = new Plane(new Vector3(0, 1, 0), 0);

    public void update() {
        //_camera_offset = new Vector2();
        if (GUI.getInstance().focused_control == null)
        {
            if (com.a2client.Input.KeyDown(Input.Keys.W))
            {
                _camera_offset.y -= MOVE_STEP;
            }
            if (com.a2client.Input.KeyDown(Input.Keys.S))
            {
                _camera_offset.y += MOVE_STEP;
            }
            if (com.a2client.Input.KeyDown(Input.Keys.A))
            {
                _camera_offset.x += MOVE_STEP;
            }
            if (com.a2client.Input.KeyDown(Input.Keys.D))
            {
                _camera_offset.x -= MOVE_STEP;
            }
        }
        if (com.a2client.Input.isWheelUpdated())
        {
            _cameraDistance += (_cameraDistance / 15f) * com.a2client.Input.MouseWheel;
            com.a2client.Input.MouseWheel = 0;
        }
        if (ObjectCache.getInstance().getMe() != null)
        {
            Vector2 pp = new Vector2(ObjectCache.getInstance().getMe().getCoord());
            pp = pp.scl(1f / MapCache.TILE_SIZE);
            _cameraPos = pp;
        }
        _cameraPos.add(_camera_offset);
        _camera.position.set(new Vector3(_cameraPos.x + _cameraDistance, _cameraDistance * 1.9f,
                                         _cameraPos.y + _cameraDistance));
        _camera.lookAt(new Vector3(_cameraPos.x, 0, _cameraPos.y));
        _camera.update();

    }

    public void onResize(int width, int height){
        float camWidth = width / 48f;
        float camHeight = camWidth * ((float) height / (float) width);


        _camera = new PerspectiveCamera(30, camWidth, camHeight);
        _camera.near = 1f;
        _camera.far = 1000f;
        _camera.update();
    }

    public Vector2 screen2world(int x, int y)
    {
        Vector3 intersection = new Vector3();
        Ray ray = _camera.getPickRay(x, y);
        Intersector.intersectRayPlane(ray, xzPlane, intersection);
        return new Vector2(intersection.x, intersection.z);
    }
}
