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
import com.badlogic.gdx.graphics.Color;

import static com.a2client.gui.Skin.*;
import static com.badlogic.gdx.Input.Keys;

/**
 * поле ввода для целочисленных значений, поддержка колеса мыши
 */
public class GUI_SpinEdit extends GUI_Control
{

    public int max, min, value;
    public String font = "default";
    public Color text_color = Color.WHITE;
    public int step = 1;

    GUI_Button btn_inc, btn_dec;
    boolean pressed = false;

    public GUI_SpinEdit(GUI_Control parent)
    {
        super(parent);
        btn_dec = new GUI_Button(this)
        {
            public void doClick()
            {
                DoDec();
                gui.setFocus(parent);
            }

            public boolean onMouseWheel(boolean isUp, int len)
            {
                return parent.onMouseWheel(isUp, len);
            }
        };
        btn_inc = new GUI_Button(this)
        {
            public void doClick()
            {
                DoInc();
                gui.setFocus(parent);
            }

            public boolean onMouseWheel(boolean isUp, int len)
            {
                return parent.onMouseWheel(isUp, len);
            }
        };
        btn_dec.skin_element = "button_left";
        btn_dec.setSize(getSkin().getElementSize("button_left"));
        btn_inc.skin_element = "button_right";
        btn_inc.setSize(getSkin().getElementSize("button_right"));
        skin_element = "edit";
        focusable = true;

        max = 100;
        min = 0;
        value = 0;
    }

    public void DoInc()
    {
        if (value < max)
        {
            value += step;
            if (value > max)
                value = max;
            DoChanged();
        }
    }

    public void DoDec()
    {
        if (value > min)
        {
            value -= step;
            if (value < min)
                value = min;
            DoChanged();
        }
    }

    public void DoInc2()
    {
        if (value < max)
        {
            value += step * 10;
            if (value > max)
                value = max;
            DoChanged();
        }
    }

    public void DoDec2()
    {
        if (value > min)
        {
            value -= step * 10;
            if (value < min)
                value = min;
            DoChanged();
        }
    }

    public void DoChanged() { }

    public void onSetSize()
    {
        btn_dec.setPos(0, (size.y - btn_dec.getHeight()) / 2);
        btn_inc.setPos(size.x - btn_inc.getWidth(), (size.y - btn_inc.getHeight()) / 2);
    }

    public void render()
    {
        int state;
        if (!enabled)
            state = StateDisable;
        else
        {
            if (isFocused())
                state = StateNormal_Checked;
            else
            {
                if (isMouseInMe())
                {
                    if (pressed)
                        state = StatePressed;
                    else
                        state = StateHighlight;
                }
                else
                    state = StateNormal;
            }
        }
        getSkin().draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y, state);
        GUIGDX.Text(font, abs_pos.x, abs_pos.y, size.x, size.y, Align.Align_Center, String.valueOf(value), text_color);
    }

    public boolean onMouseWheel(boolean isUp, int len)
    {
        if (!isMouseInMe() && !btn_dec.isMouseInMe() && !btn_inc.isMouseInMe())
            return false;

        if (isUp)
            DoInc();
        else
            DoDec();
        return true;
    }

    public boolean onMouseBtn(int btn, boolean down)
    {
        if (!enabled)
            return false;

        if (btn == Input.MB_LEFT)
            if (down)
            {
                if (isMouseInMe())
                {
                    gui.setFocus(this);

                    pressed = true;
                    return true;
                }
            }
            else
            {
                if (pressed && isMouseInMe())
                {
                    DoClick();
                    return true;
                }
                pressed = false;
            }
        return false;
    }

    public boolean onKey(char c, int key, boolean down)
    {
        if (isFocused())
        {
            if (down)
            {
                if (key == Keys.UP || key == Keys.RIGHT)
                    DoInc();
                else if (key == Keys.DOWN || key == Keys.LEFT)
                    DoDec();
                else if (key == Keys.PAGE_UP)
                    DoInc2();
                else if (key == Keys.PAGE_DOWN)
                    DoDec2();
            }
            return true;
        }
        else
            return false;
    }

    public void DoClick()
    {

    }
}
