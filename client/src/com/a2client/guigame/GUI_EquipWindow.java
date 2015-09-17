package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Window;
import com.a2client.model.Equip;
import com.a2client.model.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 18.09.15.
 */
public class GUI_EquipWindow extends GUI_Window
{
	private final List<GUI_InventoryItem> _items = new ArrayList<>();

	public GUI_EquipWindow(GUI_Control parent)
	{
		super(parent);
		SetSize(200, 200);
	}

	public void assign(Equip equip)
	{
		for (GUI_InventoryItem item : _items)
		{
			item.Unlink();
		}
		_items.clear();

		// todo заполение слотов в инвентаре
		for (InventoryItem item : equip.getItems())
		{
			GUI_InventoryItem ctrl = new GUI_InventoryItem(this, item);
			switch (item.getY())
			{
				case 0:
					ctrl.SetPos(5, 25);
					break;
				default:
					ctrl.SetPos(50, 25 * (item.getY() + 1));
			}
		}
	}
}
