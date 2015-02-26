package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Инвентарь с вещами внутри
 * Created by arksu on 26.02.15.
 */
public class GUI_Inventory extends GUI_Control
{
    private static final Logger _log = LoggerFactory.getLogger(GUI_Inventory.class.getName());

    public GUI_Inventory(GUI_Control parent)
    {
        super(parent);
    }
}
