package com.a4server.gameserver.model.inventory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * вещь в инвентаре
 * Created by arksu on 23.02.15.
 */
public class InventoryItem extends AbstractItem
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryItem.class.getName());

	/**
	 * инвентарь в котором хранится данная вещь
	 */
	private Inventory _parentInventory;

	public InventoryItem(Inventory parentInventory, ResultSet rset) throws SQLException
	{
		super(parentInventory.getObject(), rset);
		_parentInventory = parentInventory;
	}

	public Inventory getParentInventory()
	{
		return _parentInventory;
	}
}
