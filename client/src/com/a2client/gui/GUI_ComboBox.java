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
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * комбобокс - выпадающий список
 */
public class GUI_ComboBox extends GUI_Control
{
    protected List<String> Items = new ArrayList<String>();
    protected int Selected = -1;
    protected GUI_Button drop_button = null;
    protected GUI_StringList drop_list = null;
    protected String text = "";
    protected boolean pressed = false;

    public GUI_ComboBox(GUI_Control parent)
    {
        super(parent);
        drop_button = new GUI_Button(this)
        {
            public void doClick()
            {
                if (drop_list != null && !drop_list._terminated)
                    DoCollapse();
                else
                    DoDrop();
            }

            ;
        };
        drop_button.skin_element = "button_down";
        Vec2i s = getSkin().getElementSize("button_down");
        drop_button.setSize(s);
        skin_element = "listbox";
    }

    public String GetItem(int index)
    {
        if (index < 0 || index >= Items.size())
            return "";
        return Items.get(index);
    }

    public int GetCount()
    {
        return Items.size();
    }

    public void SetSelected(int value)
    {
        Selected = value;
        DoChanged();
    }

    public int GetSelected()
    {
        return Selected;
    }

    public String GetText()
    {
        if (Selected >= 0 && Selected < GetCount())
            return Items.get(Selected);
        else
            return text;
    }

    public void SetText(String value)
    {
        text = value;
        DoChanged();
    }

    protected void DoClick()
    {
        if (drop_list != null && !drop_list._terminated)
            DoCollapse();
        else
            DoDrop();
    }

    protected void DoDrop()
    {
        if (GetCount() < 1)
            return;

        if (drop_list != null)
        {
            drop_list.unlink();
        }

        drop_list = new GUI_StringList(gui.popup)
        {
            public void doClick()
            {
                OnDropListClick();
            }

            ;
        };
        for (int i = 0; i < GetCount(); i++)
            drop_list.Add(Items.get(i));
        drop_list.setPos(abs_pos.x, abs_pos.y + size.y);
        drop_list.setSize(size.x, Math.min(GetCount(), 8) * drop_list.getItemHeight(0) + 6);
        drop_list.setSelected(Selected, true);
    }

    protected void DoCollapse()
    {
        if (drop_list != null && !drop_list._terminated)
        {
            drop_list.unlink();
            drop_list = null;
        }
    }

    protected void OnDropListClick()
    {
        if (drop_list != null && !drop_list._terminated)
        {
            SetSelected(drop_list.getSelectedItem());
        }
        DoCollapse();
    }

    public void onSetSize()
    {
        super.onSetSize();
        if (drop_button != null && !drop_button._terminated)
        {
            drop_button.setPos(_clientRect.x + _clientRect.w - drop_button.getWidth(),
                               _clientRect.y + (_clientRect.h - drop_button.getHeight()) / 2);
        }
    }

    public void destroy()
    {
        super.destroy();
        if (drop_list != null && !drop_list._terminated)
        {
            drop_list.unlink();
            drop_list = null;
        }
        Items.clear();
    }

    public void render()
    {
        getSkin().draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);

        // ставим скиссор до кнопки
        GUIGDX.pushScissor(
                new Rect(abs_pos.x + _clientRect.x, abs_pos.y + _clientRect.y, _clientRect.w - drop_button.getWidth(),
                         _clientRect.h));

        // выводим текст
        GUIGDX.Text("default", abs_pos.x + _clientRect.x, abs_pos.y + _clientRect.y, GetText(), Color.WHITE);

        GUIGDX.popScissor();
    }

    public boolean onMouseBtn(int btn, boolean down)
    {
        boolean result = false;

        if (!enabled)
            return result;

        if (drop_list != null && !drop_list._terminated)
        {
            if (down)
            {
                GUI_Control c = gui._mouseInControl;
                while (c != null)
                {
                    if (c == drop_list)
                        return result;
                    c = c.parent;
                }
                DoCollapse();
                return result;
            }
        }

        if (btn == Input.MB_LEFT)
            if (down)
            {
                if (isMouseInMe())
                {
                    pressed = true;
                    result = true;
                }
            }
            else
            {
                if (pressed && isMouseInMe())
                {
                    DoClick();
                    result = true;
                }
                pressed = false;
            }
        return result;
    }

    public void DoChanged() {}

    public void AddItem(String item)
    {
        Items.add(item);
    }

    public void Clear()
    {
        Items.clear();
        Selected = -1;
    }

}
