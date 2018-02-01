package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.idfactory.IdFactory;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.inventory.InventoryItem;
import com.a4server.gameserver.model.knownlist.PcKnownList;
import com.a4server.gameserver.model.objects.CollisionTemplate;
import com.a4server.gameserver.model.objects.InventoryTemplate;
import com.a4server.gameserver.model.objects.ItemTemplate;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.*;
import com.a4server.util.Rnd;
import com.a4server.util.network.BaseSendPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * игрок и все что с ним связано
 * Created by arksu on 04.01.2015.
 */
public class Player extends Human
{
	private static final Logger _log = LoggerFactory.getLogger(Player.class);

	private static final String LOAD_CHARACTER = "SELECT account, charName, x, y, lvl, accessLevel, face, hairColor, hairStyle, sex, lastAccess, onlineTime, title, createDate FROM characters WHERE del=0 AND charId = ?";
	private static final String UPDATE_LAST_CHAR = "UPDATE accounts SET lastChar = ? WHERE login = ?";
	public static final String UPDATE_CHARACTER_POS = "UPDATE characters SET x=?, y=?, onlineTime=? WHERE charId=?";

	private GameClient _client = null;

	/**
	 * представление игрока (цвет волос, пол и тд)
	 */
	private final PcAppearance _appearance;

	/**
	 * аккаунт под которым вошел игрок
	 */
	private String _account;

	/**
	 * уровень доступа. 0 - обычный игрок
	 */
	private int _accessLevel;

	/**
	 * время проведенное в игре
	 */
	private long _onlineTime;

	/**
	 * игрок онлайн?
	 */
	private volatile boolean _isOnline = false;

	private volatile boolean _isLoaded = false;

	/**
	 * вещь которую держим в руках. null если ничего нет
	 */
	private Hand _hand;

	private final Cursor _cursor;

	public Player(int objectId, ResultSet rset, PlayerTemplate template)
	{
		super(objectId, template);
		template.setPlayer(this);

		_appearance = new PcAppearance(rset, objectId);
		try
		{
			_accessLevel = rset.getInt("accessLevel");
			_account = rset.getString("account");
			_name = rset.getString("charName");
			_title = rset.getString("title");
			_onlineTime = rset.getLong("onlinetime");

			//                    player._lastAccess = rset.getLong("lastAccess");
			//                    player.getCreateDate().setTime(rset.getDate("createDate"));
			setPos(new ObjectPosition(rset.getInt("x"),
			                          rset.getInt("y"),
			                          0,
			                          rset.getInt("lvl"), this));
		}
		catch (SQLException e)
		{
			_log.warn("failed parse db row for player id=" + objectId);
			e.printStackTrace();
		}

		setVisibleDistance(900);

		_inventory = new Inventory(this, getInventoryWidth(), getInventoryHeight());

		_cursor = new Cursor(this);

		_isLoaded = true;
	}

	private static class PlayerTemplate implements ObjectTemplate
	{
		private Player _player;

		public void setPlayer(Player player)
		{
			_player = player;
		}

		@Override
		public int getTypeId()
		{
			return 1;
		}

		@Override
		public int getWidth()
		{
			return 10;
		}

		@Override
		public int getHeight()
		{
			return 10;
		}

		@Override
		public String getName()
		{
			return "player";
		}

		@Override
		public Class<? extends GameObject> getClazz()
		{
			return null;
		}

		@Override
		public CollisionTemplate getCollision()
		{
			return null;
		}

		@Override
		public InventoryTemplate getInventory()
		{
			return new InventoryTemplate(_player.getInventoryWidth(), _player.getInventoryHeight());
		}

		@Override
		public ItemTemplate getItem()
		{
			return null;
		}

		@Override
		public boolean isLiftable()
		{
			return false;
		}
	}

	@Override
	protected void initKnownList()
	{
		_knownKist = new PcKnownList(this);
	}

	static public Player load(int objectId)
	{
		Player player = null;

		try (Connection con = Database.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement(LOAD_CHARACTER))
		{
			statement.setInt(1, objectId);
			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					// загрузим игрока из строки базы
					player = new Player(objectId, rset, new PlayerTemplate());
				}
			}

			if (player == null)
			{
				return null;
			}

			// восстанавливаем инвентарь и прочее

		}
		catch (Exception e)
		{
			_log.error("Failed loading character", e);
		}

		return player;
	}

	@Override
	protected void onEnterGrid(Grid grid)
	{
		if (getClient() != null)
		{
			getClient().sendPacket(new MapGrid(grid, getPos().getX(), getPos().getY()));
		}

		// надо "активировать" новый грид куда я вошел
		// считаем количество попыток
		int c = 0;
		do
		{
			try
			{
				if (grid.activate(this))
				{
					break;
				}
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			// если ну никак не получается активировать
			if (c++ > 20)
			{
				throw new RuntimeException("activate grid too much tries " + this._name);
			}
		}
		while (true);
	}

	@Override
	protected void onLeaveGrid(Grid grid)
	{
		grid.deactivate(this);
	}

	@Override
	public void delete()
	{
		super.delete();

		// деактивировать занятые гриды
		for (Grid g : _grids)
		{
			g.deactivate(this);
		}
	}

	/**
	 * создает пакет при добавлении игрока в мир, его основные параметры
	 * пакет остылается всем окружающим, поэтому тут должно быть все что связано с отображением игрока в мире
	 * @return пакет
	 */
	@Override
	public GameServerPacket makeAddToWorldPacket()
	{
		GameServerPacket pkt = super.makeAddToWorldPacket();

		pkt
				// раз это персонаж, отправим его представление, то как он должен выглядеть
				.addNext(new PlayerAppearance(_appearance))
				.addNext(new EquipUpdate(_objectId, getEquip()));

		if (_hand != null)
		{
			pkt.addNext(new PlayerHand(_hand));
		}

		return pkt;
	}

	public BaseSendPacket makeInitClientPacket()
	{
		return new InventoryUpdate(_inventory)
				.addNext(new ActionsList(getActions()));
	}

	/**
	 * корректно все освободить и удалить из мира
	 */
	public void deleteMe()
	{
		_isDeleting = true;

		delete();

		// удалим игрока из мира
		if (!World.getInstance().removePlayer(getObjectId()))
		{
			_log.warn("deleteMe: World remove player fail");
		}

		if (_isOnline)
		{
			_isOnline = false;
			// TODO также тут надо сохранить состояние перса в базу.
			getPos().store();
			if (_moveController != null)
			{
				_moveController.setActiveObject(null);
				_moveController = null;
			}
			GameTimeController.getInstance().removeMovingObject(this);
		}
	}

	public boolean isOnline()
	{
		return _isOnline;
	}

	/**
	 * выкинуть из игры
	 */
	public void kick()
	{
		if (getClient() != null)
		{
			// выкинуть из игры
			getClient().closeNow();
		}
		else
		{
			deleteMe();
		}
	}

	public void setClient(GameClient client)
	{
		if (_client != null && client != null)
		{
			_log.warn("Player.client not null");
		}
		_client = client;
	}

	public GameClient getClient()
	{
		return _client;
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	@Override
	public Player getActingPlayer()
	{
		return this;
	}

	public Inventory getInventory()
	{
		return _inventory;
	}

	public void setOnlineStatus(boolean status)
	{
		_isOnline = status;
	}

	public long getOnlineTime()
	{
		return _onlineTime;
	}

	/**
	 * активировать окружающие гриды
	 */
	public void activateGrids() throws InterruptedException
	{
		for (Grid g : _grids)
		{
			if (!g.activate(this))
			{
				throw new RuntimeException("fail to activate grids by " + this.toString());
			}
		}
	}

	/**
	 * обновим в базе последнего использованого перса
	 */
	public void updateLastChar()
	{
		try
		{
			try (Connection con = Database.getInstance().getConnection();
			     PreparedStatement ps = con.prepareStatement(UPDATE_LAST_CHAR))
			{
				ps.setInt(1, getObjectId());
				ps.setString(2, _account);
				ps.execute();
			}
		}
		catch (SQLException e)
		{
			_log.warn("failed: updateLastChar " + e.getMessage());
		}
	}

	/**
	 * получить текущую скорость игрока
	 * @return скорость в единицах координат в секунду (в тайле TILE_SIZE единиц)
	 */
	@Override
	public double getMoveSpeed()
	{
		// todo getMoveSpeed
		return 79f;
	}

	// нагенерить объектов в гриде
	public void randomGrid()
	{
		int ido;
		int gx = getPos().getGridX();
		int gy = getPos().getGridY();
		int grid;
		for (int i = 0; i < 2000; i++)
		{
			int rx = Rnd.get(0, Grid.GRID_FULL_SIZE) + (gx * 1200);
			int ry = Rnd.get(0, Grid.GRID_FULL_SIZE) + (gy * 1200);
			grid = rx / Grid.GRID_FULL_SIZE + ry / Grid.GRID_FULL_SIZE * Grid.SUPERGRID_SIZE;
			ido = IdFactory.getInstance().getNextId();
			_log.debug("create obj " + ido + " x=" + rx + " y=" + ry);

			String q = "INSERT INTO sg_0_obj (id, grid, x, y, type, hp, create_tick) VALUES (?,?,?,?,?,?,?);";
			try
			{
				try (Connection con = Database.getInstance().getConnection();
				     PreparedStatement ps = con.prepareStatement(q))
				{
					ps.setInt(1, ido);
					ps.setInt(2, grid);
					ps.setInt(3, rx);
					ps.setInt(4, ry);
					ps.setInt(5, 11);
					ps.setInt(6, 100);
					ps.setInt(7, GameTimeController.getInstance().getTickCount());
					ps.execute();
				}
			}
			catch (SQLException e)
			{
				_log.warn("failed: storeInDb " + e.getMessage());
			}
		}
	}

	@Override
	protected void setInteractive(boolean value)
	{
		// у игрока тут ничего не делаем
		// хотя может потом будем показывать ему вытянутые руки. мол тянет их к объекту который открыл
	}

	/**
	 * получить размеры инвентаря
	 * @return ширина
	 */
	public int getInventoryWidth()
	{
		// todo: если не загружен папердолл - кинем исключение
		return 6;
	}

	/**
	 * получить размеры инвентаря
	 * @return высота
	 */
	public int getInventoryHeight()
	{
		return 4;
	}

	public Cursor getCursor()
	{
		return _cursor;
	}

	/**
	 * сменить текущий курсор у игрока. имзенится только если реально отличается от текущего
	 * пошлет пакет на клиент
	 */
	public void setCursor(Cursor.CursorName value, int typeId)
	{
		_cursor.set(value, typeId);
	}

	public void setCursor(Cursor.CursorName value)
	{
		setCursor(value, 0);
	}

	public Hand getHand()
	{
		return _hand;
	}

	public int getAccessLevel()
	{
		return _accessLevel;
	}

	/**
	 * non blocking, check object is blocked
	 */
	public boolean setHand(Hand hand)
	{
		_hand = hand;
		if (hand != null)
		{
			_log.debug("set hand: " + hand);
			// записать в базу
			hand.getItem().setXY(200, 200);
		}
		if (_isLoaded)
		{
			getClient().sendPacket(new PlayerHand(_hand));
		}
		return true;
	}

	public List<ActionItem> getActions()
	{
		// todo: actions
		List<ActionItem> list = new ArrayList<>();
		list.add(new ActionItem("lift_up"));
		list.add(new ActionItem("craft"));
		list.add(new ActionItem("build"));

		// debug features
		if (_accessLevel > 0)
		{
			list.add(new ActionItem("online"));

			ActionItem spawn = new ActionItem("spawn");
			list.add(spawn);
			spawn.add(new ActionItem("spawn_pine_tree"));
			spawn.add(new ActionItem("spawn_apple_tree"));
			spawn.add(new ActionItem("spawn_chest"));
			spawn.add(new ActionItem("spawn_stone"));

			list.add(new ActionItem("tile_up"));
			list.add(new ActionItem("tile_down"));
			list.add(new ActionItem("tile_sand"));
			list.add(new ActionItem("tile_grass"));
			list.add(new ActionItem("tile_leaf"));
			list.add(new ActionItem("tile_fir"));
		}

		return list;
	}

	@Override
	public void actionClick(Player player)
	{
		// nothing to do
	}

	/**
	 * попытаться создать вещь в инвентаре игрока
	 * @param typeId ид типа вещи
	 * @param quality качество создаваемой вещи
	 * @param canDrop можно ли бросить на землю если места в инвентаре не оказалось
	 */
	public boolean generateItem(int typeId, int quality, boolean canDrop)
	{
		InventoryItem item = new InventoryItem(this, typeId, quality);
		// пробуем закинуть вещь в инвентарь
		InventoryItem puttedItem = getInventory().putItem(item);
		// только если реально влезло в инвентарь
		if (puttedItem != null)
		{
			// пошлем инвентарь всем с кем взаимодействуем
			sendInteractPacket(new InventoryUpdate(getInventory()));
			// сохраним вещь в бд
			puttedItem.store();
			return true;
		}
		else
		{
			if (canDrop && dropItem(item))
			{
				return true;
			}
		}
		return false;
	}

	public boolean dropItem(AbstractItem item)
	{
		if (item == null) throw new RuntimeException("drop NULL item");

		// создаем новый игровой объект на основании шаблона взятой вещи
		GameObject object = new GameObject(item.getObjectId(), item.getTemplate().getObjectTemplate());
		// зададим этому объекту позицию - прямо под игроком
		object.setPos(new ObjectPosition(getPos(), object));

		// пытаемся заспавнить этот объект
		if (object.getPos().trySpawn())
		{
			_log.debug("item dropped: " + item);
			// сначала грохнем вещь! и только потом сохраним объект в базу
			if (item.markDeleted() && object.store())
			{
				return true;
			}
			else
			{
				// но если чето сцуко пошло не так - уроним все к хуям
				throw new RuntimeException("failed update db on item drop");
			}
		}
		else
		{
			_log.debug("cant drop: " + item);
		}
		return false;
	}

	@Override
	public void addLift(GameObject o, int index)
	{
		// если что-то уже держим над собой не разрешаем поднимать еще
		if (index != 0) return;

		super.addLift(o, index);
	}
}
