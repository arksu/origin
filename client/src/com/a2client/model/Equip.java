package com.a2client.model;

import com.a2client.gui.GUI;
import com.a2client.guigame.GUI_EquipWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 17.09.15.
 */
public class Equip
{
	private static final Logger _log = LoggerFactory.getLogger(Equip.class.getName());

	private final List<InventoryItem> _items = new ArrayList<>();

	GUI_EquipWindow _wnd;

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

	public void show()
	{
		if (_wnd != null)
		{
			_wnd.BringToFront();
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
			_wnd.SetPos(300, 100);
		}
	}

	public void hide()
	{
		if (_wnd != null)
		{
			_wnd.Unlink();
			_wnd = null;
		}
	}

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
