package com.a2client.guigame;

import com.a2client.Input;
import com.a2client.gui.GUI_Control;
import com.a2client.model.InventoryItem;
import com.a2client.network.game.clientpackets.InventoryClick;
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

	InventoryItem _item;

	public GUI_InventoryItem(GUI_Control parent, InventoryItem item)
	{
		super(parent);

		SetPos(item.getX() * InventoryItem.WIDTH, item.getY() * InventoryItem.HEIGHT);
		SetSize(InventoryItem.WIDTH * item.getWidth(), InventoryItem.HEIGHT * item.getHeight());
		_item = item;
	}

	public GUI_InventoryItem(GUI_Control parent, int x, int y)
	{
		super(parent);
		SetPos(x * InventoryItem.WIDTH, y * InventoryItem.HEIGHT);
		SetSize(InventoryItem.WIDTH, InventoryItem.HEIGHT);
	}

	@Override
	public void DoRender()
	{
		// выводим нужного размера рамку вокруг итема
		getSkin().Draw("listbox", abs_pos.x, abs_pos.y, Width(), Height());

		// выводим иконку
		if (_item != null)
		{
			// это не пустая ячейка, надо вывести иконку предмета
			getSkin().Draw("hotbar_bg", abs_pos.x, abs_pos.y, Width(), Height());

		}

		// различная информация (ку, прогресс и тд)
	}

	public boolean contains(int x, int y)
	{
		return _item != null &&
				x >= _item.getX() && y >= _item.getY() &&
				x < _item.getX() + _item.getWidth() &&
				y < _item.getY() + _item.getHeight();
	}

	/**
	 * обработчик нажатия кнопок мыши
	 */
	@Override
	public boolean DoMouseBtn(int btn, boolean down)
	{
		if (MouseInMe() && down && parent != null)
		{
			onClick(btn);
			return true;
		}
		return false;
	}

	protected void onClick(int btn)
	{
		int mx = gui.mouse_pos.x - abs_pos.x;
		int my = gui.mouse_pos.y - abs_pos.y;

		if (parent instanceof GUI_Inventory)
		{
			new InventoryClick(
					((GUI_Inventory) parent).getInventoryId(),
					_item != null ? _item.getObjectId() : 0,
					btn,
					Input.GetKeyState(),
					mx,
					my
			).Send();
//			int xx = (x * 33 + mx - (Player.hand.isExist() ? Player.hand.offset_x : 0) + 16) / 33;
//			int yy = (y * 33 + my - (Player.hand.isExist() ? Player.hand.offset_y : 0) + 16) / 33;
//			click.x = xx;
//			click.y = yy;
		}
	}

	@Override
	public String toString()
	{
		return getClass().getName() + " pos=" + pos.toString() + " size=" + size.toString() + " item=" + _item;
	}
}
