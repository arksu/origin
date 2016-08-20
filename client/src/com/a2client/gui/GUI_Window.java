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
import com.a2client.util.Align;
import com.a2client.util.Rect;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Color;

import static com.a2client.gui.Skin.StateHighlight;
import static com.a2client.gui.Skin.StateNormal;

public class GUI_Window extends GUI_Control
{
    public boolean moveable = true;
    public boolean resizeable = true;
    public String caption = "";
    public Color caption_color = Color.WHITE;
    public int caption_align = Align.Align_Stretch;
    public String font = "default";

    private boolean have_close_button = true;
    private GUI_Button close_btn = null;
    private Vec2i resize_pos_begin;
    private Vec2i resize_size;
    private int resizex;
    private boolean left_resize = false;
    private boolean right_resize = false;

    public static final int CAPTION_HEIGHT = 28;
    private static final int CLOSE_BTN_W = 24;
    private static final int CLOSE_BTN_H = 22;

    public GUI_Window(GUI_Control parent)
    {
        super(parent);
        if (have_close_button)
        {
            close_btn = spawn_close_btn();
            update_close_btn();
        }
        min_size = new Vec2i(70, 35);
    }

    protected GUI_Button spawn_close_btn()
    {
        return new GUI_Button(this)
        {
            public void DoClick()
            {
                ((GUI_Window) parent).close();
            }
        };
    }

    public void close()
    {
        DoClose();
        this.unlink();
    }

    protected void DoClose()
    {
    }

    public boolean onMouseBtn(int btn, boolean down)
    {
        if (!enabled)
            return false;

        if (down && isMouseInMe())
            bringToFront();

        if (btn == Input.MB_LEFT && resizeable)
        {
            if (down)
            {
                if (mouse_in_left_resize())
                {
                    left_resize = true;
                    resize_pos_begin = gui._mousePos;
                    resize_size = size;
                    resizex = pos.x;
                    return true;
                }
                if (mouse_in_right_resize())
                {
                    right_resize = true;
                    resize_pos_begin = gui._mousePos;
                    resize_size = size;
                    resizex = pos.x;
                    return true;
                }
            }
            else
            {
                left_resize = false;
                right_resize = false;
            }
        }

        if (btn == Input.MB_LEFT && moveable)
        {
            if (down)
            {
                if (isMouseInMe() && mouse_in_caption())
                {
                    beginDragMove();
                    return true;
                }
            }
            else
            {
                endDragMove();
                DoMoved();
            }
        }

        return false;
    }

    /**
     * закончено перемещение окошка
     */
    public void DoMoved()
    {}

    public void update()
    {
        //		if (have_close_button && (close_btn == null || close_btn._terminated) ) {
        //			close_btn = spawn_close_btn();
        //		}
        int dx, dy;
        if (left_resize)
        {
            dx = gui._mousePos.x - resize_pos_begin.x;
            dy = gui._mousePos.y - resize_pos_begin.y;
            int w = resize_size.x - dx;
            setX(resizex + resize_size.x - w);
            setSize(w, resize_size.y + dy);
            update_close_btn();
        }
        if (right_resize)
        {
            dx = gui._mousePos.x - resize_pos_begin.x;
            dy = gui._mousePos.y - resize_pos_begin.y;
            setSize(resize_size.x + dx, resize_size.y + dy);
            update_close_btn();
        }
    }

    public void render()
    {
        String e_name;
        int state;
        if (have_close_button)
        {
            e_name = "window_caption_close";
            if (mouse_in_caption())
                state = StateHighlight;
            else
                state = StateNormal;
        }
        else
        {
            e_name = "window_caption";
            if (mouse_in_caption())
                state = StateHighlight;
            else
                state = StateNormal;
        }
        getSkin().draw(e_name, abs_pos.x, abs_pos.y, size.x, size.y, state);

        getSkin().draw("window", abs_pos.x, abs_pos.y + CAPTION_HEIGHT, size.x, size.y - CAPTION_HEIGHT, StateNormal);

        if (resizeable)
        {
            Vec2i sz = getSkin().getElementSize("window_resize_right");
            getSkin().draw("window_resize_right", abs_pos.x + size.x - sz.x, abs_pos.y + size.y - sz.y, sz.x, sz.y,
                    ((mouse_in_right_resize() || right_resize) ? StateHighlight : StateNormal));
            getSkin().draw("window_resize_left", abs_pos.x, abs_pos.y + size.y - sz.y, sz.x, sz.y,
                    ((mouse_in_left_resize() || left_resize) ? StateHighlight : StateNormal));
        }

        if (have_close_button)
            GUIGDX.Text(font, abs_pos.x, abs_pos.y - 2, size.x - CLOSE_BTN_W, CAPTION_HEIGHT, caption_align, caption,
                        caption_color);
        else
            GUIGDX.Text(font, abs_pos.x, abs_pos.y - 2, size.x, CAPTION_HEIGHT, caption_align, caption, caption_color);
    }

    public void onSetSize()
    {
        update_close_btn();
    }

    public void set_close_button(boolean close)
    {
        if (have_close_button != close)
        {
            if (have_close_button && close_btn != null)
            {
                close_btn.unlink();
                close_btn = null;
            }
            if (!have_close_button)
            {
                close_btn = spawn_close_btn();
            }
            have_close_button = close;
        }
        update_close_btn();
    }

    protected boolean mouse_in_caption()
    {
        Rect rect = new Rect(abs_pos, new Vec2i(size.x, CAPTION_HEIGHT));
        return (rect.PointInRect(gui._mousePos) && isMouseInMe());
    }

    protected boolean mouse_in_right_resize()
    {
        Vec2i sz = getSkin().getElementSize("window_resize_right");
        Rect rect = new Rect(abs_pos.x + size.x - sz.x, abs_pos.y + size.y - sz.y, sz);
        return (rect.PointInRect(gui._mousePos) && isMouseInMe());
    }

    protected boolean mouse_in_left_resize()
    {
        Vec2i sz = getSkin().getElementSize("window_resize_left");
        Rect rect = new Rect(abs_pos.x, abs_pos.y + size.y - sz.y, sz);
        return (rect.PointInRect(gui._mousePos) && isMouseInMe());
    }

    protected void update_close_btn()
    {
        if (close_btn == null)
            return;
        close_btn.setPos(size.x - CLOSE_BTN_W - 2, 3);
        close_btn.setSize(CLOSE_BTN_W, CLOSE_BTN_H);
        close_btn.icon_name = "button_close";
        close_btn.render_bg = false;
    }

}
