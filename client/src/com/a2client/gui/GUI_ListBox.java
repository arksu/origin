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

import java.util.ArrayList;
import java.util.List;

public class GUI_ListBox extends GUI_ScrollPage
{
	protected int SelectedItem = -1;
	protected final List<Integer> selected = new ArrayList<>();
	public boolean RenderBG = true;
	public boolean pressed = false;
	public boolean allowMutliSelect = false;

	public GUI_ListBox(GUI_Control parent)
	{
		super(parent);
		skin_element = "listbox";
		min_size = new Vec2i(getSkin().getElementSize(skin_element));
		SetStyle(true, false);
		resetSelected();
	}

	@Override
	public void render()
	{
		if (RenderBG)
		{
			getSkin().draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
		}

		Rect wr = WorkRect();
		// координаты текущей записи относительно контрола
		int ax = abs_pos.x + _clientRect.x - wr.x;
		int ay = abs_pos.y + _clientRect.y - wr.y;

		// для отсечки записей находящихся на границе контрола - ставим доп. скиссор
		boolean bs = GUIGDX.pushScissor(new Rect(abs_pos.x + _clientRect.x, abs_pos.y + _clientRect.y, wr.w, wr.h));

		int h;
		for (int i = 0; i < getCount(); i++)
		{
			h = getItemHeight(i);
			// если запись всяко за границами рисуемой области - пропускаем
			if ((ay + h >= abs_pos.y + _clientRect.y) && (ay < abs_pos.y + _clientRect.y + wr.h))
			{
				boolean scissor = GUIGDX.pushScissor(new Rect(ax, ay, wr.w, h));
				drawItemBg(i, ax, ay, wr.w, h);
				drawItem(i, ax, ay, wr.w, h);

				if (scissor)
				{
					GUIGDX.popScissor();
				}
			}
			ay += h;
		}
		if (bs)
		{
			GUIGDX.popScissor();
		}
	}

	@Override
	protected void updateFullSize()
	{
		int h = 0;
		for (int i = 0; i < getCount(); i++)
		{
			h += getItemHeight(i);
		}
		SetFullHeight(h);
		SetFullWidth(getWidth());
	}

	@Override
	public boolean onMouseBtn(int btn, boolean down)
	{
		boolean result = false;
		if (!isMouseInMe())
		{
			pressed = false;
			return result;
		}

		if (btn == Input.MB_LEFT)
		{
			if (down)
			{
				if (isMouseInMe())
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
		int ax = abs_pos.x + _clientRect.x - wr.x;
		int ay = abs_pos.y + _clientRect.y - wr.y;

		int h;
		for (int i = 0; i < getCount(); i++)
		{
			h = getItemHeight(i);
			// если запись всяко за границами рисуемой области - пропускаем
			if ((ay + h >= abs_pos.y + _clientRect.y) && (ay < abs_pos.y + _clientRect.y + wr.h))
			{
				boolean mouse_captured;
				mouse_captured = gui.isMouseInRect(new Vec2i(ax, ay), new Vec2i(wr.w, h));

				if (mouse_captured)
				{
					result = onItemClick(i, btn, down);
					if (!result)
					{
						int cx = gui._mousePos.x - ax;
						int cy = gui._mousePos.y - ay;
						result = onItemClick(i, cx, cy, btn, down);
					}
					return result;
				}
			}
			ay += h;
		}

		return result;
	}

	public void setSelected(int index)
	{
		setSelected(index, true);
	}

	public void setSelected(int index, boolean value)
	{
		if (value && index >= 0 && index < getCount())
		{
			SelectedItem = index;
			selected.add(index);
		}
		else
		{
			SelectedItem = -1;
		}
	}

	public int getSelectedItem()
	{
		return SelectedItem;
	}

	public List<Integer> getSelected()
	{
		return selected;
	}

	public boolean isSelected(int index)
	{
		return allowMutliSelect ? selected.contains(index) : SelectedItem == index;
	}

	public int getCount()
	{
		return 0;
	}

	public int getItemHeight(int index)
	{
		return 0;
	}

	protected void drawItemBg(int index, int x, int y, int w, int h)
	{
		// координаты передаются глобальные. скиссор ставит листбокс перед вызовом этой процедуры
		// рисуем обводку записи. в потомках вызываем inherited если надо
		int state;
		if (isSelected(index))
		{
			if (gui.isMouseInRect(new Vec2i(x, y), new Vec2i(w, h)) && isMouseInMe())
			{
				state = Skin.StateHighlight_Checked;
			}
			else
			{
				state = Skin.StateNormal_Checked;
			}
		}
		else
		{
			if (gui.isMouseInRect(new Vec2i(x, y), new Vec2i(w, h)) && isMouseInMe())
			{
				state = Skin.StateHighlight;
			}
			else
			{
				state = Skin.StateNormal;
			}
		}

		getSkin().draw(skin_element + "_item", x, y, w, h, state);
	}

	protected void drawItem(int index, int x, int y, int w, int h)
	{
	}

	protected boolean onItemClick(int index, int btn, boolean down)
	{
		return false;
	}

	protected boolean onItemClick(int index, int ax, int ay, int btn, boolean down)
	{
		boolean result = false;
		if (down && btn == Input.MB_LEFT)
		{
			SelectedItem = index;
			if (allowMutliSelect)
			{
				int i = selected.indexOf(index);
				if (i >= 0)
				{
					selected.remove(i);
				}
				else
				{
					selected.add(index);
				}
			}
			result = true;
			doClick();
		}
		return result;
	}

	public void doClick()
	{
	} // abstract

	public void resetSelected()
	{
		SelectedItem = -1;
		selected.clear();
	}

	// получить индекс итема над которым мышь
	public int getMouseItemIndex()
	{
		int result = -1;
		if (!isMouseInMe())
		{
			return result;
		}

		Rect wr = WorkRect();
		// координаты текущей записи относительно контрола
		int ax = abs_pos.x + _clientRect.x - wr.x;
		int ay = abs_pos.y + _clientRect.y - wr.y;

		int h;
		for (int i = 0; i < getCount(); i++)
		{
			h = getItemHeight(i);
			// если запись всяко за границами рисуемой области - пропускаем
			if ((ay + h >= abs_pos.y + _clientRect.y) && (ay < abs_pos.y + _clientRect.y + wr.h))
			{
				boolean mouse_captured;
				mouse_captured = gui.isMouseInRect(new Vec2i(ax, ay), new Vec2i(wr.w, h));

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
	protected void onDeleteItem(int index)
	{
		if (SelectedItem == index)
		{
			SelectedItem = -1;
		}
		if (getCount() <= SelectedItem)
		{
			SelectedItem = getCount() - 1;
		}
		int i = selected.indexOf(index);
		if (i >= 0) selected.remove(i);
		updateFullSize();
	}

	// обновляет селекты при инсерте новой записи
	protected void onInsertItem(int index)
	{
		updateFullSize();
	}

}
