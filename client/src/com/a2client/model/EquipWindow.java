package com.a2client.model;

import com.a2client.gui.GUI;
import com.a2client.gamegui.GUI_EquipWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * окно экипировки
 * Created by arksu on 17.09.15.
 */
public class EquipWindow
{
	private static final Logger _log = LoggerFactory.getLogger(EquipWindow.class.getName());

	/**
	 * вещи в эквипе
	 */
	private final List<InventoryItem> _items = new ArrayList<>();

	/**
	 * инстанс окна в котором выводим инвентарть
	 */
	private GUI_EquipWindow _wnd;

	public List<InventoryItem> getItems()
	{
		return _items;
	}

	/**
	 * вызывается при любых изменениях эквипа, нужно среагировать и обновить содержимое на экране
	 */
	public void onChange()
	{
		_log.debug("onChange");

		if (_wnd != null)
		{
			_wnd.assign(this);
		}
	}

	/**
	 * показать эквип
	 */
	public void show()
	{
		if (_wnd != null)
		{
			_wnd.bringToFront();
		}
		else
		{
			_wnd = new GUI_EquipWindow(GUI.rootNormal())
			{
				@Override
				protected void DoClose()
				{
					_wnd = null;
				}
			};
			_wnd.assign(this);
			_wnd.setPos(300, 100);
		}
	}

	public void hide()
	{
		if (_wnd != null)
		{
			_wnd.unlink();
			_wnd = null;
		}
	}

	/**
	 * переключить эквип показан / спрятан
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
