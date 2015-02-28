package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import com.a2client.model.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Инвентарь с вещами внутри
 * Created by arksu on 26.02.15.
 */
public class GUI_Inventory extends GUI_Control
{
	private static final Logger _log = LoggerFactory.getLogger(GUI_Inventory.class.getName());

	private Inventory _inventory;

	public GUI_Inventory(GUI_Control parent, Inventory inventory)
	{
		super(parent);
		_inventory = inventory;
	}
}
