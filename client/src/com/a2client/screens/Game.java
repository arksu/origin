package com.a2client.screens;

import com.a2client.*;
import com.a2client.gui.*;
import com.a2client.model.GameObject;
import com.a2client.model.Grid;
import com.a2client.network.game.clientpackets.ChatMessage;
import com.a2client.network.game.clientpackets.MouseClick;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    public GUI_Memo _chatMemo;
    private GUI_Edit _chatEdit;

    private static Game _instance;
    private GameState _state = GameState.ENTERING;
    private Camera _camera;

    private Vector2 _camera_offset = new Vector2(0, 0);
    private float _cameraDistance = 20;

    private ShapeRenderer _renderer = new ShapeRenderer();
    private ShaderProgram _shader;
    private Vector2 _world_mouse_pos = new Vector2();
    private int _chunksRendered = 0;
    private boolean[] mouse_btns = new boolean[3];

    static final float MOVE_STEP = 0.2f;

    public Game()
    {
        _shader = new ShaderProgram(
                "#version 120\n" +
                        "attribute vec4 a_position;\n" +
                        "attribute vec3 a_normal;\n" +
                        "attribute vec2 a_texCoord;\n" +
                        "attribute vec4 a_color;\n" +
                        "\n" +
                        "uniform mat4 u_MVPMatrix;\n" +
                        "\n" +
                        "varying float intensity;\n" +
                        "varying vec2 texCoords;\n" +
                        "varying vec4 v_color;\n" +
                        "\n" +
                        "void main() {\n" +
                        "\n" +
                        "    texCoords = a_texCoord;\n" +
                        "\n" +
                        "    gl_Position = u_MVPMatrix * a_position;\n" +
                        "}",

                "#version 120\n" +
                        "\n" +
                        "uniform sampler2D u_texture;\n" +
                        "\n" +
                        "varying vec2 texCoords;\n" +
                        "\n" +
                        "\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D(u_texture, texCoords);\n" +
                        "}"
        );
        if (!_shader.isCompiled())
        {
            Gdx.app.log("Shader", _shader.getLog());
            Gdx.app.log("Shader V", _shader.getVertexShaderSource());
            Gdx.app.log("Shader F", _shader.getFragmentShaderSource());
        }

        Player.init();
        ObjectCache.init();
        MapCache.clear();

        GUI.reCreate();
        _lblStatus = new GUI_Label(GUI.rootNormal());
        _lblStatus.SetPos(10, 10);

        _btnExit = new GUI_Button(GUI.rootNormal())
        {
            @Override
            public void DoClick()
            {
                Main.ReleaseAll();
                Login.setStatus("disconnected");
            }
        };
        _btnExit.caption = Lang.getTranslate("Game.cancel");
        _btnExit.SetSize(100, 25);
        _btnExit.SetPos(Gdx.graphics.getWidth() - 110, Gdx.graphics.getHeight() - 35);

        int hc = 100;
        int wc = 200;
        int py = Config.getScreenHeight() - hc - 30;
        _chatMemo = new GUI_Memo(GUI.rootNormal());
        _chatMemo.SetPos(5, py);
        _chatMemo.SetSize(wc, hc);

        _chatEdit = new GUI_Edit(GUI.rootNormal())
        {
            @Override
            public void DoEnter()
            {
                if (_chatEdit.text.isEmpty())
                {
                    return;
                }
                new ChatMessage(0, _chatEdit.text).Send();
                ChatHistory.add(_chatEdit.text);
                _chatEdit.SetText("");
            }
        };
        _chatEdit.SetPos(5, py + _chatMemo.Height() + 5);
        _chatEdit.SetSize(_chatMemo.Width(), 20);

    }

    @Override
    public void dispose()
    {
        Player.getInstance().dispose();
        ObjectCache.getInstance().dispose();
        MapCache.clear();
        _instance = null;
        super.dispose();
    }

    @Override
    public void onUpdate()
    {

        //_camera_offset.x += -0.1f * Gdx.graphics.getDeltaTime();


        _world_mouse_pos = screen2world(Gdx.input.getX(), Gdx.input.getY()).sub(getOffset())
                                                                           .sub(_camera_offset);

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
        else if (GUI.getInstance().focused_control == _chatEdit)
        {
            String h;
            if (com.a2client.Input.KeyHit(Input.Keys.UP))
            {
                h = ChatHistory.prev();
                if (!h.isEmpty())
                {
                    _chatEdit.SetText(h);
                    _chatEdit.SetCursor(999);
                }
            }
            if (com.a2client.Input.KeyHit(Input.Keys.DOWN))
            {
                h = ChatHistory.next();
                _chatEdit.SetText(h);
                _chatEdit.SetCursor(999);
            }
        }

        if (com.a2client.Input.isWheelUpdated())
        {
//            _camera.zoom += com.a2client.Input.MouseWheel / 10f;
            _cameraDistance += com.a2client.Input.MouseWheel / 10f;
            com.a2client.Input.MouseWheel = 0;
        }

        if (_state == GameState.IN_GAME)
        {
            _statusText = "mouse coord: " + Math.round(_world_mouse_pos.x * MapCache.TILE_SIZE) + ", " +
                    Math.round(_world_mouse_pos.y * MapCache.TILE_SIZE);
        }
        _lblStatus.caption =
                "FPS: " + Gdx.graphics.getFramesPerSecond() +
                        " " + _statusText +
                        " chunks: " + _chunksRendered;


        if (ObjectCache.getInstance() != null)
        {
            for (GameObject o : ObjectCache.getInstance().getObjects())
            {
                o.Update();
            }
        }

        if (ObjectCache.getInstance().getMe() != null)
        {
//            Vec2i pp = ObjectCache.getInstance().getMe().getCoord().div(MapCache.TILE_SIZE);
//            pp = pp.sub(pp.mul(2));
            //            _camera_offset = pp.getVector2();
        }
        _camera.position.set(new Vector3(_camera_offset.x+_cameraDistance, _cameraDistance, _camera_offset.y+_cameraDistance));
//        _camera.position.set(50, 50, 50);
        _camera.lookAt(new Vector3(_camera_offset.x, 0, _camera_offset.y));
        _camera.update();

        UpdateMouseButtons();
    }

    protected void UpdateMouseButtons()
    {
        boolean[] old_btns = new boolean[3];
        old_btns[0] = mouse_btns[0];
        old_btns[1] = mouse_btns[1];
        old_btns[2] = mouse_btns[2];
        for (int i = 0; i < 3; i++)
        {
            mouse_btns[i] = com.a2client.Input.MouseBtns[i];
            // узнаем на какую кнопку нажали
            if (mouse_btns[i] != old_btns[i])
            {
                if ((mouse_btns[i] && GUI.getInstance().mouse_in_control == null) || (!mouse_btns[i]))
                {
                    new MouseClick(
                            mouse_btns[i],
                            i,
                            Math.round(_world_mouse_pos.x * MapCache.TILE_SIZE),
                            Math.round(_world_mouse_pos.y * MapCache.TILE_SIZE),
                            0
                    ).Send();
                }
            }
        }
    }

    @Override
    public void onRender3D()
    {
        // оффсет
        Vector2 offset = getOffset();

//        offset.add(_camera_offset);

        // координаты тайла который рендерим
//        Vector2 tc = new Vector2();

        _shader.begin();
        _shader.setUniformMatrix("u_MVPMatrix", _camera.combined);
        _shader.setUniformi("u_texture", 0);


        Main.getAssetManager().get(Config.RESOURCE_DIR + "tiles_atlas.png", Texture.class).bind();
        _chunksRendered = 0;
        for (Grid grid : MapCache.grids)
        {
            _chunksRendered += grid.render(_shader, _camera);
        }
        _shader.end();

        _renderer.setProjectionMatrix(_camera.combined);
        _renderer.begin(ShapeRenderer.ShapeType.Filled);

        if (ObjectCache.getInstance() != null)
        {
            for (GameObject o : ObjectCache.getInstance().getObjects())
            {
                renderObject(o);
            }
        }

        _renderer.end();
    }

    private void renderObject(GameObject object)
    {
        Vector2 oc = new Vector2(object.getCoord().x, object.getCoord().y);
        oc.x = oc.x / MapCache.TILE_SIZE;
        oc.y = oc.y / MapCache.TILE_SIZE;
        oc = oc.add(getOffset()).add(_camera_offset);

        _renderer.setColor(Color.RED);
        float sz = 1f;
        _renderer.box(oc.x - sz, 0, oc.y - sz, sz, sz, sz);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);

        float camWidth = width / 48f;

        float camHeight = camWidth * ((float) height / (float) width);


        _camera = new PerspectiveCamera(30, camWidth, camHeight);//new IsometricCamera(camWidth, camHeight);
        _camera.near = 0.01f;
        _camera.far = 1000f;
        _camera.update();
//        _camera.zoom = 0.6f;
    }

    public Vector2 screen2world(int x, int y)
    {
        Vector3 touch = new Vector3(x, y, 0);
        _camera.unproject(touch);
        // touch.mul(_invTransform);
        return new Vector2(touch.x, touch.y);
    }

    public Vector2 getOffset()
    {
        Vector2 offset = Vector2.Zero;
        if (ObjectCache.getInstance().getMe() != null)
        {
            Vector2 op = ObjectCache.getInstance().getMe().getCoord();
            Vector2 pp = new Vector2(op.x, op.y);
            pp.x = pp.x / MapCache.TILE_SIZE;
            pp.y = pp.y / MapCache.TILE_SIZE;
            pp = pp.sub(pp.x * 2, pp.y * 2);
            offset = pp;
        }
        return offset;
    }

    public void setState(GameState state)
    {
        _state = state;
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
