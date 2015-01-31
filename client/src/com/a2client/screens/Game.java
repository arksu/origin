package com.a2client.screens;

import com.a2client.*;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Button;
import com.a2client.gui.GUI_Label;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game extends BaseScreen
{
    private static final Logger _log = LoggerFactory.getLogger(Game.class.getName());

    public enum GameState
    {
        ENTERING,
        IN_GAME
    }

    private static String _statusText = "";

    private GUI_Label _lblStatus;
    private GUI_Button _btnExit;

    private static Game _instance;
    private GameState _state = GameState.ENTERING;
    private OrthographicCamera _camera;

    private Vector2 _player_pos = new Vector2(0, 0);

    private ShapeRenderer _renderer = new ShapeRenderer();
    private Vector2 _world_mouse_pos = new Vector2();

    public Game()
    {
        Player.init();

        GUI.reCreate();
        _lblStatus = new GUI_Label(GUI.rootNormal());
        _lblStatus.SetPos(100, 100);

        _btnExit = new GUI_Button(GUI.rootNormal())
        {
            @Override
            public void DoClick()
            {
                Main.ReleaseAll();
                Login.setStatus("disconnected");
            }
        };
        _btnExit.caption = Lang.getTranslate("generic", "cancel");
        _btnExit.SetSize(100, 25);
        _btnExit.SetPos(Gdx.graphics.getWidth() - 110, Gdx.graphics.getHeight() - 35);

    }

    @Override
    public void onUpdate()
    {

        //_player_pos.x += -0.1f * Gdx.graphics.getDeltaTime();


        _world_mouse_pos = screen2world(Gdx.input.getX(), Gdx.input.getY());

        //_player_pos = new Vector2();
        if (com.a2client.Input.KeyDown(Input.Keys.W))
        {
            _player_pos.x += 1;
        }
        if (com.a2client.Input.KeyDown(Input.Keys.S))
        {
            _player_pos.x -= 1;
        }
        if (com.a2client.Input.KeyDown(Input.Keys.A))
        {
            _player_pos.y += 1;
        }
        if (com.a2client.Input.KeyDown(Input.Keys.D))
        {
            _player_pos.y -= 1;
        }

        if (com.a2client.Input.isWheelUpdated())
        {
            _camera.zoom += com.a2client.Input.MouseWheel / 10f;
            com.a2client.Input.MouseWheel = 0;
        }

        if (_state == GameState.IN_GAME)
        {
            _statusText = _world_mouse_pos.toString();
        }
        _lblStatus.caption = _statusText;

        _camera.position.set(_player_pos, 0);
        _camera.update();

    }

    @Override
    public void onRender3D()
    {

        Vec2i px = new Vec2i();

        _renderer.setProjectionMatrix(_camera.combined);
        _renderer.begin(ShapeType.Filled);

        for (Grid grid : MapCache.grids)
        {
            for (int x = 0; x < MapCache.GRID_SIZE; x++)
            {
                for (int y = 0; y < MapCache.GRID_SIZE; y++)
                {
                    px.x = (grid.getGC().x / MapCache.TILE_SIZE) + x;
                    px.y = (grid.getGC().y / MapCache.TILE_SIZE) + y;

                    if (_camera.frustum.pointInFrustum(px.x, px.y, 0))
                    {
                        _renderer.setColor(Grid.getTileColor(grid._tiles[y][x]));
                        _renderer.box(px.x, px.y, 0, 1, 1, 0.3f);
                    }
                }
            }
        }

        _renderer.end();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        float camWidth = width / 48f;

        float camHeight = camWidth * ((float) height / (float) width);

        _camera = new IsometricCamera(camWidth, camHeight);
        _camera.update();
        _camera.zoom = 2.6f;
    }

    public Vector2 screen2world(int x, int y)
    {
        Vector3 touch = new Vector3(x, y, 0);
        _camera.unproject(touch);
        // touch.mul(_invTransform);
        return new Vector2(touch.x, touch.y);
    }

    public void setState(GameState state)
    {
        _state = state;
    }

    @Override
    public void dispose()
    {
        Player.getInstance().dispose();
        _instance = null;
        super.dispose();
    }

    static public Game getInstance()
    {
        if (_instance == null)
        {
            _log.error("Game instance is NULL!");
        }
        return _instance;
    }

    static public void setStatusText(String statustext)
    {
        _statusText = statustext;
    }

    static public void Show()
    {
        _statusText = "";
        Main.freeScreen();
        _instance = new Game();
        Main.getInstance().setScreen(_instance);
    }
}
