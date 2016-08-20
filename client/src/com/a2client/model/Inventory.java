package com.a2client.model;

import com.a2client.gui.GUI;
import com.a2client.gamegui.GUI_InventoryWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * инвентарь
 * Created by arksu on 26.02.15.
 */
public class Inventory
{
	private static final Logger _log = LoggerFactory.getLogger(Inventory.class.getName());

	/**
	 * объект родитель (внутри объекта может быть несколько вложенных инвентарей, вещи внутри вещей, типа сумок)
	 */
	private int _parentObjectId;

	/**
	 * ид объекта или вещи чей это инвентарь
	 */
	private int _inventoryId;

	/**
	 * размеры
	 */
	private int _width;
	private int _height;

	/**
	 * содержимое <id, item>
	 */
	private final Map<Integer, InventoryItem> _items = new HashMap<>();

	/**
	 * окно инвентаря которое отображает содержимое
	 */
	private GUI_InventoryWindow _wnd;

	public Inventory(int parentObjectId, int inventoryId, int width, int height)
	{
		_parentObjectId = parentObjectId;
		_inventoryId = inventoryId;
		_width = width;
		_height = height;
	}

	public Map<Integer, InventoryItem> getItems()
	{
		return _items;
	}

	/**
	 * добавить вещь в инвентарь или заменит если уже есть с таким же ид
	 */
	public void addItem(InventoryItem item)
	{
		_items.put(item.getObjectId(), item);
	}

	public void clear()
	{
		_items.clear();
	}

	public int getParentObjectId()
	{
		return _parentObjectId;
	}

	public int getInventoryId()
	{
		return _inventoryId;
	}

	public int getWidth()
	{
		return _width;
	}

	public int getHeight()
	{
		return _height;
	}

	public void onChange()
	{
		if (_wnd != null)
		{
			_wnd.assign(this);
		}
	}

	public void show()
	{
		// если окна еще нет - создадим
		if (_wnd == null)
		{
			_wnd = new GUI_InventoryWindow(GUI.rootNormal())
			{
				@Override
				protected void DoClose()
				{
					super.DoClose();
					_wnd = null;
				}
			};
			_wnd.assign(this);
			// TODO хранить позиции инвентарей
			_wnd.setPos(50, 50);
		}
		else
		{
			// окно есть, поместим поверх
			_wnd.bringToFront();
		}
	}

	/**
	 * скрыть инвентарь с экрана
	 */
	public void hide()
	{
		if (_wnd != null)
		{
			_wnd.unlink();
			_wnd = null;
		}
	}

	/**
	 * показать/скрыть инвентарь
	 */
	public void toggle()
	{
		if (_wnd != null)
		{
			hide();
		}
		else
		{
			show();
		}
	}
}
