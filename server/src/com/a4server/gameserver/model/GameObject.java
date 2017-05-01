package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.Broadcast;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.model.ai.player.MoveActionAI;
import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.objects.InventoryTemplate;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.*;
import com.a4server.util.Rect;
import com.a4server.util.network.BaseSendPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * базовый игровой объект
 */
public class GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(GameObject.class.getName());

	public static final String LOAD_OBJECTS = "SELECT id, x, y, heading, type, q, hp, data, create_tick, last_tick FROM sg_0_obj WHERE del=0 AND grid = ?";

	public static final String STORE = "REPLACE INTO sg_0_obj (id, grid, x, y, heading, type, q, create_tick) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

	public static final String DELETE = "DELETE FROM sg_0_obj WHERE id=?";

	public static final String MARK_DELETED = "UPDATE sg_0_obj SET del=? WHERE id=?";

	public static final String UPDATE_POSITION = "UPDATE sg_0_obj SET x=?, y=?, heading=?, grid=? WHERE id=?";

	/**
	 * ид объекта, задается лишь единожды
	 */
	protected final int _objectId;

	/**
	 * качество объекта
	 */
	protected int _quality;

	/**
	 * позиция объекта в мире
	 */
	protected ObjectPosition _pos;

	/**
	 * размеры объекта
	 */
	private Rect _boundRect;

	/**
	 * шаблон объекта по которому он создан
	 */
	protected ObjectTemplate _template;

	/**
	 * инвентарь объекта
	 */
	protected Inventory _inventory;

	/**
	 * имя которое отображается над объектом
	 */
	protected String _name = "";

	/**
	 * подпись над объектом
	 */
	protected String _title = "";

	/**
	 * с кем взаимодействует объект. список двусторонний.
	 * если у меня тут есть ктото, то и у оного в списке есть я
	 */
	protected final Set<GameObject> _interactWith = ConcurrentHashMap.newKeySet(2);

	/**
	 * объект который несем над собой, или в котором едем. по сути это контейнер для вложенных объектов
	 * они больше не находятся в гриде, а обслуживаются только объектом который их "несет/везет"
	 */
	protected Map<Integer, GameObject> _lift = new ConcurrentHashMap<>();

	/**
	 * объект в процессе удаления из мира и ни на какие события больше не должен реагировать
	 */
	protected boolean _isDeleting = false;

	/**
	 * блокировка на все операции с объектом
	 */
	protected final ReentrantLock _lock = new ReentrantLock();

	/**
	 * время ожидания блокировки объекта по умолчанию
	 */
	public static final int WAIT_LOCK = 300;

	public GameObject(int objectId, ObjectTemplate template)
	{
		if (objectId == 0)
		{
			throw new RuntimeException("objectId can not be zero");
		}
		_objectId = objectId;
		_template = template;
		_boundRect = new Rect(
				-_template.getWidth() / 2,
				-_template.getHeight() / 2,
				_template.getWidth() / 2,
				_template.getHeight() / 2);
	}

	/**
	 * загружаем объект из базы в грид
	 * @param grid грид
	 * @param rset строка в базе в таблице супергрида с объектом
	 */
	public GameObject(Grid grid, ResultSet rset) throws SQLException
	{
		_objectId = rset.getInt("id");
		_pos = new ObjectPosition(
				rset.getInt("x"),
				rset.getInt("y"),
				rset.getInt("heading"),
				grid.getLevel(), grid, this);
		int typeId = rset.getInt("type");
		_quality = rset.getInt("q");
		_template = ObjectsFactory.getInstance().getTemplate(typeId);
		_boundRect = new Rect(
				-_template.getWidth() / 2,
				-_template.getHeight() / 2,
				_template.getWidth() / 2,
				_template.getHeight() / 2);
		// есть ли у объекта инвентарь?
		InventoryTemplate inventoryTemplate = _template.getInventory();
		if (inventoryTemplate != null)
		{
			_inventory = new Inventory(this, inventoryTemplate.getWidth(), inventoryTemplate.getHeight());
		}
	}

	/**
	 * сохранить объект в базу
	 */
	public boolean store()
	{
		String query = STORE;
		query = query.replaceFirst("sg_0", "sg_" + Integer.toString(getPos().getGrid().getSg()));

		// query queue
		try (Connection con = Database.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, getPos().getGrid().getId());
			statement.setInt(3, getPos().getX());
			statement.setInt(4, getPos().getY());
			statement.setInt(5, getPos().getHeading());
			statement.setInt(6, getTemplate().getTypeId());
			statement.setInt(7, getQuality());
			statement.setInt(8, GameTimeController.getInstance().getTickCount());
			statement.executeUpdate();
			con.close();
			return true;
		}
		catch (Exception e)
		{
			_log.warn("failed store object " + toString(), e);
		}
		return false;
	}

	/**
	 * пометить объект в базе как удаленный
	 */
	public boolean markDeleted()
	{
		return markDeleted(true);
	}

	public boolean markDeleted(boolean value)
	{
		if (isPlayer()) return true;

		String query = MARK_DELETED;
		query = query.replaceFirst("sg_0", "sg_" + Integer.toString(getPos().getGrid().getSg()));

		// query queue
		try (Connection con = Database.getInstance().getConnection();
		     PreparedStatement statement = con.prepareStatement(query))
		{
			statement.setInt(1, value ? 1 : 0);
			statement.setInt(2, _objectId);
			statement.executeUpdate();
			con.close();
			_isDeleting = value;
			return true;
		}
		catch (Exception e)
		{
			_log.warn("failed mark delete object " + toString(), e);
		}
		return false;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getTypeId()
	{
		return _template.getTypeId();
	}

	public int getQuality()
	{
		return _quality;
	}

	public void setQuality(int quality)
	{
		_quality = quality;
	}

	public ObjectTemplate getTemplate()
	{
		return _template;
	}

	public String getName()
	{
		return _name;
	}

	public String getTitle()
	{
		return _title;
	}

	public ObjectPosition getPos()
	{
		return _pos;
	}

	public Inventory getInventory()
	{
		return _inventory;
	}

	/**
	 * получить грид в котором находится объект
	 */
	public Grid getGrid()
	{
		return _pos.getGrid();
	}

	public Rect getBoundRect()
	{
		return _boundRect;
	}

	public boolean isDeleting()
	{
		return _isDeleting;
	}

	/**
	 * удалить объект из базы и из мира
	 */
	public void delete()
	{
		if (markDeleted())
		{
			try (GameLock ignored = getGrid().lock())
			{
				getGrid().removeObject(this);
			}

			// если есть что-то вложенное внутри
			if (_lift.size() > 0)
			{
				for (GameObject object : _lift.values())
				{
					// все это попытаемся заспавнить рядом
					object.getPos().setXY(getPos().getX(), getPos().getY());
					if (object.getPos().trySpawn())
					{
						object.getPos().store();
					}
				}
			}
		}
	}

	/**
	 * установить позицию объекту. можем сделать это только 1 раз когда объект еще не инициализирован (при создании)
	 * @param pos позиция
	 */
	public ObjectPosition setPos(ObjectPosition pos)
	{
		if (_pos == null)
		{
			_pos = pos;
		}
		else
		{
			throw new RuntimeException("try set pos, when != null");
		}
		return pos;
	}

	/**
	 * попытаться захватить блокировку на этот объект
	 * @return истина если блокировку получили
	 */
	public GameLock lock()
	{
		if (tryLock(WAIT_LOCK))
		{
			return new GameLock(this._lock);
		}
		throw new RuntimeException("failed get game object lock: " + this);
	}

	/**
	 * попытаться захватить блокировку на этот объект
	 * @param time время в мс сколько будем ждать
	 * @return истина если блокировку получили
	 */
	public boolean tryLock(int time)
	{
		try
		{
			return !_isDeleting && _lock.tryLock(time, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{
			return false;
		}
	}

	/**
	 * освободить блокировку
	 */
	public void unlock()
	{
		_lock.unlock();
	}

	public boolean isLocked()
	{
		return _lock.isLocked();
	}

	/**
	 * создать пакет о добавлении меня в мир
	 * @return пакет
	 */
	public GameServerPacket makeAddToWorldPacket()
	{
		GameServerPacket pkt = new ObjectAdd(this);
		if (isInteractive())
		{
			pkt.addNext(new ObjectInteractive(_objectId, true));
		}
		return pkt;
	}

	/**
	 * создать пакет об удалении объекта из мира
	 * @return пакет
	 */
	public GameServerPacket makeRemoveFromWorldPacket()
	{
		return new ObjectRemove(this._objectId);
	}

	@Override
	public String toString()
	{
		return "(" + getClass().getSimpleName() + ": " + (!_name
				.isEmpty() ? _name + " " : "") + "id=" + _objectId + " " + getPos() + ")";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof GameObject)
		{
			return ((GameObject) obj)._objectId == _objectId;
		}
		return super.equals(obj);
	}

	/**
	 * объект взаимодействует с другим объектом?
	 */
	public boolean isInteractive()
	{
		return !_interactWith.isEmpty();
	}

	public Set<GameObject> getInteractWith()
	{
		return _interactWith;
	}

	/**
	 * я (игрок) начинаю взаимодействие с другим объектом
	 * @param other другой объект c которым мы будем взаимодействовать
	 */
	public void beginInteract(GameObject other)
	{
		if (other != null)
		{
			this.interact(other, false);
			other.interact(this, true);
		}
	}

	/**
	 * я (подчиненный) взаимодействую с другим объектом
	 * @param other другой объект который взаимодействует со мной
	 */
	protected void interact(GameObject other, boolean impl)
	{
		boolean wasEmpty = _interactWith.isEmpty();
		if (_interactWith.add(other))
		{
			if (impl)
			{
				interactImpl(other);
			}
			if (wasEmpty)
			{
				// изменилось состояние
				// создадим для этого событие чтобы уведомить остальных
				setInteractive(true);
			}
		}
	}

	/**
	 * реакция на взаимодействие с другим объектом
	 * что я должен сделать если другой (игрок) начал взаимодействие со мной
	 * @param other другой объект который взаимодействует со мной
	 */
	protected void interactImpl(GameObject other)
	{
		// если у нас есть инвентарь
		if (_inventory != null)
		{
			// todo открыть инвентарь объекта
			Player player = other.getActingPlayer();
			if (player != null)
			{
				// TODO
//				player.sendInteractPacket();
			}
		}
	}

	/**
	 * другой объект хочет разорвать связь со мной
	 * @param other другой объект
	 */
	public void unlink(GameObject other)
	{
		_interactWith.remove(other);
		if (_interactWith.isEmpty())
		{
			// список пуст. изменим состояние
			setInteractive(false);
		}
	}

	/**
	 * установить режим взаимодействия с другим объектом
	 * например ящык - показать с открытой крышкой
	 * @param value в режиме взаимодействия?
	 */
	protected void setInteractive(boolean value)
	{
		_log.debug(toString() + " setInteractive " + value);

		Broadcast.toGrid(this, new ObjectInteractive(_objectId, value));
	}

	/**
	 * разорвать связи со всеми объектами из списка
	 */
	public void unlinkFromAll()
	{
		for (GameObject o : _interactWith)
		{
			o.unlink(this);
		}
		_interactWith.clear();
		setInteractive(false);
	}

	/**
	 * отправить пакет всем объектам с кем я взаимодействую
	 * @param pkt пакет
	 */
	public void sendInteractPacket(BaseSendPacket pkt)
	{
		for (GameObject object : _interactWith)
		{
			if (object instanceof Player)
			{
				((Player) object).getClient().sendPacket(pkt);
			}
		}
		if (this instanceof Player)
		{
			((Player) this).getClient().sendPacket(pkt);
		}
	}

	/**
	 * пункты контекстного меню объекта которые доступны в данный момент
	 */
	public List<String> getContextMenu(Player player)
	{
		return null;
	}

	/**
	 * выбран пункт контекстного меню
	 */
	public void contextSelected(final Player player, final String item)
	{
		_log.debug("contextSelected: " + item);

		// проверим что такой пункт есть в меню
		final List<String> contextMenu = getContextMenu(player);
		if (contextMenu != null && contextMenu.contains(item))
		{
			player.setAi(new MoveActionAI(player, _objectId, moveResult ->
			{
				try (GameLock ignored = player.lock())
				{
					List<String> cm = getContextMenu(player);
					if (cm != null && cm.contains(item))
					{
						contextRun(player, item);
					}
				}
			}));
		}
	}

	/**
	 * запустить выполнение пункта контекстного меню
	 */
	protected void contextRun(final Player player, final String contextItem)
	{
	}

	/**
	 * клик мышкой по объекту для совершения действия (пкм на объекте)
	 */
	public void actionClick(final Player player)
	{
		// если у объекта есть инвентарь - надо подойти к объекту и открыть его
		if (getTemplate().getInventory() != null)
		{
			// клик по объекту. бежим к нему и делаем действие над ним
			player.setAi(new MoveActionAI(player, getObjectId(), moveResult ->
			{
				player.beginInteract(this);
			}));
		}
		else
		{
			List<String> contextMenu = getContextMenu(player);
			if (contextMenu != null)
			{
				player.getClient().sendPacket(new ContextMenu(_objectId, contextMenu));
			}
		}
	}

	public boolean isPlayer()
	{
		return false;
	}

	/**
	 * это инвентарная вещь?
	 */
	public boolean isItem()
	{
		return _template.getItem() != null;
	}

	/**
	 * получить активного игрока (управляющего этим объектом)
	 */
	public Player getActingPlayer()
	{
		return null;
	}

	public Map<Integer, GameObject> getLift()
	{
		return _lift;
	}

	public GameObject getLift(int index)
	{
		return _lift.get(index);
	}

	/**
	 * добавить прилинкованный объект
	 * @param index слот в который садим объект
	 */
	public void addLift(GameObject o, int index)
	{
		if (!o.isLocked())
		{
			throw new IllegalStateException("object is not locked");
		}
		Grid grid = o.getGrid();
		try (GameLock ignored = grid.lock())
		{
			if (!_lift.containsKey(index))
			{
				_lift.put(index, o);
				o.unlinkFromAll();
				// сначала расскажем всем что объект привязали к другому
				grid.broadcastPacket(o, new ObjectLift(this));

				// и только потом удалим его нафиг из грида и мира
				grid.removeObject(o);
			}
		}
	}

	/**
	 * удалить объект ил прилинкованных слотов
	 * @param index слот
	 */
	public GameObject removeLift(int index)
	{
		GameObject object = _lift.remove(index);
		// если реально удалили
		if (object != null)
		{
			// разошлем в грид пакет
			ObjectLift pkt = new ObjectLift(this);
			Broadcast.toGrid(this, pkt);
		}
		return object;
	}

	/**
	 * объект сменил грид при движении
	 */
	public void swapGrids(Grid old, Grid n)
	{
		// TODO
	}
}
