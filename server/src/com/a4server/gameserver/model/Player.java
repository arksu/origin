package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.idfactory.IdFactory;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.inventory.InventoryItem;
import com.a4server.gameserver.model.objects.*;
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

/**
 * игрок и все что с ним связано
 * Created by arksu on 04.01.2015.
 */
public class Player extends Human
{
	private static final Logger _log = LoggerFactory.getLogger(Player.class);

	private static final String LOAD_CHARACTER = "SELECT account, charName, x, y, lvl, accessLevel, face, hairColor, hairStyle, sex, lastAccess, onlineTime, title, createDate FROM characters WHERE del=0 AND charId = ?";
	private static final String UPDATE_LAST_CHAR = "UPDATE accounts SET lastChar = ? WHERE login = ?";
	private static final String UPDATE_CHARACTER = "UPDATE characters SET x=?, y=? WHERE charId=?";

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
	 * игрок онлайн?
	 */
	private volatile boolean _isOnline = false;

	private volatile boolean _isLoaded = false;

	/**
	 * вещь которую держим в руках. null если ничего нет
	 */
	private Hand _hand;

	private final Equip _equip;

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

			//                    player._lastAccess = rset.getLong("lastAccess");
			//                    player.setOnlineTime(rset.getLong("onlinetime"));
			//                    player.getCreateDate().setTime(rset.getDate("createDate"));
			_pos = new ObjectPosition(rset.getInt("x"), rset.getInt("y"), rset.getInt("lvl"), this);
		}
		catch (SQLException e)
		{
			_log.warn("failed parse db row for player id=" + objectId);
			e.printStackTrace();
		}

		setVisibleDistance(500);

		// сначала грузим папердоллл! от него может зависеть размер инвентаря
		_equip = new Equip(this);

		_inventory = new Inventory(this, getInventoryWidth(), getInventoryHeight());
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
			getClient().sendPacket(new MapGrid(grid, getPos()._x, getPos()._y));
		}
	}

	/**
	 * добавили объект в грид в котором находится игрок
	 */
	public void onGridObjectAdded(GameObject object)
	{
		// тут проверим видим ли мы этот объект
		if (isObjectVisibleForMe(object))
		{
			addKnownObject(object);
		}
	}

	/**
	 * грид говорит что какой то объект был удален
	 */
	public void onGridObjectRemoved(GameObject object)
	{
		removeKnownObject(object);
	}

	/**
	 * добавить объект в список видимых объектов
	 * @param object объект
	 */
	@Override
	protected void addKnownObject(GameObject object)
	{
		// такого объекта еще не было в списке
		if (!_knownKist.contains(object))
		{
			super.addKnownObject(object);
			getClient().sendPacket(object.makeAddToWorldPacket());
		}
	}

	/**
	 * удалить из списка видимых объектов
	 * @param object объект
	 */
	@Override
	protected void removeKnownObject(GameObject object)
	{
		if (_knownKist.contains(object))
		{
			super.removeKnownObject(object);
			getClient().sendPacket(object.makeRemoveFromWorldPacket());
		}
	}

	/**
	 * создать пакет для отсылки другим игрокам
	 * @return пакет
	 */
	public GameServerPacket makeAddToWorldPacket()
	{
		GameServerPacket pkt = new ObjectAdd(this);
		BaseSendPacket next = pkt
				// раз это персонаж, отправим его представление, то как он должен выглядеть
				.addNext(new PlayerAppearance(_appearance))
				.addNext(new InventoryUpdate(_inventory))
				.addNext(new EquipUpdate(_equip))
				.addNext(new Actions(getActions()));
		if (_hand != null)
		{
			next.addNext(new PlayerHand(_hand));
		}
		return pkt;
	}

	/**
	 * корректно все освободить и удалить из мира
	 */
	public void deleteMe()
	{
		_isDeleteing = true;

		// деактивировать занятые гриды
		for (Grid g : _grids)
		{
			g.deactivate(this);
		}
		Grid grid = getPos().getGrid();
		if (grid != null)
		{
			try
			{
				grid.tryLockSafe(Grid.MAX_WAIT_LOCK);
				grid.removeObject(this);
			}
			catch (InterruptedException e)
			{
				_log.warn("deleteMe: InterruptedException");
				e.printStackTrace();
			}
			finally
			{
				grid.unlock();
			}
		}

		// удалим игрока из мира
		if (!World.getInstance().removePlayer(getObjectId()))
		{
			_log.warn("deleteMe: World remove player fail");
		}

		if (_isOnline)
		{
			_isOnline = false;
			// также тут надо сохранить состояние перса в базу.
			storeInDb();
			if (_moveController != null)
			{
				_moveController.setActiveObject(null);
				_moveController = null;
			}
			GameTimeController.getInstance().RemoveMovingObject(this);
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

	public Inventory getInventory()
	{
		return _inventory;
	}

	public Equip getEquip()
	{
		return _equip;
	}

	public void setOnlineStatus(boolean status)
	{
		_isOnline = status;
	}

	/**
	 * активировать окружающие гриды
	 * @throws InterruptedException
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
	 * сохранить состояние персонажа в базу
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	public void storeInDb()
	{
		// todo player storeInDb
		_log.debug("storeInDb " + toString());
		try
		{
			try (Connection con = Database.getInstance().getConnection();
				 PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER))
			{
				ps.setInt(1, getPos()._x);
				ps.setInt(2, getPos()._y);
				ps.setInt(3, getObjectId());
				ps.execute();
			}
		}
		catch (SQLException e)
		{
			_log.warn("failed: storeInDb " + e.getMessage());
		}
	}

	public void UpdateLastChar()
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
			_log.warn("failed: UpdateLastChar " + e.getMessage());
		}
	}

	/**
	 * получить текущую скорость игрока
	 * @return скорость в единицах координат в секунду (в тайле TILE_SIZE единиц)
	 */
	@Override
	public double getMoveSpeed()
	{
		return 15f;
	}

	// нагенерить объектов в гриде
	public void randomGrid()
	{
		int ido = 100;
		int gx = getPos().getGridX();
		int gy = getPos().getGridY();
		int grid;
		for (int i = 0; i < 2000; i++)
		{
			int rx = Rnd.get(0, Grid.GRID_FULL_SIZE) + (gx * 1200);
			int ry = Rnd.get(0, Grid.GRID_FULL_SIZE) + (gy * 1200);
			grid = rx / Grid.GRID_FULL_SIZE + ry / Grid.GRID_FULL_SIZE * Grid.SUPERGRID_SIZE;
			ido++;
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
	protected boolean onChatMessage(Event event)
	{
		String message = (String) event.getExtraInfo();
		if (message.startsWith("/"))
		{
			if (_accessLevel >= 100)
			{
				_log.debug("console command: " + message);
				if ("/randomgrid".equalsIgnoreCase(message))
				{
					randomGrid();
				}
				else if ("/nextid".equalsIgnoreCase(message))
				{
					IdFactory.getInstance().getNextId();
					IdFactory.getInstance().getNextId();
					IdFactory.getInstance().getNextId();
					int nextId = IdFactory.getInstance().getNextId();
					getClient().sendPacket(new CreatureSay(getObjectId(), "next id: " + nextId));
					_log.debug("nextid: " + nextId);
				}
				// заспавнить вещь себе в инвентарь
				else if (message.startsWith("/createitem") || message.startsWith("/ci"))
				{
					try
					{
						String[] v = message.split(" ");
						int typeId = Integer.parseInt(v[1]);
						int count = Integer.parseInt(v[2]);
						ObjectTemplate template = ObjectsFactory.getInstance().getTemplate(typeId);
						if (template != null)
						{
							while (count > 0)
							{
								int id = IdFactory.getInstance().getNextId();
								_log.debug("сreate item: " + template.getName() + " count: " + count + " id: " + id);
								InventoryItem item = new InventoryItem(this, typeId, 10);
								// пробуем закинуть вещь в инвентарь
								if (getInventory().putItem(item, -1, -1))
								{
									sendInteractPacket(new InventoryUpdate(getInventory()));
									item.store();
								}
								else
								{
									break;
								}
								count--;
							}
						}
					}
					catch (NumberFormatException nfe)
					{
						getClient().sendPacket(new CreatureSay(getObjectId(), "spawn item: params error"));
					}
				}
			}
			// тут исполняем обычные команды доступные для всех
			if ("/online".equalsIgnoreCase(message))
			{
				_log.debug("server online: " + World.getInstance().getPlayersCount());
				// онлайн сервера написать в чат игроку
				getClient().sendPacket(new CreatureSay(getObjectId(), "online: " + World.getInstance().getPlayersCount()));
			}

			// консольные команды проглотим
			return false;
		}
		return true;
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

	public Hand getHand()
	{
		return _hand;
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

	public Action[] getActions()
	{
		Action[] list = new Action[2];
		list[0] = new Action("craft");
		list[1] = new Action("online");
		return list;
	}
}
