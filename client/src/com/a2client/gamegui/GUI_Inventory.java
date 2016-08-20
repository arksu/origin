package com.a2client.gamegui;

import com.a2client.gui.GUI_Control;
import com.a2client.model.Inventory;
import com.a2client.model.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Инвентарь с вещами внутри
 * Created by arksu on 26.02.15.
 */
public class GUI_Inventory extends GUI_Control
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_Inventory.class.getName());

	private Inventory _inventory;
	private List<GUI_InventoryItem> _items = new ArrayList<>();

	public GUI_Inventory(GUI_Control parent, Inventory inventory)
	{
		super(parent);
		assign(inventory);
	}

	/**
	 * прицепить инвентарь
	 */
	public void assign(Inventory inventory)
	{
		// сначала чистим
		for (GUI_InventoryItem item : _items)
		{
			item.unlink();
		}
		_items.clear();

		_inventory = inventory;
		setSize(InventoryItem.WIDTH * _inventory.getWidth(), InventoryItem.HEIGHT * _inventory.getHeight());

		// тупо создаем итемы - контролы, они сами будут себя отрисовывать как надо
		for (InventoryItem item : _inventory.getItems().values())
		{
			GUI_InventoryItem ctrl = new GUI_InventoryItem(this, item);
			ctrl.tagi = 1;
			_items.add(ctrl);
		}

		// по сетке создаем недостающие пустые ячейки
		for (int x = 0; x < _inventory.getWidth(); x++)
		{
			for (int y = 0; y < _inventory.getHeight(); y++)
			{
				boolean found = false;
				for (GUI_InventoryItem item : _items)
				{
					if (item.contains(x, y))
					{
						found = true;
						break;
					}
				}
				if (!found)
				{
					GUI_InventoryItem ctrl = new GUI_InventoryItem(this, x, y);
					ctrl.tagi = 1;
					_items.add(ctrl);
				}
			}
		}
	}

	@Override
	public void render()
	{
		// возможно в будущем отрисуем какой нибудь общий фон
	}

	public Inventory getInventory()
	{
		return _inventory;
	}

	public int getInventoryId()
	{
		return _inventory.getInventoryId();
	}
}
