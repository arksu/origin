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

import com.a2client.gui.utils.DragInfo;
import com.a2client.util.Rect;
import com.a2client.util.Vec2i;

import static com.a2client.util.Utils.max;

public class GUI_Control
{
	// гуи менеджер
	public final GUI gui;
	// ид контрола (для локальных -1)
	public int id = -1;
	// положение и размер
	public Vec2i pos = Vec2i.z;
	public Vec2i size = Vec2i.z;
	public Vec2i min_size = new Vec2i(10, 10);
	public Vec2i margins = new Vec2i(12, 12);
	public String skin_element = "";
	// юзер тег
	public String tag = "";
	// юезр тег целочисленный
	public int tagi = 0;
	// абсолютные координаты на экране
	protected Vec2i abs_pos = Vec2i.z;
	// рабочая область
	protected Rect _clientRect = new Rect(0, 0, 0, 0);
	// рендерить ли детей. если нет - то рендерим в ручном режиме.
	public boolean _renderChilds = true;
	// уничтожен ли контрол. елси истина - любое использование контрола не допускается
	// его необходимо исключить из всех обработок и навсегда о нем забыть.
	public boolean _terminated = false;

	/**
	 * простой ли хинт у контрола
	 * если простой - выводится тупо текст. берется в getHint()
	 * если хинт продвинутый - контрол сам выводит содержимое. Vec2i getHintSize() - должен вернуть размер области под хинт
	 * renderHint(int x, int y) - вывести сам хинт в этих координатах.
	 * гуи сам ищет оптимальное расположение хинта, а также выводит подложку если стоит _needHintBg - иначе контрол должен вывести еще и подложку
	 */
	public boolean _isSimpleHint = true;

	/**
	 * текст простого хинта
	 */
	public String _simpleHint = "";

	// нужно ли вывести подложку под хинт
	public boolean _needHintBg = true;

	public GUI_Control prev, next, child, last_child, parent;
	public boolean visible = true;
	public boolean focusable = false;
	public boolean enabled = true;
	public boolean _isWindow = false;
	public boolean _dragEnabled = false;

	//--------------------------------------------------------------------------------------------

	public GUI_Control(GUI gui)
	{
		this.gui = gui;
		afterCreate();
	}

	public GUI_Control(GUI_Control parent)
	{
		if (parent == null)
		{
			this.gui = GUI.getInstance();
			this.parent = gui.normal;
			link();
		}
		else
		{
			synchronized (parent.gui)
			{
				this.gui = parent.gui;
				this.parent = parent;
				link();
			}
		}
		afterCreate();
	}

	private void link()
	{
		synchronized (gui)
		{
			if (parent.last_child != null)
			{
				parent.last_child.next = this;
			}
			if (parent.child == null)
			{
				parent.child = this;
			}
			this.prev = parent.last_child;
			parent.last_child = this;
			updateAbsPos();
		}
	}

	public void unlinkChilds()
	{
		while (child != null)
			child.unlink();
	}

	public void unlink()
	{
		if (_terminated)
		{
			return;
		}

		_terminated = true;
		destroy();
		synchronized (gui)
		{
			gui.onUnlink(this);
			if (next != null)
			{
				next.prev = prev;
			}
			if (prev != null)
			{
				prev.next = next;
			}
			if (parent != null)
			{
				if (parent.child == this)
				{
					parent.child = next;
				}
				if (parent.last_child == this)
				{
					parent.last_child = prev;
				}
			}
			next = null;
			prev = null;
		}
	}

	final void doUpdate()
	{
//		if (!((this == gui.root) || (this == gui.custom)))
//		{
//			return;
//		}

		if (gui._dragInfo._dragControl == this)
		{
			setX(gui._mousePos.x - gui._dragInfo._hotspot.x);
			setY(gui._mousePos.y - gui._dragInfo._hotspot.y);
		}
		update();
		for (GUI_Control c = child; c != null; c = c.next)
		{
			c.doUpdate();
		}
	}

	public int childsCount()
	{
		int r = 0;
		for (GUI_Control c = child; c != null; c = c.next)
		{
			r++;
		}
		return r;
	}

	final void doRender()
	{
		// если не рендерим гуй. выходим если это руты
//		if (!((this == gui.root) || (this == gui.custom)))
//		{
//			return;
//		}

		if (visible)
		{
			render();
			if (_renderChilds)
			{
				for (GUI_Control c = child; c != null; c = c.next)
				{
					c.doRender();
				}
			}
			afterRenderChilds();
		}
	}

	public void setPos(Vec2i pos)
	{
		this.pos = new Vec2i(pos);
		for (GUI_Control c = child; c != null; c = c.next)
		{
			c.setPos(c.pos);
		}
		updateAbsPos();
	}

	public void setPos(int x, int y)
	{
		this.pos = new Vec2i(x, y);
		for (GUI_Control c = child; c != null; c = c.next)
		{
			c.setPos(c.pos);
		}
		updateAbsPos();
	}

	public void setX(int x)
	{
		this.pos = new Vec2i(x, pos.y);
		for (GUI_Control c = child; c != null; c = c.next)
		{
			c.setPos(c.pos);
		}
		updateAbsPos();
	}

	public void setY(int y)
	{
		this.pos = new Vec2i(pos.x, y);
		for (GUI_Control c = child; c != null; c = c.next)
		{
			c.setPos(c.pos);
		}
		updateAbsPos();
	}

	public void centerX()
	{
		if (parent != null)
		{
			setX((parent.size.x - size.x) / 2);
		}
	}

	public void centerY()
	{
		if (parent != null)
		{
			setY((parent.size.y - size.y) / 2);
		}
	}

	public void center()
	{
		centerX();
		centerY();
	}

	public void setWidth(int val)
	{
		size = new Vec2i(max(val, min_size.x), size.y);
		onSetSize();
	}

	public void setHeight(int val)
	{
		size = new Vec2i(size.x, max(val, min_size.y));
		onSetSize();
	}

	public void setSize(int w, int h)
	{
		size = new Vec2i(max(w, min_size.x), max(h, min_size.y));
		onSetSize();
	}

	public void setSize(GUI_Control c)
	{
		setSize(c.size.x, c.size.y);
	}

	public void setSize(Vec2i c)
	{
		setSize(c.x, c.y);
	}

	public int getHeight()
	{
		return size.y;
	}

	public int getWidth()
	{
		return size.x;
	}

	public void updateAbsPos()
	{
		abs_pos = new Vec2i(pos);
		GUI_Control ctrl = parent;
		while (ctrl != null)
		{
			abs_pos = abs_pos.add(ctrl.pos);
			ctrl = ctrl.parent;
		}
		onSetPos();
	}

	public void show()
	{
		visible = true;
	}

	public void hide()
	{
		visible = false;
	}

	public boolean toggleVisible()
	{
		if (visible)
		{
			hide();
		}
		else
		{
			show();
		}
		return visible;
	}

	public void updateSize()
	{
		size.x = 0;
		size.y = 0;

		for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
		{
			int right = ctrl.pos.x + ctrl.size.x;
			int bottom = ctrl.pos.y + ctrl.size.y;
			if (right > size.x)
			{
				size.x = right;
			}

			if (bottom > size.y)
			{
				size.y = bottom;
			}
		}
		size.add(margins);
	}

	public final boolean handleKey(char c, int code, boolean down)
	{
		boolean r = onKey(c, code, down);
		if (r)
		{
			return true;
		}
		for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
		{
			r = ctrl.onKey(c, code, down);
			if (r)
			{
				return true;
			}
		}
		return false;
	}

	public final boolean handleMouseBtn(int btn, boolean down)
	{
		boolean r = false;
		for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
		{
			if (ctrl.enabled && ctrl.visible)
			{
				if (down)
				{
					r = ctrl.handleMouseBtn(btn, down);
					if (r)
					{
						break;
					}
				}
				else
				{
					ctrl.handleMouseBtn(btn, down);
				}
			}
		}
		if (enabled && visible)
		{
			if (down)
			{
				if (!r)
				{
					r = onMouseBtn(btn, down);
				}
			}
			else
			{
				onMouseBtn(btn, down);
			}

		}
		if (down && r)
		{
			GUI_Control ctrl = parent;
			while (ctrl != null)
			{
				if (ctrl._isWindow)
				{
					ctrl.bringToFront();
				}
				ctrl = ctrl.parent;
			}
		}
		return r;
	}

	public final boolean handleMouseWheel(boolean isUp, int len)
	{
		boolean r = false;
		for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
		{
			if (ctrl.visible && ctrl.enabled)
			{
				r = ctrl.handleMouseWheel(isUp, len);
			}
			if (r)
			{
				return true;
			}
		}
		if (enabled && visible)
		{
			r = onMouseWheel(isUp, len);
		}
		return r;
	}

	public GUI_Control getMouseInControl()
	{
		GUI_Control ret = null;
		if (enabled)
		{
			for (GUI_Control ctrl = last_child; ctrl != null; ctrl = ctrl.prev)
			{
				if (gui.isMouseInRect(ctrl.abs_pos, ctrl.size))
				{
					if (!ctrl.checkMouseInControl())
					{
						continue;
					}
				}
				ret = ctrl.getMouseInControl();
				if (ret != null)
				{
					return ret;
				}
			}
			if (checkMouseInControl() && gui.isMouseInRect(abs_pos, size))
			{
				ret = this;
			}
		}
		return ret;
	}

	public void bringToFront()
	{
		if (gui.isRoot(this))
		{
			return;
		}
		if (parent == null)
		{
			return;
		}
		if (parent.last_child == this)
		{
			return;
		}

		if (prev != null)
		{
			prev.next = next;
		}
		if (next != null)
		{
			next.prev = prev;
		}
		if (parent.last_child != null)
		{
			parent.last_child.next = this;
		}

		if (parent.child == this)
		{
			parent.child = next;
		}
		prev = parent.last_child;
		next = null;

		parent.last_child = this;
	}

	public void sendToBack()
	{
		if (gui.isRoot(this))
		{
			return;
		}
		if (parent == null)
		{
			return;
		}
		if (parent.child == this)
		{
			return;
		}

		if (prev != null)
		{
			prev.next = next;
		}
		if (next != null)
		{
			next.prev = prev;
		}
		if (parent.child != null)
		{
			parent.child.prev = this;
		}

		if (parent.last_child == this)
		{
			parent.last_child = prev;
		}
		next = parent.child;
		prev = null;
		parent.child = this;
	}

	public void beginDragMove()
	{
		gui._dragMoveControl = this;
	}

	public void endDragMove()
	{
		if (isSelfDragged())
		{
			gui._dragMoveControl = null;
		}
	}

	protected boolean isSelfDragged()
	{
		return gui._dragMoveControl == this;
	}

	public Skin getSkin()
	{
		return Skin.getInstance();
	}

	// условие по котороу определяется мышь в контроле
	public boolean checkMouseInControl()
	{
		return visible;
	}

	public boolean isFocused()
	{
		return gui._focusedControl == this;
	}

	public boolean isMouseInMe()
	{
		return gui._mouseInControl == this;
	}

	public boolean mouseInChilds()
	{
		if (isMouseInMe())
		{
			return true;
		}
		for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
		{
			if (ctrl.mouseInChilds())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isDragOver()
	{
		return (gui.haveDrag() && isMouseInMe());
	}

	// запрос на возможность принять контрол
	public boolean doRequestDrop(DragInfo info)
	{
		return false;
	}

	// получить текст хинта
	public String getHint()
	{
		return _simpleHint;
	}

	// получить размер хинта
	public Vec2i getHintSize()
	{
		return Vec2i.z;
	}

	// вывести хинт
	public void renderHint(int x, int y, int w, int h)
	{

	}

	// ОБРАБОТЧИКИ СОБЫТИЙ --------------------------------------------------------------------------------------------------------

	// обработчик получения фокуса
	public void onGetFocus()
	{
	}

	// обработчик потери фокуса
	public void onLostFocus()
	{
	}

	// обработчик нажатия клавиш
	public boolean onKey(char c, int code, boolean down)
	{
		return false;
	}

	// обработчик нажатия кнопок мыши
	public boolean onMouseBtn(int btn, boolean down)
	{
		return false;
	}

	/**
	 * обработчик вращения колеса мыши
	 * @param isUp вверх?
	 * @param len сколько щелчков
	 * @return обработали ли?
	 */
	public boolean onMouseWheel(boolean isUp, int len)
	{
		return false;
	}

	// обработчик апдейта
	public void update()
	{
	}

	// обработчик рендера
	public void render()
	{
	}

	// выполняется после рендера себя и всех детей
	public void afterRenderChilds()
	{
	}

	// смена позиции
	public void onSetPos()
	{
	}

	// смена размера
	public void onSetSize()
	{
		_clientRect = new Rect(3, 3, size.x - 6, size.y - 6);
	}

	// завершить перетаскивание
	public void endDrag(DragInfo info)
	{
	}

	public void updateDrag(DragInfo info)
	{
	}

	// уничтожение контрола
	public void destroy()
	{
	}

	// создание контрола
	public void afterCreate()
	{
	}

	public String toString()
	{
		return getClass().getName() + " pos=" + pos.toString() + " size=" + size.toString();
	}

}

