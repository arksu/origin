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

import com.a2client.util.Align;
import com.badlogic.gdx.graphics.Color;

public class GUI_Panel extends GUI_Control
{
    public boolean pressed = false;

    public enum RenderMode
    {
        rmSkin,
        rmColor,
        rmNone
    }

    public Color bg_color = Color.WHITE;
    public RenderMode render_mode = RenderMode.rmNone;

    public String caption = "";
    public int caption_align = Align.Align_Center;
    public String font = "default";
    public Color caption_color = Color.WHITE;

    public GUI_Panel(GUI_Control parent)
    {
        super(parent);
    }

    public void DoClick()
    {
    }

    public boolean onMouseBtn(int btn, boolean down)
    {
        if (!enabled)
            return false;

        if (down && isMouseInMe())
        {
            DoClick();
            return true;
        }

        return false;
    }

    public void render()
    {
        switch (render_mode)
        {
            case rmColor:
                GUIGDX.fillRect(abs_pos, size, bg_color);
                break;
            case rmSkin:
                getSkin().draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
                break;
            case rmNone:
                break;
        }

        if (!caption.isEmpty())
            GUIGDX.Text(font, abs_pos.x, abs_pos.y, size.x, size.y, caption_align, caption, caption_color);

    }
}
