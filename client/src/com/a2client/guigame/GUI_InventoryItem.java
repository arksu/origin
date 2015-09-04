package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import com.a2client.model.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * вещь в инвентаре, отрисовывает 1 слот инвентаря
 * если в слоте ничего нет - выводит пустую ячейку
 * если есть вещь - выводит ее иконку и данные по ней (качестов и прочее)
 * Created by arksu on 04.09.15.
 */
public class GUI_InventoryItem extends GUI_Control
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_InventoryItem.class.getName());

	public GUI_InventoryItem(GUI_Control parent, InventoryItem item)
	{
		super(parent);

		SetPos(item.getX() * InventoryItem.WIDTH, item.getY() * InventoryItem.HEIGHT);
		SetSize(InventoryItem.WIDTH * item.getWidth(), InventoryItem.HEIGHT * item.getHeight());
	}

	@Override
	public void DoRender()
	{
		// выводим нужного размера рамку вокруг итема
		getSkin().Draw("listbox", abs_pos.x, abs_pos.y, Width(), Height());

		// выводим иконку

		// различная информация (ку, прогресс и тд)
	}
}
