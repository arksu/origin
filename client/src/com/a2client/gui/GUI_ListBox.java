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

package com.a2client.gui;

import com.a2client.Input;
import com.a2client.util.Rect;
import com.a2client.util.Vec2i;

public class GUI_ListBox extends GUI_ScrollPage
{
    protected int SelectedItem = -1;
    public boolean RenderBG = true;
    public boolean pressed = false;

    public GUI_ListBox(GUI_Control parent)
    {
        super(parent);
        skin_element = "listbox";
        min_size = new Vec2i(getSkin().GetElementSize(skin_element));
        SetStyle(true, false);
        ResetSelected();
    }

    public void DoRender()
    {
        if (RenderBG)
        {
            getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
        }

        Rect wr = WorkRect();
        // координаты текущей записи относительно контрола
        int ax = abs_pos.x + ClientRect.x - wr.x;
        int ay = abs_pos.y + ClientRect.y - wr.y;

        // для отсечки записей находящихся на границе контрола - ставим доп. скиссор
        GUIGDX.PushScissor(new Rect(abs_pos.x + ClientRect.x, abs_pos.y + ClientRect.y, wr.w, wr.h));

        int h;
        for (int i = 0; i < GetCount(); i++)
        {
            h = GetItemHeight(i);
            // если запись всяко за границами рисуемой области - пропускаем
            if ((ay + h >= abs_pos.y + ClientRect.y) && (ay < abs_pos.y + ClientRect.y + wr.h))
            {
                GUIGDX.PushScissor(new Rect(ax, ay, wr.w, h));
                DrawItemBg(i, ax, ay, wr.w, h);
                DoDrawItem(i, ax, ay, wr.w, h);

                GUIGDX.PopScissor();
            }
            ay += h;
        }
        GUIGDX.PopScissor();
    }

    protected void UpdateFullSize()
    {
        int h = 0;
        for (int i = 0; i < GetCount(); i++)
        {
            h += GetItemHeight(i);
        }
        SetFullHeight(h);
        SetFullWidth(Width());
    }

    public boolean DoMouseBtn(int btn, boolean down)
    {
        boolean result = false;
        if (!MouseInMe())
        {
            pressed = false;
            return result;
        }

        if (btn == Input.MB_LEFT)
        {
            if (down)
            {
                if (MouseInMe())
                {
                    pressed = true;
                }
            }
            else
            {
                pressed = false;
            }
        }

        Rect wr = WorkRect();
        // координаты текущей записи относительно контрола
        int ax = abs_pos.x + ClientRect.x - wr.x;
        int ay = abs_pos.y + ClientRect.y - wr.y;

        int h;
        for (int i = 0; i < GetCount(); i++)
        {
            h = GetItemHeight(i);
            // если запись всяко за границами рисуемой области - пропускаем
            if ((ay + h >= abs_pos.y + ClientRect.y) && (ay < abs_pos.y + ClientRect.y + wr.h))
            {
                boolean mouse_captured;
                mouse_captured = gui.MouseInRect(new Vec2i(ax, ay), new Vec2i(wr.w, h));

                if (mouse_captured)
                {
                    result = OnItemClick(i, btn, down);
                    if (!result)
                    {
                        int cx = gui.mouse_pos.x - ax;
                        int cy = gui.mouse_pos.y - ay;
                        result = OnItemClick(i, cx, cy, btn, down);
                    }
                    return result;
                }
            }
            ay += h;
        }

        return result;
    }

    public void SetSelected(int index)
    {
        SetSelected(index, true);
    }

    public void SetSelected(int index, boolean value)
    {
        if (value)
        {
            SelectedItem = index;
        }
        else
        {
            SelectedItem = -1;
        }
    }

    public int GetSelected()
    {
        return SelectedItem;
    }

    public int GetCount()
    {
        return 0;
    }

    public int GetItemHeight(int index)
    {
        return 0;
    }

    protected void DrawItemBg(int index, int x, int y, int w, int h)
    {
        // координаты передаются глобальные. скиссор ставит листбокс перед вызовом этой процедуры
        // рисуем обводку записи. в потомках вызываем inherited если надо
        int state;
        if (GetSelected() == index)
        {
            if (gui.MouseInRect(new Vec2i(x, y), new Vec2i(w, h)) && MouseInMe())
                state = Skin.StateHighlight_Checked;
            else
                state = Skin.StateNormal_Checked;
        }
        else
        {
            if (gui.MouseInRect(new Vec2i(x, y), new Vec2i(w, h)) && MouseInMe())
                state = Skin.StateHighlight;
            else
                state = Skin.StateNormal;
        }

        getSkin().Draw(skin_element + "_item", x, y, w, h, state);
    }

    protected void DoDrawItem(int index, int x, int y, int w, int h) {}

    protected boolean OnItemClick(int index, int btn, boolean down)
    {
        return false;
    }

    protected boolean OnItemClick(int index, int ax, int ay, int btn, boolean down)
    {
        boolean result = false;
        if (down && btn == Input.MB_LEFT)
        {
            SelectedItem = index;
            result = true;
            DoClick();
        }
        return result;
    }

    public void DoClick() {} // abstract

    public void ResetSelected()
    {
        SelectedItem = -1;
    }

    // получить индекс итема над которым мышь
    public int GetMouseItemIndex()
    {
        int result = -1;
        if (!MouseInMe())
            return result;

        Rect wr = WorkRect();
        // координаты текущей записи относительно контрола
        int ax = abs_pos.x + ClientRect.x - wr.x;
        int ay = abs_pos.y + ClientRect.y - wr.y;

        int h;
        for (int i = 0; i < GetCount(); i++)
        {
            h = GetItemHeight(i);
            // если запись всяко за границами рисуемой области - пропускаем
            if ((ay + h >= abs_pos.y + ClientRect.y) && (ay < abs_pos.y + ClientRect.y + wr.h))
            {
                boolean mouse_captured;
                mouse_captured = gui.MouseInRect(new Vec2i(ax, ay), new Vec2i(wr.w, h));

                if (mouse_captured)
                {
                    return i;
                }
            }
            ay += h;
        }
        return result;
    }

    // обновляет селекты при удалении записи из списка
    // используется по мере надобности
    protected void OnDeleteItem(int index)
    {
        if (SelectedItem == index)
            SelectedItem = -1;
        if (GetCount() <= SelectedItem)
            SelectedItem = GetCount() - 1;
        UpdateFullSize();
    }

    // обновляет селекты при инсерте новой записи
    protected void OnInsertItem(int index)
    {
        UpdateFullSize();
    }

}
