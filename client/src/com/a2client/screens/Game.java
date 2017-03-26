package com.a2client.screens;

import com.a2client.*;
import com.a2client.gamegui.GUI_ActionsList;
import com.a2client.gamegui.GUI_GameArea;
import com.a2client.gui.*;
import com.a2client.model.GameObject;
import com.a2client.model.Inventory;
import com.a2client.network.game.clientpackets.ActionSelect;
import com.a2client.network.game.clientpackets.ChatMessage;
import com.a2client.render.GameCamera;
import com.a2client.render.Render;
import com.a2client.render.skybox.Skybox;
import com.a2client.util.Keys;
import com.a2client.util.Utils;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Terrain.TILE_SIZE;

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

	private GUI_GameArea _gameArea;
	private GUI_Label _lblStatus;
	private GUI_Label _lblStatus2;
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
	private Vector3 _terrainPoint;
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
		_gameArea = new GUI_GameArea(GUI.getInstance().custom, this);
		_gameArea.setPos(0, 0);
		_gameArea.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		_lblStatus = new GUI_Label(GUI.rootNormal());
		_lblStatus.setPos(10, 10);

		_lblStatus2 = new GUI_Label(GUI.rootNormal());
		_lblStatus2.setPos(10, 30);

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
		_btnExit.setSize(100, 25);
		_btnExit.setPos(Gdx.graphics.getWidth() - 100, 0);

		int hc = 100;
		int wc = 200;
		int py = Gdx.graphics.getHeight() - hc - 30;
		_chatMemo = new GUI_Memo(GUI.rootNormal());
		_chatMemo.setPos(5, py);
		_chatMemo.setSize(wc, hc);

		_chatEdit = new GUI_Edit(GUI.rootNormal())
		{
			@Override
			public void DoEnter()
			{
				if (Utils.isEmpty(_chatEdit.text))
				{
					if (GUI.getInstance()._focusedControl == _chatEdit)
					{
						GUI.getInstance()._focusedControl = null;
						_lostFocus = true;
					}
					return;
				}
				new ChatMessage(0, _chatEdit.text).send();
				ChatHistory.add(_chatEdit.text);
				_chatEdit.SetText("");
			}
		};
		_chatEdit.setSize(_chatMemo.getWidth(), 20);
		_chatEdit.setPos(5, py + _chatMemo.getHeight() + 5);

		_actions = new GUI_ActionsList(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				String item = getItemTag(getSelectedItem());
				new ActionSelect(item).send();
			}
		};
		_actions.setSize(150, 100);
		_actions.setPos(Gdx.graphics.getWidth() - _actions.size.x, Gdx.graphics.getHeight() - _actions.size.y);

		_gameCamera = new GameCamera();
		_render = new Render(this);
		_bgcolor = Skybox.skyColor;
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
		_actions.setPos(Gdx.graphics.getWidth() - _actions.size.x, Gdx.graphics.getHeight() - _actions.size.y);
		if (_state == GameState.IN_GAME)
		{
			if (GUI.getInstance()._focusedControl == _chatEdit)
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
			else if (GUI.getInstance()._mouseInControl == _gameArea)
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
					Player.getInstance().getEquipWindow().toggle();
				}
				else if (Input.KeyHit(Hotkey.CHAT_ENTER))
				{
					if (GUI.getInstance()._focusedControl == null && !_lostFocus)
					{
						GUI.getInstance()._focusedControl = _chatEdit;
					}
					_lostFocus = false;
				}
				else if (Input.KeyHit(Hotkey.FOG))
				{
					Skybox.fogEnabled = !Skybox.fogEnabled;
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

/*
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
*/
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
			_terrainPoint = _gameCamera.getMousePicker().getCurrentTerrainPoint();
			if (_terrainPoint != null)
			{
				_worldMousePos = new Vec2i(Math.round(_terrainPoint.x * TILE_SIZE), Math.round(_terrainPoint.z * TILE_SIZE));
				_statusText = "mouse coord: " +
				              _worldMousePos.x + ", " + _worldMousePos.y
//							 + " : " + Terrain.getTileHeight(_worldMousePos.x / TILE_SIZE, _worldMousePos.y / TILE_SIZE)
				;
			}
			else
			{
				_worldMousePos = null;
				_statusText = "mouse coord: NULL";
			}
		}
		_lblStatus.caption =
				"FPS: " + Gdx.graphics.getFramesPerSecond() +
				" " + _statusText +
				" chunks: " + _render.getChunksRendered() + " / " + _render.getWaterChunksRendered() +
				" selected: " + (_render.getSelected() != null ? "" + _render.getSelected() : "null") +
				" objects: " + _render.getRenderedObjects() +
//				" cam: " + _gameCamera.getCameraDistance()+
				" gui: " + GUI.getInstance()._mouseInControl
		;

		_lblStatus2.caption =
				" mesh switch=" + _render.getModelBatch().getSwitchMeshCounter() +
				" material switch=" + _render.getModelBatch().getSwitchMaterialCounter();

//		_lblStatus.caption =
//				" sun: " + Skybox.sunTime +
//				" sun y: " + Skybox.sunPosition.y;

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
		_gameArea.setSize(width, height);
		_render.onResize(width, height);
	}

	public void setState(GameState state)
	{
		_state = state;
	}

	public Vec2i getWorldMousePos()
	{
		return _worldMousePos;
	}

	public Vector3 getTerrainPoint()
	{
		return _terrainPoint;
	}

	public Render getRender()
	{
		return _render;
	}

	static public Game getInstance()
	{
		// создаем в Show
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
