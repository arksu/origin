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
import com.a2client.gui.utils.DragInfo;

import static com.a2client.gui.Skin.StateNormal;

public class GUI_Icon extends GUI_Control
{
	public String iname;

	public GUI_Icon(String name, GUI_Control parent)
	{
		super(parent);
		setSize(32, 32);
		if (getSkin().hasElement("icon_" + name))
		{
			iname = "icon_" + name;
		}
		else
		{
			iname = "icon_unknown";
		}
	}

	public void doClick() {}

	public boolean onMouseBtn(int btn, boolean down)
	{
		if (!enabled)
		{
			return false;
		}

		if (btn == Input.MB_LEFT && down && isMouseInMe())
		{
			doClick();
			return true;
		}
		return false;
	}

	public void render()
	{
		getSkin().draw("hotbar_slot", abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
		getSkin().draw(iname, abs_pos.x, abs_pos.y, size.x, size.y, StateNormal);
	}

	public void endDrag(DragInfo info)
	{
		// если дропнули не на слот а тащили с хотбар
		// TODO : поддержка GUI_HotbarSlot
		//        if (!(gui.mouse_in_control instanceof GUI_HotbarSlot) && (drag_parent instanceof GUI_HotbarSlot))
		//        {
		//            чистим слот
		//            ((GUI_HotbarSlot) drag_parent).ClearSlot();
		//        }
	}

}
