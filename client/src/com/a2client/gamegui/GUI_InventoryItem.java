package com.a2client.gamegui;

import com.a2client.Input;
import com.a2client.gui.GUI_Control;
import com.a2client.model.InventoryItem;
import com.a2client.network.game.clientpackets.EquipClick;
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

	/**
	 * слот с вещью
	 * @param parent
	 * @param item
	 */
	public GUI_InventoryItem(GUI_Control parent, InventoryItem item)
	{
		super(parent);

		setPos(item.getX() * WIDTH, item.getY() * HEIGHT);
		setSize(WIDTH * item.getWidth(), HEIGHT * item.getHeight());
		_item = item;
		_x = _item.getX();
		_y = _item.getY();

		_simpleHint = "<" + item.getIcon() + "> q=" + item.getQ() + " cnt=" + item.getAmount();
	}

	/**
	 * пустой слот, без вещи
	 * @param parent
	 * @param x
	 * @param y
	 */
	public GUI_InventoryItem(GUI_Control parent, int x, int y)
	{
		super(parent);
		setPos(x * WIDTH, y * HEIGHT);
		setSize(WIDTH, HEIGHT);
		_x = x;
		_y = y;
	}

	@Override
	public void render()
	{
		// выводим нужного размера рамку вокруг итема
		getSkin().draw("listbox", abs_pos.x, abs_pos.y, getWidth(), getHeight());

		// выводим иконку
		if (_item != null)
		{
			// это не пустая ячейка, надо вывести иконку предмета
			getSkin().draw("icon_" + _item.getIcon(), abs_pos.x, abs_pos.y, getWidth(), getHeight());
		}

		// todo различная информация (ку, прогресс и тд)
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
	public boolean onMouseBtn(int btn, boolean down)
	{
		if (isMouseInMe() && down && parent != null)
		{
			onClick(btn);
			return true;
		}
		return false;
	}

	private void onClick(int btn)
	{
		int mx = gui._mousePos.x - abs_pos.x;
		int my = gui._mousePos.y - abs_pos.y;

		// в какой слот тыкнули (если вещь большая и может занимать несколько слотов)
		int ox = _x + mx / (WIDTH + MARGIN);
		int oy = _y + my / (HEIGHT + MARGIN);

		// это слот инвентаря?
		if (tagi == 1)
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
			).send();
		}
		// это эквип слот?
		else if (tagi == 2)
		{
			new EquipClick(
					_item != null ? _item.getObjectId() : 0,
					btn,
					Input.GetKeyState(),
					mx,
					my,
					_y
			).send();
		}
	}

	@Override
	public String toString()
	{
		return getClass().getName() + " pos=" + pos.toString() + " size=" + size.toString() + " item=" + _item;
	}
}
