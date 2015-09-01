package com.a2client.screens;

import com.a2client.*;
import com.a2client.gui.*;
import com.a2client.model.GameObject;
import com.a2client.network.game.clientpackets.ChatMessage;
import com.a2client.network.game.clientpackets.MouseClick;
import com.a2client.render.GameCamera;
import com.a2client.render.Render1;
import com.a2client.util.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
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

	private Vector2 _world_mouse_pos = new Vector2();
	private boolean[] mouse_btns = new boolean[3];

	private Render1 _render;
	private GameCamera _gameCamera;

	public Game()
	{
		Player.init();
		ObjectCache.getInstance().clear();
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

		_gameCamera = new GameCamera();
		_render = new Render1(this);
	}

	@Override
	public void dispose()
	{
		Player.getInstance().dispose();
		ObjectCache.getInstance().clear();
		InventoryCache.getInstance().clear();
		MapCache.clear();
		_instance = null;
		super.dispose();
	}

	@Override
	public void onUpdate()
	{
		_world_mouse_pos = screen2world(Gdx.input.getX(), Gdx.input.getY());

		if (GUI.getInstance().focused_control == _chatEdit)
		{
			String h;
			if (Input.KeyHit(Keys.UP))
			{
				h = ChatHistory.prev();
				if (!h.isEmpty())
				{
					_chatEdit.SetText(h);
					_chatEdit.SetCursor(_chatEdit.text.length());
				}
			}
			if (Input.KeyHit(Keys.DOWN))
			{
				h = ChatHistory.next();
				_chatEdit.SetText(h);
				_chatEdit.SetCursor(_chatEdit.text.length());
			}
		}

		if (_state == GameState.IN_GAME)
		{
			if (Input.KeyHit(Hotkey.INVENTORY))
			{
				// по нажатию на таб - откроем инвентарь
				InventoryCache.getInstance().toggleInventory(Player.getInstance().getObjectId());
			}
			_statusText = "mouse coord: " + Math.round(_world_mouse_pos.x * MapCache.TILE_SIZE) + ", " +
					Math.round(_world_mouse_pos.y * MapCache.TILE_SIZE);
		}
		_lblStatus.caption =
				"FPS: " + Gdx.graphics.getFramesPerSecond() +
						" " + _statusText +
						" chunks: " + _render.getChunksRendered() +
						" selected: " + (_render.getSelected() != null ? "" + _render.getSelected() : "null") +
						" objects: " + _render.getRenderedObjects();

		if (ObjectCache.getInstance() != null)
		{
			for (GameObject o : ObjectCache.getInstance().getObjects())
			{
				o.Update();
			}
		}

		_gameCamera.update();
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
			mouse_btns[i] = Input.MouseBtns[i];
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
							(_render.getSelected() != null ? _render.getSelected().getObjectId() : 0)
					).Send();
				}
			}
		}
	}

	@Override
	public void onRender3D()
	{
		_render.render(_gameCamera.getGdxCamera());
	}

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		_gameCamera.onResize(width, height);
	}

	public Vector2 screen2world(int x, int y)
	{
		return _gameCamera.screen2world(x, y);
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

	public GameCamera getCamera()
	{
		return _gameCamera;
	}
}
