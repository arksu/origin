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
import com.a2client.Main;
import com.a2client.Input;
import com.a2client.gui.utils.DragInfo;
import com.a2client.gui.utils.SimpleHint;
import com.a2client.util.Vec2i;


public class GUI
{
    private static GUI _instance = new GUI();

    // game features
    //    public static GUI_Map map;
    public static boolean game_gui_render = true;

    // core
    public GUI_Control root;
    public GUI_Control normal;
    public GUI_Control popup;
    public GUI_Control modal;
    public GUI_Control custom;
    public Vec2i mouse_pos = Vec2i.z;
    public GUI_Control mouse_in_control = null;
    public GUI_Control focused_control = null;
    public GUI_Control mouse_grabber = null;
    public GUI_Control drag_move_control = null;
    public DragInfo drag_info = new DragInfo();
    private boolean active = true;

    private long MbLeftPressTime = MOUSE_DBL_CLICK_TIME + 1;
    private Vec2i MbLeftPressCoord = Vec2i.z;
    private boolean[] mouse_btns = new boolean[3];

    private static final int MOUSE_DBL_CLICK_TIME = 250;
    // отступ от мыши для хинта
    private static final int HINT_OFFSET = 15;

    static public GUI getInstance() { return _instance; }

    static public void reCreate()
    {
        _instance = new GUI();
    }

    public GUI()
    {
        root = new GUI_Control(this);
        root.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        custom = new GUI_Control(root);
        normal = new GUI_Control(root);
        modal = new GUI_Control(root);
        popup = new GUI_Control(root);
        custom.SetSize(root);
        normal.SetSize(root);
        popup.SetSize(root);
        modal.SetSize(root);
    }

    public void setActive(boolean val)
    {
        this.active = val;
    }

    public boolean getActive()
    {
        return this.active;
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

    public void Update()
    {
        if (!active)
            return;

        UpdateMousePos();
        if (!GUI_Debug.active)
            UpdateMouseButtons();
        UpdateMouseWheel();
        UpdateDragState();

        root.Update();
    }

    public void Render()
    {
        if (!active)
            return;
        root.Render();
        RenderHint();
    }

    public void RenderHint()
    {
        if (mouse_in_control == null)
            return;

        int w, h;
        String text = "";

        // получаем размер
        if (mouse_in_control._isSimpleHint)
        {
            text = mouse_in_control.getHint();
            if (text == null || text.length() == 0)
                return;
            Vec2i sz = SimpleHint.getSize(text);
            w = sz.x;
            h = sz.y;
        }
        else
        {
            Vec2i sz = mouse_in_control.getHintSize();
            w = sz.x;
            h = sz.y;
        }
        if (w == 0 || h == 0)
            return;

        int x = mouse_pos.x;
        int y = mouse_pos.y;

        // ищем куда вывести хинт
        if (x + HINT_OFFSET + w > Config.getScreenWidth())
            x = Config.getScreenWidth() - w;
        else
            x += HINT_OFFSET;
        if (y + HINT_OFFSET + h > Config.getScreenHeight())
            y -= (h + 5);
        else
            y += HINT_OFFSET;

        // выводим хинт
        if (mouse_in_control._needHintBg)
            Skin.getInstance().draw("hint", x, y, w, h);
        if (mouse_in_control._isSimpleHint)
        {
            SimpleHint.Render(x, y, w, h, text);
        }
        else
        {
            mouse_in_control.RenderHint(x, y, w, h);
        }
    }

    public boolean isRoot(GUI_Control c)
    {
        return (c == root || c == normal || c == popup || c == modal || c == custom);
    }

    public void HandleKey(char c, int code, boolean down)
    {
        if (!active)
            return;
        boolean r = false;

        if (focused_control != null)
            r = focused_control.DoKey(c, code, down);
        if (r)
            return;

        for (GUI_Control ctrl = root.last_child; ctrl != null; ctrl = ctrl.prev)
        {
            r = ctrl.HandleKey(c, code, down);
            if (r)
                return;
        }
    }

    public void UpdateMousePos()
    {
        Vec2i old_pos = new Vec2i(mouse_pos);
        mouse_pos = new Vec2i(Input.MouseX, Input.MouseY);
        if ((mouse_pos.x - old_pos.x != 0) || (mouse_pos.y - old_pos.y != 0))
            OnMouseMoved(new Vec2i(mouse_pos.x - old_pos.x, mouse_pos.y - old_pos.y));
        if (drag_move_control != null)
            mouse_in_control = drag_move_control;
        else
            mouse_in_control = GetMouseInControl();
    }

    public void UpdateMouseButtons()
    {
        int btn = Input.MB_LEFT;
        MbLeftPressTime += Main.DT;
        boolean[] old_btns = new boolean[3];
        old_btns[0] = mouse_btns[0];
        old_btns[1] = mouse_btns[1];
        old_btns[2] = mouse_btns[2];
        for (int i = 0; i < 3; i++)
        {
            mouse_btns[i] = Input.MouseBtns[i];
            // узнаем на какую кнопку нажали
            if (mouse_btns[i] != old_btns[i])
            {
                switch (i)
                {
                    case Input.MB_LEFT:
                        if (mouse_btns[i])
                            if (MbLeftPressTime < MOUSE_DBL_CLICK_TIME && MbLeftPressCoord.equals(mouse_pos))
                            {
                                btn = Input.MB_DOUBLE;
                                MbLeftPressTime = MOUSE_DBL_CLICK_TIME;
                            }
                            else
                            {
                                btn = Input.MB_LEFT;
                                MbLeftPressTime = 0;
                                MbLeftPressCoord = new Vec2i(mouse_pos);
                            }
                        else
                            btn = Input.MB_LEFT;
                        break;
                    case Input.MB_RIGHT:
                        btn = Input.MB_RIGHT;
                        break;
                    case Input.MB_MIDDLE:
                        btn = Input.MB_MIDDLE;
                        break;
                }

                if (mouse_in_control != null && mouse_in_control != focused_control)
                    if (mouse_btns[i])
                        focused_control = null;

                if (HaveDrag())
                {
                    if (!mouse_btns[i] && btn == Input.MB_LEFT)
                    {
                        EndDrag(false);
                    }
                }
                else
                {
                    if (mouse_grabber != null)
                    {
                        if (!mouse_grabber.DoMouseBtn(btn, mouse_btns[i]))
                            if (!popup.HandleMouseBtn(btn, mouse_btns[i]))
                                if (!modal.HandleMouseBtn(btn, mouse_btns[i]))
                                    if (modal.ChildsCount() == 0)
                                        if (!normal.HandleMouseBtn(btn, mouse_btns[i]))
                                            custom.HandleMouseBtn(btn, mouse_btns[i]);
                    }
                    else if (!popup.HandleMouseBtn(btn, mouse_btns[i]))
                        if (!modal.HandleMouseBtn(btn, mouse_btns[i]))
                            if (modal.ChildsCount() == 0)
                                if (!normal.HandleMouseBtn(btn, mouse_btns[i]))
                                    custom.HandleMouseBtn(btn, mouse_btns[i]);
                }
            }
        }
    }

    public void UpdateMouseWheel()
    {
        int mw = Input.MouseWheel;
        if (mw != 0)
        {
            if (!popup.HandleMouseWheel(mw > 0, Math.abs(mw)))
                if (!modal.HandleMouseWheel(mw > 0, Math.abs(mw)))
                    if (modal.ChildsCount() == 0)
                        if (!normal.HandleMouseWheel(mw > 0, Math.abs(mw)))
                            custom.HandleMouseWheel(mw > 0, Math.abs(mw));
        }
    }

    public void OnMouseMoved(Vec2i c)
    {
        if (drag_move_control != null)
        {
            drag_move_control.SetPos(drag_move_control.pos.add(c));
        }
    }

    public GUI_Control GetMouseInControl()
    {
        GUI_Control ret = null;
        for (GUI_Control ctrl = popup.last_child; ctrl != null; ctrl = ctrl.prev)
        {
            ret = ctrl.GetMouseInControl();
            if (ret != null)
                return ret;
        }
        for (GUI_Control ctrl = modal.last_child; ctrl != null; ctrl = ctrl.prev)
        {
            ret = ctrl.GetMouseInControl();
            if (ret != null)
                return ret;
        }
        for (GUI_Control ctrl = normal.last_child; ctrl != null; ctrl = ctrl.prev)
        {
            ret = ctrl.GetMouseInControl();
            if (ret != null)
                return ret;
        }
        for (GUI_Control ctrl = custom.last_child; ctrl != null; ctrl = ctrl.prev)
        {
            ret = ctrl.GetMouseInControl();
            if (ret != null)
                return ret;
        }
        return ret;
    }

    public boolean MouseInRect(Vec2i c, Vec2i size)
    {
        Vec2i cc = new Vec2i(mouse_pos.x, mouse_pos.y);
        return cc.in_rect(c, size);
    }

    public void SetFocus(GUI_Control ctrl)
    {
        if (ctrl != null && !ctrl.focusable)
            return;

        if (focused_control != null)
            focused_control.DoLostFocus();

        focused_control = ctrl;

        if (focused_control != null)
            focused_control.DoGetFocus();
    }

    public void SetMouseGrab(GUI_Control ctrl)
    {
        mouse_grabber = ctrl;
    }

    public void OnUnlink(GUI_Control c)
    {
        if (drag_info.drag_control == c)
        {
            EndDrag(true);
        }
        if (focused_control == c)
        {
            focused_control = null;
        }
        if (drag_move_control == c)
        {
            drag_move_control = null;
        }
        if (mouse_grabber == c)
        {
            mouse_grabber = null;
        }
        if (mouse_in_control == c)
        {
            mouse_in_control = null;
        }
    }

    public boolean HaveDrag()
    {
        return drag_info.state != DragInfo.DRAG_STATE_NONE;
    }

    public void BeginDrag(GUI_Control parent, GUI_DragControl drag, Vec2i hotspot)
    {
        if (HaveDrag() || drag == null)
            return;

        drag_info.Reset();
        drag_info.hotspot = hotspot;
        drag_info.drag_control = drag;
        // ставим контрол который создал драг
        drag.drag_parent = parent;

        // перестрахуемся - убъем перемещение контрола
        drag_move_control = null;
        // так как мы только начали драг - сразу апдейтим состояние драга
        UpdateDragState();
    }

    public void EndDrag(boolean reset)
    {
        if (!reset)
        {
            if (drag_info.drag_control != null)
                if (!drag_info.drag_control.terminated)
                {
                    drag_info.drag_control.DoEndDrag(drag_info);
                    if (mouse_in_control != null)
                    {
                        if (!mouse_in_control.terminated)
                            mouse_in_control.DoEndDrag(drag_info);
                    }
                }
        }
        drag_info.Reset();
    }

    public void UpdateDragState()
    {
        // если драг контрол жив и будет жить =)
        if (drag_info.drag_control != null)
        {
            if (!drag_info.drag_control.terminated)
            {
                if (mouse_in_control == null)
                {
                    drag_info.state = DragInfo.DRAG_STATE_MISS;
                }
                else
                {
                    if (mouse_in_control.drag_enabled)
                    {
                        if (mouse_in_control.DoRequestDrop(drag_info))
                            drag_info.state = DragInfo.DRAG_STATE_ACCEPT;
                        else
                            drag_info.state = DragInfo.DRAG_STATE_REFUSE;
                    }
                    else
                        drag_info.state = DragInfo.DRAG_STATE_MISS;
                }
                drag_info.drag_control.DoUpdateDrag(drag_info);
                return;
            }
            else
            {
                drag_info.state = DragInfo.DRAG_STATE_NONE;
            }
        }
        else
        {
            drag_info.state = DragInfo.DRAG_STATE_NONE;
        }
        EndDrag(true);
    }

    public void ResolutionChanged()
    {
        root.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        normal.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        popup.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        modal.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
        custom.SetSize(Config.getScreenWidth(), Config.getScreenHeight());
    }

}
