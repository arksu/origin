package com.a2client.gamegui;

import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Window;
import com.a2client.model.EquipWindow;
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

	/**
	 * сами контролы эквипа <ид слота, контрол>
	 */
	private final Map<Integer, GUI_InventoryItem> _items = new HashMap<>();

	/**
	 * позиции слотов внутри окна, по индексу (номера) слота
	 */
	private static final List<Vec2i> _slotPos = new ArrayList<>();

	static
	{
		// позиции слотов
		int py = 35;

		for (int i = 0; i < 7; i++)
		{
			_slotPos.add(new Vec2i(10, py));
			py += 40;
		}
	}

	protected GUI_EquipWindow(GUI_Control parent)
	{
		super(parent);
		setSize(190, 350);
	}

	public void assign(EquipWindow equipWindow)
	{
		for (GUI_InventoryItem item : _items.values())
		{
			item.unlink();
		}
		_items.clear();

		// создаем и заполняем контролы слотов в которых есть вещи
		for (InventoryItem item : equipWindow.getItems())
		{
			GUI_InventoryItem ctrl = new GUI_InventoryItem(this, item);
			// укажем что это слот для эквипа
			ctrl.tagi = 2;

			// поставим слоту правильную позицию
			// если у нас есть заранее определенная позиция для слота с таким ид
			// ид слота хранится в Y координате вещи
			if (item.getY() >= 0 && item.getY() < _slotPos.size())
			{
				ctrl.setPos(_slotPos.get(item.getY()));
			}
			else
			{
				ctrl.setPos(5, item.getY() * 40 + 40);
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
				// укажем что это слот для эквипа
				ctrl.tagi = 2;
				ctrl.setPos(_slotPos.get(i));
			}
		}
	}
}
