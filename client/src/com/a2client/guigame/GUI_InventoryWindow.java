package com.a2client.guigame;

import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_Window;
import com.a2client.model.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * специальное окно для отображения инвентаря
 * Created by arksu on 26.02.15.
 */
public class GUI_InventoryWindow extends GUI_Window
{
    private static final Logger _log = LoggerFactory.getLogger(GUI_InventoryWindow.class.getName());

    /**
     * объект родитель чей это инвентарь и все вложенные открытые
     */
    private GameObject _parentObject;

    /**
     * контрол инвентарь для отображения содержимого
     */
    private GUI_Inventory _inventory;

    public GUI_InventoryWindow(GUI_Control parent, GameObject parentObject)
    {
        super(parent);
        _parentObject = parentObject;
        _inventory = new GUI_Inventory(this);
    }
}
