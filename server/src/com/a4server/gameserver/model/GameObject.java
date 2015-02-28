package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectAdd;
import com.a4server.gameserver.network.serverpackets.ObjectRemove;
import com.a4server.util.Rect;

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
	private ObjectTemplate _template;

	private Inventory _inventory;

	/**
	 * имя которое отображается над объектом
	 */
	protected String _name = "";

	/**
	 * подпись над объектом
	 */
	protected String _title = "";

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
		_pos = new ObjectPosition(rset.getInt("x"), rset.getInt("y"), grid.getLevel());
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
}
