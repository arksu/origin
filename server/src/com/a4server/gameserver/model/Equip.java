package com.a4server.gameserver.model;

import com.a4server.Database;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.objects.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * эквип / папердолл
 * Created by arksu on 12.09.15.
 */
public class Equip
{
	private static final Logger _log = LoggerFactory.getLogger(Equip.class.getName());

	/**
	 * грузим эквип из основной таблицы с вещами, x=200
	 * y - определяет номер слота в котором находится вещь
	 * x = 200, y = 200 это рука (что находится в курсоре клиента)
	 */
	public static final String LOAD_EQUIP = "SELECT id, itemId, x, y, q, amount, stage, ticks, ticksTotal, del FROM items WHERE inventoryId=? AND x = 200 AND del=0";

	protected final Human _human;

	/**
	 * ид игрока
	 */
	protected final int _objectId;

	protected final Map<EquipItem.Slot, EquipItem> _items = new HashMap<>();

	public Equip(Human human)
	{
		_human = human;
		_objectId = human.getObjectId();
		load();
	}

	private void load()
	{
		_log.debug("load equip: " + _objectId);
		try
		{
			try (Connection con = Database.getInstance().getConnection();
			     PreparedStatement ps = con.prepareStatement(LOAD_EQUIP))
			{
				ps.setInt(1, _objectId);
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						int y = rset.getInt("y");
						// это слот рука?
						if (y == 200)
						{
							if (_human.isPlayer())
							{
								Player player = ((Player) _human);
								AbstractItem handItem = new AbstractItem(_human, rset);
								player.setHand(new Hand(player, handItem, 0, 0, 10, 10));
							}
						}
						else
						{
							EquipItem slot = new EquipItem(_human, rset);
							_items.put(slot.getSlot(), slot);
						}
					}
					_log.debug("loaded " + _items.size() + " items");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Cant load equip " + toString());
			throw new RuntimeException("Cant load equip " + toString());
		}
	}

	public Map<EquipItem.Slot, EquipItem> getItems()
	{
		return _items;
	}

	/**
	 * положить вещь в указанный слот
	 */
	public boolean putItem(AbstractItem item, EquipItem.Slot slot)
	{
		// если слот еще не занят
		if (!_items.containsKey(slot))
		{
			EquipItem equipItem = item instanceof EquipItem ? (EquipItem) item : new EquipItem(item);
			ItemTemplate template = equipItem.getTemplate();
			// определен ли в шаблоне список слотов и есть ли указанный слот в этом списке
			if (template.getEquipSlots() != null && template.getEquipSlots().contains(slot.name().toLowerCase()))
			{
				equipItem.setSlot(slot);
				_items.put(slot, equipItem);
				return true;
			}
		}
		return false;
	}

	public EquipItem takeItem(EquipItem.Slot slot)
	{
		return _items.remove(slot);
	}
}
