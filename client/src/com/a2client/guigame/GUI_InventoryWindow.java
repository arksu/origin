package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Window;
import com.a2client.model.Inventory;
import com.a2client.model.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * специальное окно для отображения инвентаря
 * Created by arksu on 26.02.15.
 */
public class GUI_InventoryWindow extends GUI_Window
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_InventoryWindow.class.getName());

	/**
	 * контрол инвентарь для отображения содержимого
	 */
	private GUI_Inventory _inventoryControl;

	public GUI_InventoryWindow(GUI_Control parent)
	{
		super(parent);
	}

	public void assign(Inventory inventory)
	{
		if (_inventoryControl != null)
		{
			_inventoryControl.assign(inventory);
		}
		else
		{
			_inventoryControl = new GUI_Inventory(this, inventory);
			_inventoryControl.SetPos(5, 35);
			SetSize(InventoryItem.WIDTH * inventory.getWidth()+10, InventoryItem.HEIGHT * inventory.getHeight()+40);
//			updateSize();
		}
	}
}
