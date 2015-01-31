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

import com.a2client.gui.GUIGDX;
import com.a2client.gui.GUI_DragControl;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Color;

public class gui_somedrag extends GUI_DragControl
{
    public void DoRender()
    {
        GUIGDX.FillRect(abs_pos, new Vec2i(50, 50), Color.DARK_GRAY);
    }
}
