package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.objects.InventoryTemplate;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectAdd;
import com.a4server.gameserver.network.serverpackets.ObjectInteractive;
import com.a4server.gameserver.network.serverpackets.ObjectRemove;
import com.a4server.util.Rect;
import com.a4server.util.network.BaseSendPacket;
import javolution.util.FastSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * базовый игровой объект
 */
public class GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(GameObject.class.getName());

	public static final String STORE = "REPLACE INTO sg_0_obj (id, grid, x, y, type, create_tick) VALUES (?, ?, ?, ?, ?, ?)";

	/**
	 * ид объекта, задается лишь единожды
	 */
	protected final int _objectId;

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
	protected final FastSet<GameObject> _interactWith = new FastSet<GameObject>().shared();

	/**
	 * объект в процессе удаления и ни на какие события больше не должен реагировать
	 */
	protected boolean _isDeleteing = false;

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
		_boundRect = new Rect(-_template.getWidth() / 2,
							  -_template.getHeight() / 2,
							  _template.getWidth() / 2,
							  _template.getHeight() / 2);
	}

	/**
	 * загружаем объект из базы в грид
	 * @param grid грид
	 * @param rset строка в базе в таблице супергрида с объектом
	 * @throws SQLException
	 */
	public GameObject(Grid grid, ResultSet rset) throws SQLException
	{
		_objectId = rset.getInt("id");
		_pos = new ObjectPosition(rset.getInt("x"), rset.getInt("y"), grid.getLevel(), grid);
		int typeId = rset.getInt("type");
		_template = ObjectsFactory.getInstance().getTemplate(typeId);
		_boundRect = new Rect(-_template.getWidth() / 2,
							  -_template.getHeight() / 2,
							  _template.getWidth() / 2,
							  _template.getHeight() / 2);
		// есть ли у объекта инвентарь?
		InventoryTemplate inventory = _template.getInventory();
		if (inventory != null)
		{
			_inventory = new Inventory(this, inventory.getWidth(), inventory.getHeight());
		}
	}

	/**
	 * сохранить объект в базу
	 */
	public boolean store()
	{
		// query queue
		try (Connection con = Database.getInstance().getConnection();
			 PreparedStatement statement = con.prepareStatement(STORE))
		{
			statement.setInt(1, getObjectId());
			statement.setInt(2, getPos().getGrid().getId());
			statement.setInt(3, getPos().getX());
			statement.setInt(4, getPos().getY());
			statement.setInt(5, getTemplate().getTypeId());
			statement.setInt(6, GameTimeController.getInstance().getTickCount());
			statement.executeUpdate();
			con.close();
			return true;
		}
		catch (Exception e)
		{
			_log.warn("failed update xy item pos " + toString(), e);
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

	/**
	 * установить позицию объекту. можем сделать это только 1 раз когда объект еще не инициализирован (при создании)
	 * @param pos позиция
	 */
	public void setPos(ObjectPosition pos)
	{
		if (_pos == null)
		{
			_pos = pos;
		}
		else
		{
			throw new RuntimeException("try set pos, when != null");
		}
	}

	public Rect getBoundRect()
	{
		return _boundRect;
	}

	public boolean isDeleteing()
	{
		return _isDeleteing;
	}

	/**
	 * попытаться захватить блокировку на этот объект
	 * @return истина если блокировку получили
	 */
	public GameLock tryLock()
	{
		if (tryLock(WAIT_LOCK))
		{
			return new GameLock(this);
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
			return !_isDeleteing && _lock.tryLock(time, TimeUnit.MILLISECONDS);
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
				.isEmpty() ? _name + " " : "") + "id=" + _objectId + ")";
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

	public FastSet<GameObject> getInteractWith()
	{
		return _interactWith;
	}

	/**
	 * начать взаимодействие с другим объектом
	 * @param other другой объект c которым мы будем взаимодействовать
	 */
	public void beginInteract(GameObject other)
	{
		boolean wasEmpty = _interactWith.isEmpty();
		if (_interactWith.add(other))
		{
			other.interact(this);
			if (wasEmpty)
			{
				// изменилось состояние
				// создадим для этого событие чтобы уведомить остальных
				setInteractive(true);
			}
		}
	}

	/**
	 * взаимодействие с другим объектом
	 * @param other другой объект который взаимодействует со мной
	 */
	protected void interact(GameObject other)
	{
		if (other == null)
		{
			return;
		}
		boolean wasEmpty = _interactWith.isEmpty();
		if (_interactWith.add(other))
		{
			interactImpl(other);
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
	@SuppressWarnings({"UnusedParameters", "StatementWithEmptyBody"})
	protected void interactImpl(GameObject other)
	{
		// если у нас есть инвентарь
		if (_inventory != null)
		{
			// todo открыть инвентарь объекта
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
		_log.debug("setInteractive " + value);
		Event evt = new Event(this, Event.EventType.INTERACT, value);
		evt.setPacket(new ObjectInteractive(_objectId, value));
		getPos().getGrid().broadcastEvent(evt);
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
	}

	public Inventory getInventory()
	{
		return _inventory;
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
	 * получить грид в котором находится объект
	 */
	public Grid getGrid()
	{
		return _pos.getGrid();
	}
}
