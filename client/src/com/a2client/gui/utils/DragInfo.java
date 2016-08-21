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

import com.a2client.gui.GUI_Control;
import com.a2client.gui.GUI_DragControl;
import com.a2client.util.Vec2i;

public class DragInfo
{
	public static final int DRAG_STATE_NONE = 0;
	// over empty space or controls that don't have drag'n'drop
	public static final int DRAG_STATE_MISS = 1;
	// over another Object that accept dropping on it
	public static final int DRAG_STATE_ACCEPT = 2;
	// over another Object that refuse dropping on it
	public static final int DRAG_STATE_REFUSE = 3;
	//--------------------------------------------------------------------

	// состояние перетаскивания
	public int state = DRAG_STATE_NONE;
	// перетаскиваемый контрол
	public GUI_DragControl _dragControl = null;
	// смещение перетаскиваемого объекта относительно мыши
	public Vec2i _hotspot = new Vec2i(0, 0);

	public void reset()
	{
		state = DRAG_STATE_NONE;

		if (_dragControl != null)
		{
			GUI_Control c = _dragControl;
			_dragControl = null;
			c.unlink();
		}

		_hotspot = new Vec2i(0, 0);
	}
}
