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

package com.a2client.gui.utils;


import com.a2client.gui.GUIGDX;
import com.a2client.util.Align;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Color;

public class SimpleHint
{
    static String hint_font = "default";

    static public void Render(int x, int y, int w, int h, String s)
    {
        int cy = 0;
        String[] sl = s.split("%n");
        for (String ss : sl)
        {
            GUIGDX.Text(hint_font, x + 4, y + cy, w, h, Align.Align_Left + Align.Align_Top, ss, Color.WHITE);
            cy += GUIGDX.getTextHeight(hint_font, ss) + 5;
        }
    }

    static public Vec2i getSize(String s)
    {
        String[] sl = s.split("%n");
        int w = 0, h = 0, cw;
        for (String ss : sl)
        {
            cw = GUIGDX.getTextWidth(hint_font, ss) + 10;
            h += GUIGDX.getTextHeight(hint_font, ss) + 5;
            if (cw > w)
                w = cw;
        }

        return new Vec2i(w, h);
    }
}
