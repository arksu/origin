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

import java.util.ArrayList;
import java.util.List;

public class GUI_StringList extends GUI_ListBox
{
    protected List<String> Strings = new ArrayList<String>();
    public String font_name = "default";

    public GUI_StringList(GUI_Control parent)
    {
        super(parent);
        Strings.clear();
    }

    public String getItem(int index)
    {
        if (index < 0 || index >= Strings.size())
            return "";
        return Strings.get(index);
    }

    public void UpdateItem(int index, String value)
    {
        if (index < 0 || index >= Strings.size())
            return;
        Strings.set(index, value);
    }

    public int getCount()
    {
        return Strings.size();
    }

    public int getItemHeight(int index)
    {
        if (index < 0 || index >= Strings.size())
        {
            return 0;
        }
        return GUIGDX.getTextHeight(font_name, getItem(index)) + 2;
    }

    protected void drawItem(int index, int x, int y, int w, int h)
    {
        GUIGDX.Text(font_name, x, y, getItem(index));
    }

    public void Add(String s)
    {
        Strings.add(s);
        onInsertItem(0);
    }

    public void Insert(int index, String s)
    {
        Strings.add(index, s);
        onInsertItem(index);
    }

    public void Delete(int index)
    {
        if (index < 0 || index >= Strings.size())
            return;
        Strings.remove(index);
        onDeleteItem(index);
    }

    public void clear()
    {
        Strings.clear();
        onDeleteItem(0);
    }

}
