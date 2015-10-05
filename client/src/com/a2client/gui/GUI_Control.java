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
    protected Rect ClientRect = new Rect(0, 0, 0, 0);
    // рендерить ли детей. если нет - то рендерим в ручном режиме.
    public boolean render_childs = true;
    // уничтожен ли контрол. елси истина - любое использование контрола не допускается
    // его необходимо исключить из всех обработок и навсегда о нем забыть.
    public boolean terminated = false;
    // простой ли хинт у контрола
    // если простой - выводится тупо текст. берется в getHint()
    // если хинт продвинутый - контрол сам выводит содержимое. Vec2i getHintSize() - должен вернуть размер области под хинт
    // RenderHint(int x, int y) - вывести сам хинт в этих координатах.
    // гуи сам ищет оптимальное расположение хинта, а также выводит подложку если стоит need_hint_bg - иначе контрол должен вывести еще и подложку
    public boolean is_simple_hint = true;
    // текст простого хинта
    public String simple_hint = "";
    // нужно ли вывести подложку под хинт
    public boolean need_hint_bg = true;

    public GUI_Control prev, next, child, last_child, parent;
    public boolean visible = true;
    public boolean focusable = false;
    public boolean enabled = true;
    public boolean is_window = false;
    public boolean drag_enabled = false;

    //--------------------------------------------------------------------------------------------

    public GUI_Control(GUI gui)
    {
        this.gui = gui;
        DoCreate();
    }

    public GUI_Control(GUI_Control parent)
    {
        if (parent == null)
        {
            this.gui = GUI.getInstance();
            this.parent = gui.normal;
            Link();
        }
        else
            synchronized (parent.gui)
            {
                this.gui = parent.gui;
                this.parent = parent;
                Link();
            }
        DoCreate();
    }

    public void Link()
    {
        synchronized (gui)
        {
            if (parent.last_child != null)
                parent.last_child.next = this;
            if (parent.child == null)
                parent.child = this;
            this.prev = parent.last_child;
            parent.last_child = this;
            UpdateAbsPos();
        }
    }

    public void UnlinkChilds()
    {
        while (child != null)
            child.Unlink();
    }

    public void Unlink()
    {
        if (terminated)
            return;

        terminated = true;
        DoDestroy();
        synchronized (gui)
        {
            gui.OnUnlink(this);
            if (next != null)
                next.prev = prev;
            if (prev != null)
                prev.next = next;
            if (parent != null)
            {
                if (parent.child == this)
                    parent.child = next;
                if (parent.last_child == this)
                    parent.last_child = prev;
            }
            next = null;
            prev = null;
        }
    }

    public final void Update()
    {
        if (!gui.game_gui_render && !((this == gui.root) || (this == gui.custom)))
            return;

        if (gui.drag_info.drag_control == this)
        {
            SetX(gui.mouse_pos.x - gui.drag_info.hotspot.x);
            SetY(gui.mouse_pos.y - gui.drag_info.hotspot.y);
        }
        DoUpdate();
        for (GUI_Control c = child; c != null; c = c.next)
        {
            c.Update();
        }
    }

    public int ChildsCount()
    {
        int r = 0;
        for (GUI_Control c = child; c != null; c = c.next)
        {
            r++;
        }
        return r;
    }

    public final void Render()
    {
        // если не рендерим гуй. выходим если это руты
        if (!gui.game_gui_render && !((this == gui.root) || (this == gui.custom)))
            return;

        if (visible)
        {
            DoRender();
            if (render_childs)
                for (GUI_Control c = child; c != null; c = c.next)
                {
                    c.Render();
                }
            DoRenderAfterChilds();
        }
    }

    public void SetPos(Vec2i pos)
    {
        this.pos = new Vec2i(pos);
        for (GUI_Control c = child; c != null; c = c.next)
        {
            c.SetPos(c.pos);
        }
        UpdateAbsPos();
    }

    public void SetPos(int x, int y)
    {
        this.pos = new Vec2i(x, y);
        for (GUI_Control c = child; c != null; c = c.next)
        {
            c.SetPos(c.pos);
        }
        UpdateAbsPos();
    }

    public void SetX(int x)
    {
        this.pos = new Vec2i(x, pos.y);
        for (GUI_Control c = child; c != null; c = c.next)
        {
            c.SetPos(c.pos);
        }
        UpdateAbsPos();
    }

    public void SetY(int y)
    {
        this.pos = new Vec2i(pos.x, y);
        for (GUI_Control c = child; c != null; c = c.next)
        {
            c.SetPos(c.pos);
        }
        UpdateAbsPos();
    }

    public void CenterX()
    {
        if (parent != null)
            SetX((parent.size.x - size.x) / 2);
    }

    public void CenterY()
    {
        if (parent != null)
            SetY((parent.size.y - size.y) / 2);
    }

    public void Center()
    {
        CenterX();
        CenterY();
    }

    public void SetWidth(int val)
    {
        size = new Vec2i(max(val, min_size.x), size.y);
        DoSetSize();
    }

    public void SetHeight(int val)
    {
        size = new Vec2i(size.x, max(val, min_size.y));
        DoSetSize();
    }

    public void SetSize(int w, int h)
    {
        size = new Vec2i(max(w, min_size.x), max(h, min_size.y));
        DoSetSize();
    }

    public void SetSize(GUI_Control c)
    {
        SetSize(c.size.x, c.size.y);
    }

    public void SetSize(Vec2i c)
    {
        SetSize(c.x, c.y);
    }

    public int Height()
    {
        return size.y;
    }

    public int Width()
    {
        return size.x;
    }

    public void UpdateAbsPos()
    {
        abs_pos = new Vec2i(pos);
        GUI_Control ctrl = parent;
        while (ctrl != null)
        {
            abs_pos = abs_pos.add(ctrl.pos);
            ctrl = ctrl.parent;
        }
        DoSetPos();
    }

    public void Show()
    {
        visible = true;
    }

    public void Hide()
    {
        visible = false;
    }

    public boolean ToggleVisible()
    {
        if (visible)
            Hide();
        else
            Show();
        return visible;
    }
    
    public void packSize() 
    {
    	size.x = 0;
    	size.y = 0;
    	
        for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
        {
        	int dx = ctrl.pos.x + ctrl.size.x;
        	int dy = ctrl.pos.y + ctrl.size.y;
            if (size.x < dx)
            	size.x = dx;
            
            if (size.y < dy)
            	size.y = dy;
        }
        size.add(margins);
    }

    public final boolean HandleKey(char c, int code, boolean down)
    {
        boolean r = DoKey(c, code, down);
        if (r)
            return true;
        for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
        {
            r = ctrl.DoKey(c, code, down);
            if (r)
                return true;
        }
        return false;
    }

    public final boolean HandleMouseBtn(int btn, boolean down)
    {
        boolean r = false;
        for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
        {
            if (ctrl.enabled && ctrl.visible)
                if (down)
                {
                    r = ctrl.HandleMouseBtn(btn, down);
                    if (r)
                        break;
                }
                else
                    ctrl.HandleMouseBtn(btn, down);
        }
        if (enabled && visible)
        {
            if (down)
            {
                if (!r)
                    r = DoMouseBtn(btn, down);
            }
            else
                DoMouseBtn(btn, down);

        }
        if (down && r)
        {
            GUI_Control ctrl = parent;
            while (ctrl != null)
            {
                if (ctrl.is_window)
                {
                    ctrl.BringToFront();
                }
                ctrl = ctrl.parent;
            }
        }
        return r;
    }

    public final boolean HandleMouseWheel(boolean isUp, int len)
    {
        boolean r = false;
        for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
        {
            if (ctrl.visible && ctrl.enabled)
                r = ctrl.HandleMouseWheel(isUp, len);
            if (r)
                return true;
        }
        if (enabled && visible)
            r = DoMouseWheel(isUp, len);
        return r;
    }

    public GUI_Control GetMouseInControl()
    {
        GUI_Control ret = null;
        if (enabled)
        {
            for (GUI_Control ctrl = last_child; ctrl != null; ctrl = ctrl.prev)
            {
                if (gui.MouseInRect(ctrl.abs_pos, ctrl.size))
                    if (!ctrl.CheckMouseInControl())
                        continue;
                ret = ctrl.GetMouseInControl();
                if (ret != null)
                    return ret;
            }
            if (CheckMouseInControl() && gui.MouseInRect(abs_pos, size))
                ret = this;
        }
        return ret;
    }

    public void BringToFront()
    {
        if (gui.isRoot(this))
            return;
        if (parent == null)
            return;
        if (parent.last_child == this)
            return;

        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        if (parent.last_child != null)
            parent.last_child.next = this;

        if (parent.child == this)
            parent.child = next;
        prev = parent.last_child;
        next = null;

        parent.last_child = this;
    }

    public void SendToBack()
    {
        if (gui.isRoot(this))
            return;
        if (parent == null)
            return;
        if (parent.child == this)
            return;

        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        if (parent.child != null)
            parent.child.prev = this;

        if (parent.last_child == this)
            parent.last_child = prev;
        next = parent.child;
        prev = null;
        parent.child = this;
    }

    public void BeginDragMove()
    {
        gui.drag_move_control = this;
    }

    public void EndDragMove()
    {
        if (SelfDragged())
        {
            gui.drag_move_control = null;
        }
    }

    protected boolean SelfDragged()
    {
        return gui.drag_move_control == this;
    }

    public Skin getSkin()
    {
        return Skin.getInstance();
    }

    // условие по котороу определяется мышь в контроле
    public boolean CheckMouseInControl()
    {
        return visible;
    }

    public boolean isFocused()
    {
        return gui.focused_control == this;
    }

    public boolean MouseInMe()
    {
        return gui.mouse_in_control == this;
    }

    public boolean MouseInChilds()
    {
        if (MouseInMe())
            return true;
        for (GUI_Control ctrl = child; ctrl != null; ctrl = ctrl.next)
        {
            if (ctrl.MouseInChilds())
                return true;
        }
        return false;
    }

    public boolean isDragOver()
    {
        return (gui.HaveDrag() && MouseInMe());
    }

    // запрос на возможность принять контрол
    public boolean DoRequestDrop(DragInfo info)
    {
        return false;
    }

    // получить текст хинта
    public String getHint()
    {
        return simple_hint;
    }

    // получить размер хинта
    public Vec2i getHintSize()
    {
        return Vec2i.z;
    }

    // вывести хинт
    public void RenderHint(int x, int y, int w, int h)
    {

    }

    // ОБРАБОТЧИКИ СОБЫТИЙ --------------------------------------------------------------------------------------------------------

    // обработчик получения фокуса
    public void DoGetFocus()
    {
    }

    // обработчик потери фокуса
    public void DoLostFocus()
    {
    }

    // обработчик нажатия клавиш
    public boolean DoKey(char c, int code, boolean down)
    {
        return false;
    }

    // обработчик нажатия кнопок мыши
    public boolean DoMouseBtn(int btn, boolean down)
    {
        return false;
    }

    /**
     * обработчик вращения колеса мыши
     *
     * @param isUp вверх?
     * @param len  сколько щелчков
     * @return обработали ли?
     */
    public boolean DoMouseWheel(boolean isUp, int len)
    {
        return false;
    }

    // обработчик апдейта
    public void DoUpdate()
    {
    }

    // обработчик рендера
    public void DoRender()
    {
    }

    // выполняется после рендера себя и всех детей
    public void DoRenderAfterChilds()
    {
    }

    // смена позиции
    public void DoSetPos()
    {
    }

    // смена размера
    public void DoSetSize()
    {
        ClientRect = new Rect(3, 3, size.x - 6, size.y - 6);
    }

    // завершить перетаскивание
    public void DoEndDrag(DragInfo info)
    {
    }

    public void DoUpdateDrag(DragInfo info)
    {
    }

    // уничтожение контрола
    public void DoDestroy()
    {
    }

    // создание контрола
    public void DoCreate()
    {
    }

    public String toString()
    {
        return getClass().getName() + " pos=" + pos.toString() + " size=" + size.toString();
    }

}

