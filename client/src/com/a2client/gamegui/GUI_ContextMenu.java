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

package com.a2client.gamegui;

import com.a2client.gui.GUI;
import com.a2client.gui.GUIGDX;
import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_StringList;
import com.badlogic.gdx.Gdx;

import static com.a2client.util.Utils.max;

/**
 * Контекстное меню
 */
public class GUI_ContextMenu extends GUI_StringList
{
	private ContextMenu impl;
	public final int OFFSET = 15;

	public GUI_ContextMenu(GUI_Control parent)
	{
		super(parent);
	}

	/**
	 * показать контекстное меню в указанных координатах
	 */
	public static GUI_ContextMenu popup(ContextMenu impl)
	{
		GUI_ContextMenu control = new GUI_ContextMenu(GUI.getInstance().popup);
		control.impl = impl;
		return control;
	}

	/**
	 * добавить пункт меню
	 * @param s название
	 */
	public void addMenuItem(String s)
	{
		Add(s);
	}

	/**
	 * закончить формирование меню и вывести на экран
	 */
	public void apply()
	{
		int h = getCount() * getItemHeight(0) + 6;
		int w = 0;
		for (int i = 0; i < getCount(); i++)
		{
			w = max(w, GUIGDX.getTextWidth(font_name, getItem(i)));
		}
		w += 25;

		int x = gui._mousePos.x;
		int y = gui._mousePos.y;

		// ищем куда вывести хинт
		if (x + OFFSET + w > Gdx.graphics.getWidth())
		{
			x = Gdx.graphics.getWidth() - w;
		}
		else
		{
			x += OFFSET;
		}
		if (y + OFFSET + h > Gdx.graphics.getHeight())
		{
			y -= (h + 5);
		}
		else
		{
			y += OFFSET;
		}

		setSize(w, h);
		setPos(x, y);
	}

	public void doClick()
	{
		this.unlink();
		impl.OnContextClick(getSelectedItem());
	}

	@Override
	public boolean onMouseBtn(int btn, boolean down)
	{
		if (down && !isMouseInMe())
		{
			unlink();
		}
		return super.onMouseBtn(btn, down);
	}
}
