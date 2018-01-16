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

import com.a2client.Config;
import com.a2client.Input;
import com.a2client.Main;
import com.a2client.gui.utils.DragInfo;
import com.a2client.gui.utils.SimpleHint;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;

public class GUI
{
	private static GUI _instance = new GUI();

	// core
	public GUI_Control root;
	public GUI_Control normal;
	public GUI_Control popup;
	public GUI_Control modal;
	public GUI_Control custom;
	public Vec2i _mousePos = Vec2i.z;
	public GUI_Control _mouseInControl = null;
	public GUI_Control _focusedControl = null;
	public GUI_Control _mouseGrabber = null;
	public GUI_Control _dragMoveControl = null;
	public DragInfo _dragInfo = new DragInfo();
	private boolean _active = true;

	private static final int MOUSE_DBL_CLICK_TIME = 250;
	private long MbLeftPressTime = MOUSE_DBL_CLICK_TIME + 1;
	private Vec2i MbLeftPressCoord = Vec2i.z;
	private boolean[] _mouseBtns = new boolean[3];

	// отступ от мыши для хинта
	private static final int HINT_OFFSET = 10;

	static public GUI getInstance() { return _instance; }

	static public void reCreate()
	{
		_instance = new GUI();
	}

	public GUI()
	{
		root = new GUI_Control(this);
		root.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		custom = new GUI_Control(root);
		normal = new GUI_Control(root);
		modal = new GUI_Control(root);
		popup = new GUI_Control(root);
		custom.setSize(root);
		normal.setSize(root);
		popup.setSize(root);
		modal.setSize(root);
	}

	public void setActive(boolean val)
	{
		this._active = val;
	}

	public boolean getActive()
	{
		return this._active;
	}

	public static GUI_Control rootNormal()
	{
		return getInstance().normal;
	}

	public static GUI_Control rootModal()
	{
		return getInstance().modal;
	}

	public static GUI_Control rootPopup()
	{
		return getInstance().popup;
	}

	public void update()
	{
		if (!_active)
		{
			return;
		}

		updateMousePos();
		if (!GUI_Debug.active)
		{
			updateMouseButtons();
		}
		UpdateMouseWheel();
		updateDragState();

		root.doUpdate();
	}

	public void render()
	{
		if (!_active)
		{
			return;
		}
		root.doRender();
		renderHint();
	}

	public void renderHint()
	{
		if (_mouseInControl == null)
		{
			return;
		}

		int w, h;
		String text = "";

		// получаем размер
		if (_mouseInControl._isSimpleHint)
		{
			text = _mouseInControl.getHint();
			if (text == null || text.length() == 0)
			{
				return;
			}
			Vec2i sz = SimpleHint.getSize(text);
			w = sz.x;
			h = sz.y;
		}
		else
		{
			Vec2i sz = _mouseInControl.getHintSize();
			w = sz.x;
			h = sz.y;
		}
		if (w == 0 || h == 0)
		{
			return;
		}

		int x = _mousePos.x;
		int y = _mousePos.y;

		// ищем куда вывести хинт
		// сначала определимся с X
		if (x + HINT_OFFSET + w > Config.getInstance().getScreenWidth())
		{
			x = Config.getInstance().getScreenWidth() - w;
		}
		else
		{
			x += HINT_OFFSET;
		}

		// определимся с Y
		if (y - HINT_OFFSET - h < 0)
		{
			y += 5;
		}
		else
		{
			y -= HINT_OFFSET + h;
		}

		// выводим хинт
		if (_mouseInControl._needHintBg)
		{
			Skin.getInstance().draw("hint", x, y, w, h);
		}
		if (_mouseInControl._isSimpleHint)
		{
			SimpleHint.render(x, y, w, h, text);
		}
		else
		{
			_mouseInControl.renderHint(x, y, w, h);
		}
	}

	public boolean isRoot(GUI_Control c)
	{
		return (c == root || c == normal || c == popup || c == modal || c == custom);
	}

	public void handleKey(char c, int code, boolean down)
	{
		if (!_active)
		{
			return;
		}
		boolean r = false;

		if (_focusedControl != null)
		{
			r = _focusedControl.onKey(c, code, down);
		}
		if (r)
		{
			return;
		}

		for (GUI_Control ctrl = root.last_child; ctrl != null; ctrl = ctrl.prev)
		{
			r = ctrl.handleKey(c, code, down);
			if (r)
			{
				return;
			}
		}
	}

	public void updateMousePos()
	{
		Vec2i oldPos = new Vec2i(_mousePos);
		_mousePos = new Vec2i(Input.MouseX, Input.MouseY);
		if ((_mousePos.x - oldPos.x != 0) || (_mousePos.y - oldPos.y != 0))
		{
			onMouseMoved(new Vec2i(_mousePos.x - oldPos.x, _mousePos.y - oldPos.y));
		}
		if (_dragMoveControl != null)
		{
			_mouseInControl = _dragMoveControl;
		}
		else
		{
			_mouseInControl = getMouseInControl();
		}
	}

	public void updateMouseButtons()
	{
		int btn = Input.MB_LEFT;
		MbLeftPressTime += Main.DT;
		boolean[] oldBtns = new boolean[3];
		oldBtns[0] = _mouseBtns[0];
		oldBtns[1] = _mouseBtns[1];
		oldBtns[2] = _mouseBtns[2];
		for (int i = 0; i < 3; i++)
		{
			_mouseBtns[i] = Input.MouseBtns[i];
			// узнаем на какую кнопку нажали
			if (_mouseBtns[i] != oldBtns[i])
			{
				switch (i)
				{
					case Input.MB_LEFT:
						if (_mouseBtns[i])
						{
							if (MbLeftPressTime < MOUSE_DBL_CLICK_TIME && MbLeftPressCoord.equals(_mousePos))
							{
								btn = Input.MB_DOUBLE;
								MbLeftPressTime = MOUSE_DBL_CLICK_TIME;
							}
							else
							{
								btn = Input.MB_LEFT;
								MbLeftPressTime = 0;
								MbLeftPressCoord = new Vec2i(_mousePos);
							}
						}
						else
						{
							btn = Input.MB_LEFT;
						}
						break;
					case Input.MB_RIGHT:
						btn = Input.MB_RIGHT;
						break;
					case Input.MB_MIDDLE:
						btn = Input.MB_MIDDLE;
						break;
				}

				if (_mouseInControl != null && _mouseInControl != _focusedControl)
				{
					if (_mouseBtns[i])
					{
						_focusedControl = null;
					}
				}

				if (haveDrag())
				{
					if (!_mouseBtns[i] && btn == Input.MB_LEFT)
					{
						endDrag(false);
					}
				}
				else
				{
					if (_mouseGrabber != null)
					{
						if (!_mouseGrabber.onMouseBtn(btn, _mouseBtns[i]))
						{
							if (!popup.handleMouseBtn(btn, _mouseBtns[i]))
							{
								if (!modal.handleMouseBtn(btn, _mouseBtns[i]))
								{
									if (modal.childsCount() == 0)
									{
										if (!normal.handleMouseBtn(btn, _mouseBtns[i]))
										{
											custom.handleMouseBtn(btn, _mouseBtns[i]);
										}
									}
								}
							}
						}
					}
					else if (!popup.handleMouseBtn(btn, _mouseBtns[i]))
					{
						if (!modal.handleMouseBtn(btn, _mouseBtns[i]))
						{
							if (modal.childsCount() == 0)
							{
								if (!normal.handleMouseBtn(btn, _mouseBtns[i]))
								{
									custom.handleMouseBtn(btn, _mouseBtns[i]);
								}
							}
						}
					}
				}
			}
		}
	}

	public void UpdateMouseWheel()
	{
		int mw = Input.MouseWheel;
		if (mw != 0)
		{
			if (!popup.handleMouseWheel(mw > 0, Math.abs(mw)))
			{
				if (!modal.handleMouseWheel(mw > 0, Math.abs(mw)))
				{
					if (modal.childsCount() == 0)
					{
						if (!normal.handleMouseWheel(mw > 0, Math.abs(mw)))
						{
							custom.handleMouseWheel(mw > 0, Math.abs(mw));
						}
					}
				}
			}
		}
	}

	public void onMouseMoved(Vec2i c)
	{
		if (_dragMoveControl != null)
		{
			_dragMoveControl.setPos(_dragMoveControl.pos.add(c));
		}
	}

	public GUI_Control getMouseInControl()
	{
		GUI_Control ret = null;
		for (GUI_Control ctrl = popup.last_child; ctrl != null; ctrl = ctrl.prev)
		{
			ret = ctrl.getMouseInControl();
			if (ret != null)
			{
				return ret;
			}
		}
		for (GUI_Control ctrl = modal.last_child; ctrl != null; ctrl = ctrl.prev)
		{
			ret = ctrl.getMouseInControl();
			if (ret != null)
			{
				return ret;
			}
		}
		for (GUI_Control ctrl = normal.last_child; ctrl != null; ctrl = ctrl.prev)
		{
			ret = ctrl.getMouseInControl();
			if (ret != null)
			{
				return ret;
			}
		}
		for (GUI_Control ctrl = custom.last_child; ctrl != null; ctrl = ctrl.prev)
		{
			ret = ctrl.getMouseInControl();
			if (ret != null)
			{
				return ret;
			}
		}
		return ret;
	}

	public boolean isMouseInRect(Vec2i c, Vec2i size)
	{
		Vec2i cc = new Vec2i(_mousePos.x, _mousePos.y);
		return cc.in_rect(c, size);
	}

	public void setFocus(GUI_Control ctrl)
	{
		if (ctrl != null && !ctrl.focusable)
		{
			return;
		}

		if (_focusedControl != null)
		{
			_focusedControl.onLostFocus();
		}

		_focusedControl = ctrl;

		if (_focusedControl != null)
		{
			_focusedControl.onGetFocus();
		}
	}

	public void setMouseGrab(GUI_Control ctrl)
	{
		_mouseGrabber = ctrl;
	}

	public void onUnlink(GUI_Control c)
	{
		if (_dragInfo._dragControl == c)
		{
			endDrag(true);
		}
		if (_focusedControl == c)
		{
			_focusedControl = null;
		}
		if (_dragMoveControl == c)
		{
			_dragMoveControl = null;
		}
		if (_mouseGrabber == c)
		{
			_mouseGrabber = null;
		}
		if (_mouseInControl == c)
		{
			_mouseInControl = null;
		}
	}

	public boolean haveDrag()
	{
		return _dragInfo.state != DragInfo.DRAG_STATE_NONE;
	}

	public void beginDrag(GUI_Control parent, GUI_DragControl drag, Vec2i hotspot)
	{
		if (haveDrag() || drag == null)
		{
			return;
		}

		_dragInfo.reset();
		_dragInfo._hotspot = hotspot;
		_dragInfo._dragControl = drag;
		// ставим контрол который создал драг
		drag.drag_parent = parent;

		// перестрахуемся - убъем перемещение контрола
		_dragMoveControl = null;
		// так как мы только начали драг - сразу апдейтим состояние драга
		updateDragState();
	}

	public void endDrag(boolean reset)
	{
		if (!reset)
		{
			if (_dragInfo._dragControl != null)
			{
				if (!_dragInfo._dragControl._terminated)
				{
					_dragInfo._dragControl.endDrag(_dragInfo);
					if (_mouseInControl != null)
					{
						if (!_mouseInControl._terminated)
						{
							_mouseInControl.endDrag(_dragInfo);
						}
					}
				}
			}
		}
		_dragInfo.reset();
	}

	public void updateDragState()
	{
		// если драг контрол жив и будет жить =)
		if (_dragInfo._dragControl != null)
		{
			if (!_dragInfo._dragControl._terminated)
			{
				if (_mouseInControl == null)
				{
					_dragInfo.state = DragInfo.DRAG_STATE_MISS;
				}
				else
				{
					if (_mouseInControl._dragEnabled)
					{
						if (_mouseInControl.doRequestDrop(_dragInfo))
						{
							_dragInfo.state = DragInfo.DRAG_STATE_ACCEPT;
						}
						else
						{
							_dragInfo.state = DragInfo.DRAG_STATE_REFUSE;
						}
					}
					else
					{
						_dragInfo.state = DragInfo.DRAG_STATE_MISS;
					}
				}
				_dragInfo._dragControl.updateDrag(_dragInfo);
				return;
			}
			else
			{
				_dragInfo.state = DragInfo.DRAG_STATE_NONE;
			}
		}
		else
		{
			_dragInfo.state = DragInfo.DRAG_STATE_NONE;
		}
		endDrag(true);
	}

	public void ResolutionChanged()
	{
		Config config = Config.getInstance();
		root.setSize(config.getScreenWidth(), config.getScreenHeight());
		normal.setSize(config.getScreenWidth(), config.getScreenHeight());
		popup.setSize(config.getScreenWidth(), config.getScreenHeight());
		modal.setSize(config.getScreenWidth(), config.getScreenHeight());
		custom.setSize(config.getScreenWidth(), config.getScreenHeight());
	}

}
