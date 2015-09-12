package com.a2client.guigame;

import com.a2client.Input;
import com.a2client.gui.GUI_Control;
import com.a2client.model.InventoryItem;
import com.a2client.network.game.clientpackets.InventoryClick;

import static com.a2client.model.InventoryItem.*;

/**
 * вещь в инвентаре, отрисовывает 1 слот инвентаря
 * если в слоте ничего нет - выводит пустую ячейку
 * если есть вещь - выводит ее иконку и данные по ней (качестов и прочее)
 * Created by arksu on 04.09.15.
 */
public class GUI_InventoryItem extends GUI_Control
{
	/**
	 * вещь
	 */
	protected InventoryItem _item;

	/**
	 * координаты слота
	 */
	protected final int _x;
	protected final int _y;

	public GUI_InventoryItem(GUI_Control parent, InventoryItem item)
	{
		super(parent);

		SetPos(item.getX() * WIDTH, item.getY() * InventoryItem.HEIGHT);
		SetSize(WIDTH * item.getWidth(), InventoryItem.HEIGHT * item.getHeight());
		_item = item;
		_x = _item.getX();
		_y = _item.getY();
	}

	public GUI_InventoryItem(GUI_Control parent, int x, int y)
	{
		super(parent);
		SetPos(x * WIDTH, y * InventoryItem.HEIGHT);
		SetSize(WIDTH, InventoryItem.HEIGHT);
		_x = x;
		_y = y;
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

		// в какой слот тыкнули (если вещь большая и может занимать несколько слотов)
		int ox = _x + mx / (WIDTH + MARGIN);
		int oy = _y + my / (HEIGHT + MARGIN);

		if (parent instanceof GUI_Inventory)
		{
			new InventoryClick(
					((GUI_Inventory) parent).getInventoryId(),
					_item != null ? _item.getObjectId() : 0,
					btn,
					Input.GetKeyState(),
					mx,
					my,
					ox,
					oy
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
