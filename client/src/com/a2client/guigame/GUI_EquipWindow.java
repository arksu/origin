package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Window;
import com.a2client.model.Equip;
import com.a2client.model.InventoryItem;
import com.a2client.util.Vec2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * окно инвентаря
 * Created by arksu on 18.09.15.
 */
public class GUI_EquipWindow extends GUI_Window
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_EquipWindow.class.getName());

	private final Map<Integer, GUI_InventoryItem> _items = new HashMap<>();

	public static final List<Vec2i> _slotPos = new ArrayList<>();

	static
	{
		_slotPos.add(new Vec2i(10, 25));
		_slotPos.add(new Vec2i(10, 65));
		_slotPos.add(new Vec2i(10, 105));
		_slotPos.add(new Vec2i(10, 145));
		_slotPos.add(new Vec2i(10, 185));
	}

	public GUI_EquipWindow(GUI_Control parent)
	{
		super(parent);
		SetSize(190, 190);
	}

	public void assign(Equip equip)
	{
		for (GUI_InventoryItem item : _items.values())
		{
			item.Unlink();
		}
		_items.clear();

		for (InventoryItem item : equip.getItems())
		{
			GUI_InventoryItem ctrl = new GUI_InventoryItem(this, item);
			// поставим слоту правильную позицию
			if (item.getY() >= 0 && item.getY() < _slotPos.size())
			{
				ctrl.SetPos(_slotPos.get(item.getY()));
			}
			else
			{
				ctrl.SetPos(5, item.getY() * 40 + 40);
				_log.warn("equip slot invalid code");
			}
			_items.put(item.getY(), ctrl);
		}

		// заполняем пустые слоты
		for (int i = 0; i < _slotPos.size(); i++)
		{
			if (!_items.containsKey(i))
			{
				GUI_InventoryItem ctrl = new GUI_InventoryItem(this, 200, i);
				ctrl.SetPos(_slotPos.get(i));
			}
		}
	}
}
