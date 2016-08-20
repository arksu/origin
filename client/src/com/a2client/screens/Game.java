package com.a2client.screens;

import com.a2client.*;
import com.a2client.gamegui.GUI_ActionsList;
import com.a2client.gui.*;
import com.a2client.model.GameObject;
import com.a2client.model.Grid;
import com.a2client.model.GridChunk;
import com.a2client.model.Inventory;
import com.a2client.network.game.clientpackets.ActionSelect;
import com.a2client.network.game.clientpackets.ChatMessage;
import com.a2client.network.game.clientpackets.MouseClick;
import com.a2client.render.Fog;
import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.a2client.util.Keys;
import com.a2client.util.Utils;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Terrain.TILE_SIZE;
import static com.a2client.Terrain.getGrid;
import static com.a2client.model.Grid.CHUNK_SIZE;

/**
 * основной игровой экран. тут выводим все объекты и всю информацию по ним
 */
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
	public GUI_ActionsList _actions;

	private static Game _instance;
	private GameState _state = GameState.ENTERING;

	/**
	 * координаты куда указывает мышь. пересечение с ладншафтом.
	 */
	private Vec2i _worldMousePos;
	private boolean[] _mouseBtns = new boolean[3];

	private Render _render;
	private GameCamera _gameCamera;

	/**
	 * мини костыльчик чтобы выключать фокус из чат эдита
	 */
	private boolean _lostFocus = false;

	public Game()
	{
		Player.init();
		ObjectCache.getInstance().clear();
		Terrain.clear();

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
		_btnExit.caption = Lang.getTranslate("Game.quit");
		_btnExit.SetSize(100, 25);
		_btnExit.SetPos(Gdx.graphics.getWidth() - 100, 0);

		int hc = 100;
		int wc = 200;
		int py = Gdx.graphics.getHeight() - hc - 30;
		_chatMemo = new GUI_Memo(GUI.rootNormal());
		_chatMemo.SetPos(5, py);
		_chatMemo.SetSize(wc, hc);

		_chatEdit = new GUI_Edit(GUI.rootNormal())
		{
			@Override
			public void DoEnter()
			{
				if (Utils.isEmpty(_chatEdit.text))
				{
					if (GUI.getInstance().focused_control == _chatEdit)
					{
						GUI.getInstance().focused_control = null;
						_lostFocus = true;
					}
					return;
				}
				new ChatMessage(0, _chatEdit.text).Send();
				ChatHistory.add(_chatEdit.text);
				_chatEdit.SetText("");
			}
		};
		_chatEdit.SetSize(_chatMemo.Width(), 20);
		_chatEdit.SetPos(5, py + _chatMemo.Height() + 5);

		_actions = new GUI_ActionsList(GUI.rootNormal())
		{
			@Override
			public void DoClick()
			{
				String item = getItemTag(GetSelected());
				new ActionSelect(item).Send();
			}
		};
		_actions.SetSize(150, 100);
		_actions.SetPos(Gdx.graphics.getWidth() - _actions.size.x, Gdx.graphics.getHeight() - _actions.size.y);

		_gameCamera = new GameCamera();
		_render = new Render(this);
		_bgcolor = Fog.skyColor;
	}

	@Override
	public void dispose()
	{
		_render.dispose();
		Player.getInstance().dispose();
		ObjectCache.getInstance().clear();
		InventoryCache.getInstance().clear();
		Terrain.clear();
		_instance = null;
		super.dispose();
	}

	@Override
	public void onUpdate()
	{
		_actions.SetPos(Gdx.graphics.getWidth() - _actions.size.x, Gdx.graphics.getHeight() - _actions.size.y);
		if (_state == GameState.IN_GAME)
		{
			if (GUI.getInstance().focused_control == _chatEdit)
			{
				String h = "";
				if (Input.KeyHit(Keys.UP))
				{
					h = ChatHistory.prev();
				}
				if (Input.KeyHit(Keys.DOWN))
				{
					h = ChatHistory.next();
				}
				if (!Utils.isEmpty(h))
				{
					_chatEdit.SetText(h);
					_chatEdit.SetCursor(_chatEdit.text.length());
				}
			}
			else if (GUI.getInstance().mouse_in_control == null)
			{
				if (Input.KeyHit(Hotkey.INVENTORY))
				{
					// по нажатию на таб - откроем инвентарь
					Inventory inventory = InventoryCache.getInstance().get(Player.getInstance().getObjectId());
					if (inventory != null)
					{
						inventory.toggle();
					}
				}
				else if (Input.KeyHit(Hotkey.EQUIP))
				{
					Player.getInstance().getEquip().toggle();
				}
				else if (Input.KeyHit(Hotkey.CHAT_ENTER))
				{
					if (GUI.getInstance().focused_control == null && !_lostFocus)
					{
						GUI.getInstance().focused_control = _chatEdit;
					}
					_lostFocus = false;
				}
				else if (Input.KeyHit(Hotkey.FOG))
				{
					Fog.enabled = !Fog.enabled;
				}
				else if (Input.KeyHit(Hotkey.TERRAIN_WIREFRAME))
				{
					Config.getInstance()._renderTerrainWireframe = !Config.getInstance()._renderTerrainWireframe;
				}
				else if (Input.KeyHit(Hotkey.RENDER_OUTLINE))
				{
					Config.getInstance()._renderOutline = !Config.getInstance()._renderOutline;
				}
				else if (Input.KeyHit(Keys.P))
				{
					Config.getInstance()._renderImproveWater = !Config.getInstance()._renderImproveWater;
				}
				else if (Input.KeyHit(Keys.K))
				{
					Config.getInstance()._renderShadows = !Config.getInstance()._renderShadows;
				}
				else if (Input.KeyHit(Keys.J))
				{
					Config.getInstance()._renderPostProcessing = !Config.getInstance()._renderPostProcessing;
				}

				else if (Input.KeyDown(Keys.W))
				{
					_gameCamera.getOffset().add(0, 0, Main.deltaTime * GameCamera.OFFSET_SPEED);
				}
				else if (Input.KeyDown(Keys.S))
				{
					_gameCamera.getOffset().add(0, 0, -Main.deltaTime * GameCamera.OFFSET_SPEED);
				}
				else if (Input.KeyDown(Keys.A))
				{
					_gameCamera.getOffset().add(Main.deltaTime * GameCamera.OFFSET_SPEED, 0, 0);
				}
				else if (Input.KeyDown(Keys.D))
				{
					_gameCamera.getOffset().add(-Main.deltaTime * GameCamera.OFFSET_SPEED, 0, 0);
				}
				else if (Input.KeyDown(Keys.E))
				{
					_gameCamera.getOffset().add(0, Main.deltaTime * GameCamera.OFFSET_SPEED, 0);
				}
				else if (Input.KeyDown(Keys.Q))
				{
					_gameCamera.getOffset().add(0, -Main.deltaTime * GameCamera.OFFSET_SPEED, 0);
				}
			}

			if (ObjectCache.getInstance() != null)
			{
				for (GameObject o : ObjectCache.getInstance().getObjects())
				{
					o.update();
				}
			}

			// обновить камеру и позицию куда проецируется мышь
			_gameCamera.update();
			Vector3 terrainPoint = _gameCamera.getMousePicker().getCurrentTerrainPoint();
			if (terrainPoint != null)
			{
				_worldMousePos = new Vec2i(Math.round(terrainPoint.x * TILE_SIZE), Math.round(terrainPoint.z * TILE_SIZE));
				_statusText = "mouse coord: " +
							  _worldMousePos.x + ", " + _worldMousePos.y + " : " +
							  Terrain.getTileHeight(_worldMousePos.x / TILE_SIZE, _worldMousePos.y / TILE_SIZE);

				int wx = _worldMousePos.x;
				int wy = _worldMousePos.y;
				int tx = _worldMousePos.x / TILE_SIZE;
				int ty = _worldMousePos.y / TILE_SIZE;
				Grid grid = getGrid(tx, ty);
				if (grid != null)
				{
					tx -= grid.getTc().x;
					ty -= grid.getTc().y;
					tx /= CHUNK_SIZE;
					ty /= CHUNK_SIZE;
					GridChunk chunk = grid.getChunk(tx, ty);
					if (chunk != null)
					{
//						_log.debug(chunk.toString());
					}
				}

			}
			else
			{
				_worldMousePos = null;
				_statusText = "mouse coord: NULL";
			}
			UpdateMouseButtons();
		}
		_lblStatus.caption =
				"FPS: " + Gdx.graphics.getFramesPerSecond() +
				" " + _statusText +
				" chunks: " + _render.getChunksRendered() + " / " + _render.getWaterChunksRendered() +
				" selected: " + (_render.getSelected() != null ? "" + _render.getSelected() : "null") +
				" objects: " + _render.getRenderedObjects() +
				" cam: " + _gameCamera.getCameraDistance()
		;
	}

	protected void UpdateMouseButtons()
	{
		boolean[] old_btns = new boolean[3];
		old_btns[0] = _mouseBtns[0];
		old_btns[1] = _mouseBtns[1];
		old_btns[2] = _mouseBtns[2];
		for (int i = 0; i < 3; i++)
		{
			_mouseBtns[i] = Input.MouseBtns[i];
			boolean click = _mouseBtns[i] != old_btns[i];
			// вращаем камеру
			if (i == Hotkey.BUTTON_CAMERA)
			{
				// если кнопка нажата
				if (_mouseBtns[i])
				{
					// это был клик?
					if (click)
					{
						_gameCamera.startDrag(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
					}
					else
					{
						_gameCamera.updateDrag(new Vec2i(Gdx.input.getX(), Gdx.input.getY()));
					}
				}
				// кнопку отжали
				else if (click)
				{
					// закончим вращение перетаскивание камеры
					_gameCamera.startDrag(null);
				}
			}
			// узнаем на какую кнопку нажали
			if (click)
			{
				if (!_mouseBtns[i] || GUI.getInstance().mouse_in_control == null)
				{
					GUI.getInstance().focused_control = null;
					if (_worldMousePos != null)
					{
						int action = -1;
						if (i == Hotkey.BUTTON_ACTION_PRIMARY) action = 0;
						if (i == Hotkey.BUTTON_ACTION_SECONDARY) action = 1;
						new MouseClick(
								_mouseBtns[i],
								action,
								_worldMousePos.x,
								_worldMousePos.y,
								(_render.getSelected() != null ? _render.getSelected().getObjectId() : 0)
						).Send();
					}
				}
			}
		}
	}

	@Override
	public void onRender3D()
	{
		_render.render(_gameCamera);
	}

	@Override
	public void resize(int width, int height)
	{
		super.resize(width, height);
		_gameCamera.onResize(width, height);
		_render.onResize(width, height);
	}

	public void setState(GameState state)
	{
		_state = state;
	}

	static public Game getInstance()
	{
		if (_instance == null)
		{
			throw new RuntimeException("Game instance is NULL");
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
