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

import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Color;

// класс для вывода картинки из скина
public class GUI_Image extends GUI_Control
{
	// прилеплена к мыши
	public boolean drag = false;
	public Vec2i drag_offset = Vec2i.z;

	public GUI_Image(GUI_Control parent)
	{
		super(parent);
	}

	public boolean onMouseBtn(int btn, boolean down)
	{
		return !drag && isMouseInMe() && down;
	}

	public boolean checkMouseInControl()
	{
		return !drag && visible;
	}

	public void update()
	{
		if (drag)
		{
			setSize(getSkin().getElementSize(skin_element));
			setPos(gui._mousePos.sub(drag_offset));
			bringToFront();
		}
	}

	public void render()
	{
		Color c = drag ? (new Color(255, 255, 255, 200)) : Color.WHITE;
		getSkin().draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y, Skin.StateNormal, c);
	}
}
