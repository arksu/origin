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

import com.a2client.util.Rect;
import com.a2client.util.Vec2i;
import com.a2client.util.WordWrap;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class GUI_Memo extends GUI_ScrollPage
{
    List<String> lines = new ArrayList<String>();
    List<MemoLine> render_lines = new ArrayList<MemoLine>();

    public GUI_Memo(GUI_Control parent)
    {
        super(parent);
        skin_element = "memo";
        SetStyle(true, false);
    }

    private class MemoLine
    {
        String text;
        String font;
        Color col;

        public MemoLine(String t, String font, Color col)
        {
            text = t;
            this.font = font;
            this.col = col;
        }
    }

    public void AddLine(String t)
    {
        lines.add(t);

        WordWrap ww = new WordWrap(t, WorkRect().w, "default");
        for (int i = 0; i < ww.Lines.size(); i++)
        {
            render_lines.add(new MemoLine(ww.Lines.get(i), "default", Color.WHITE));
        }

        UpdateFullSize();
    }

    public void UpdateLines()
    {
        render_lines.clear();

        for (String l : lines)
        {
            WordWrap ww = new WordWrap(l, WorkRect().w, "default");
            for (int i = 0; i < ww.Lines.size(); i++)
            {
                render_lines.add(new MemoLine(ww.Lines.get(i), "default", Color.WHITE));
            }
        }
        UpdateFullSize();
    }

    public void DoRender()
    {
        DrawBackground();
        Rect wr = WorkRect();
        Vec2i awr = AbsWorkCoord();
        int ay = awr.y;
        GUIGDX.PushScissor(new Rect(abs_pos.x + ClientRect.x, abs_pos.y + ClientRect.y, wr.w, wr.h));
        for (int i = 0; i < render_lines.size(); i++)
        {
            DrawMemoLine(awr.x, ay, wr.w, wr.h, i);
            ay += GUIGDX.getTextHeight(render_lines.get(i).font, render_lines.get(i).text);
        }
        GUIGDX.PopScissor();
    }

    protected void DrawMemoLine(int ax, int ay, int aw, int ah, int index)
    {
        GUIGDX.Text(render_lines.get(index).font, ax, ay, render_lines.get(index).text, render_lines.get(index).col);
    }

    protected void DrawBackground()
    {
        getSkin().draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
    }

    protected void UpdateFullSize()
    {
        int h = 0;
        for (MemoLine l : render_lines)
        {
            if (FullWidth < GUIGDX.getTextWidth(l.font, l.text))
                SetFullWidth(GUIGDX.getTextWidth(l.font, l.text));
            h += GUIGDX.getTextWidth(l.font, l.text);
        }
        SetFullHeight(h);
    }

    public void ClearLines()
    {
        render_lines.clear();
        UpdateFullSize();
    }

    public void DoSetSize()
    {
        ClientRect = new Rect(3, 3, size.x - 6, size.y - 6);
        UpdateLines();
        super.DoSetSize();
    }

}
