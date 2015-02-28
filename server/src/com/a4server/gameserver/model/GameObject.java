package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectAdd;
import com.a4server.gameserver.network.serverpackets.ObjectRemove;
import com.a4server.util.Rect;
import javolution.util.FastSet;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * игровой объект
 */
public class GameObject
{
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
	protected FastSet<GameObject> _interactWith = new FastSet<GameObject>().shared();

	/**
	 * объект в процессе удаления и ни на какие события больше не должен реагировать
	 */
	protected boolean _isDeleteing = false;

	public GameObject(int objectId, ObjectTemplate template)
	{
		if (objectId == 0)
		{
			throw new RuntimeException("objectId can not be zero");
		}
		_objectId = objectId;
		_template = template;
		_boundRect = new Rect(-_template.getWidth() / 2, -_template.getHeight() / 2, _template.getWidth() / 2,
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
		_boundRect = new Rect(-_template.getWidth() / 2, -_template.getHeight() / 2, _template.getWidth() / 2,
							  _template.getHeight() / 2);
		// есть ли у объекта инвентарь?
		if (_template.getInventory() != null)
		{
			_inventory = new Inventory(this);
		}
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

	public Rect getBoundRect()
	{
		return _boundRect;
	}

	public boolean isDeleteing()
	{
		return _isDeleteing;
	}

	/**
	 * создать пакет о добавлении меня в мир
	 * @return пакет
	 */
	public GameServerPacket makeAddPacket()
	{
		return new ObjectAdd(this);
	}

	/**
	 * создать пакет об удалении объекта из мира
	 * @return пакет
	 */
	public GameServerPacket makeRemovePacket()
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
	 * начать взаимодействие с другим объектом
	 * @param other другой объект c которым мы будем взаимодействовать
	 */
	public void beginInteract(GameObject other)
	{
		if (_interactWith.add(other))
		{
			other.interact(this);
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
			if (_inventory != null)
			{

			}
			if (wasEmpty)
			{
				// изменилось состояние
				setInteractive(true);
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

	protected void setInteractive(boolean value)
	{
//		getPos().getGrid().broadcastEvent(null);
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
}
