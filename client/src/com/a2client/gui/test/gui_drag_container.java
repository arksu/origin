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

package com.a2client.gui.test;

import com.a2client.Input;
import com.a2client.Log;
import com.a2client.gui.GUIGDX;
import com.a2client.gui.GUI_Control;
import com.a2client.gui.utils.DragInfo;
import com.badlogic.gdx.graphics.Color;

public class gui_drag_container extends GUI_Control
{
    // имеем ли мы нечто что можно перетащить
    public boolean have_some = false;
    // что то над нами тащат...
    public boolean drag_above_me = false;

    public gui_drag_container(GUI_Control parent)
    {
        super(parent);
        _dragEnabled = true;
    }

    boolean pressed = false;

    public boolean onMouseBtn(int btn, boolean down)
    {
        if (down)
        {
            if (pressed)
            {
                pressed = false;
                DoClick();
            }
            pressed = true;
        }
        else
            pressed = false;
        // в контейнере начинаем драг
        if (down && btn == Input.MB_LEFT && isMouseInMe() && have_some)
        {
            // если левой мышкой чото начинаем тащить
            //gui.BeginCheckDrag(this);
            gui.beginDrag(this, new gui_somedrag(), gui._mousePos.sub(abs_pos));
            // при этом вещь не удаляется из контейнера.
            // удалить надо только когда закончится драг.
            return true;
        }
        return false;
    }

    void DoClick()
    {
        Log.info("CLICKED");
    }

    // запрос на возможность принять контрол
    public boolean doRequestDrop(DragInfo info)
    {
        // тут по идее надо проверять что мы пытаемся дропнуть на контейнер.
        // и вернуть истину только если дроп возможен.
        return (info._dragControl instanceof gui_somedrag);
    }

    public void endDrag(DragInfo info)
    {
        // закончили перетаскивание
        // тут момент бросания некой вещи на этот контейнер
        // (не с которого начали перетаскивание)
        if (info._dragControl instanceof gui_somedrag)
        {
            ((gui_drag_container) info._dragControl.drag_parent).have_some = false;
            have_some = true;
        }
    }

    public void update()
    {
        drag_above_me = (isMouseInMe() && gui._dragInfo.state == DragInfo.DRAG_STATE_ACCEPT);
    }

    public void render()
    {
        if (drag_above_me)
            GUIGDX.fillRect(abs_pos, size, Color.YELLOW);
        else if (have_some)
            GUIGDX.fillRect(abs_pos, size, Color.RED);
        else
            GUIGDX.fillRect(abs_pos, size, Color.WHITE);
    }

}
