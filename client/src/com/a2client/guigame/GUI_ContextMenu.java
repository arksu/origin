/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2013 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.a2client.guigame;

import com.a2client.Config;
import com.a2client.gui.*;

import static com.a2client.util.Utils.max;

/**
 * Контекстное меню
 */
public class GUI_ContextMenu extends GUI_StringList
{
    private ContextMenu impl;
    public final int OFFSET = 15;

    public GUI_ContextMenu(GUI_Control parent)
    {
        super(parent);
    }

    /**
     * показать контекстное меню в указанных координатах
     */
    public static GUI_ContextMenu Popup(ContextMenu impl)
    {
        GUI_ContextMenu control = new GUI_ContextMenu(GUI.getInstance().popup);
        control.impl = impl;
        return control;
    }

    /**
     * добавить пункт меню
     *
     * @param s название
     */
    public void AddMenuItem(String s)
    {
        Add(s);
    }

    /**
     * закончить формирование меню и вывести на экран
     */
    public void Apply()
    {
        int h = GetCount() * GetItemHeight(0) + 6;
        int w = 0;
        for (int i = 0; i < GetCount(); i++)
        {
            w = max(w, GUIGDX.getTextWidth(font_name, GetItem(i)));
        }
        w += 25;

        int x = gui.mouse_pos.x;
        int y = gui.mouse_pos.y;

        // ищем куда вывести хинт
        if (x + OFFSET + w > Config.getScreenWidth())
            x = Config.getScreenWidth() - w;
        else
            x += OFFSET;
        if (y + OFFSET + h > Config.getScreenHeight())
            y -= (h + 5);
        else
            y += OFFSET;

        SetSize(w, h);
        SetPos(x, y);
    }

    public void DoClick()
    {
        this.Unlink();
        impl.OnContextClick(GetSelected());
    }

    @Override
    public boolean DoMouseBtn(int btn, boolean down)
    {
        if (down && !MouseInMe())
        {
            Unlink();
        }
        return super.DoMouseBtn(btn, down);
    }
}
