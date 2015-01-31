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
import com.a2client.util.Align;
import com.badlogic.gdx.graphics.Color;

public class GUI_Label extends GUI_Control
{
    public String caption = "";
    public String font = "default";
    public int align = Align.Align_Default;
    public Color color = Color.WHITE;
    public boolean pressed = false;
    public boolean multi_row = false;

    public GUI_Label(GUI_Control parent)
    {
        super(parent);
    }


    public boolean DoMouseBtn(int btn, boolean down)
    {
        if (!enabled)
            return false;

        if (btn == Input.MB_LEFT)
            if (down)
            {
                if (MouseInMe())
                {
                    pressed = true;
                    return true;
                }
            }
            else
            {
                if (pressed && MouseInMe())
                {
                    DoClick();
                    pressed = false;
                    return true;
                }
                pressed = false;
            }
        return false;
    }

    public void DoClick()
    {
    }

    public void DoRender()
    {
        if (caption.isEmpty())
            return;

        if (multi_row)
        {
            int ay = 0;
            String[] sl = caption.split("%n");
            for (String cc : sl)
            {
                GUIGDX.Text(font, abs_pos.x, abs_pos.y + ay, size.x, size.y, align, cc, color);
                ay += GUIGDX.getTextHeight(font, "aW12");
            }
        }
        else
            GUIGDX.Text(font, abs_pos.x, abs_pos.y, size.x, size.y, align, caption, color);
    }

    public void UpdateSize()
    {
        SetSize(GUIGDX.getfontMetrics(font, caption));
    }

}
