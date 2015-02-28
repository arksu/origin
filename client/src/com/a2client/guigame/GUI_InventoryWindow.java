package com.a2client.guigame;

import com.a2client.InventoryCache;
import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Window;
import com.a2client.model.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * специальное окно для отображения инвентаря
 * Created by arksu on 26.02.15.
 */
public class GUI_InventoryWindow extends GUI_Window
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_InventoryWindow.class.getName());

	private int _inventoryId;

	/**
	 * контрол инвентарь для отображения содержимого
	 */
	private GUI_Inventory _inventory;

	public GUI_InventoryWindow(GUI_Control parent, int inventoryId)
	{
		super(parent);
		_inventoryId = inventoryId;
		Inventory inv = InventoryCache.getInstance().get(inventoryId);
		if (inv != null)
		{
			_inventory = new GUI_Inventory(this, inv);
			_inventory.SetPos(5, 5);
		}
	}
}
