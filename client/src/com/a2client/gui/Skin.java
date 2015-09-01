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
import com.a2client.util.Align;
import com.a2client.util.Rect;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.a2client.Config.RESOURCE_DIR;
import static com.a2client.util.Align.*;
import static com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

public class Skin
{
    static public final int StateDefault = 0;
    static public final int StateNormal = 1;
    static public final int StateHighlight = 2;
    static public final int StatePressed = 3;
    static public final int StateDisable = 4;
    static public final int StateNormal_Checked = 5;
    static public final int StateHighlight_Checked = 6;
    static public final int StatePressed_Checked = 7;
    static public final int StateDisable_Checked = 8;

    static private Skin _instance;

    private Map<String, SkinElement> elements = new HashMap<String, SkinElement>();

    static public void setInstance(Skin skin)
    {
        _instance = skin;
    }

    static public Skin getInstance()
    {
        return _instance;
    }

    public void Init()
    {

    }

    public boolean hasElement(String name)
    {
        return elements.containsKey(name);
    }

    public void AddElement(String name, SkinElement el)
    {
        elements.put(name, el);
    }

    public void Draw(String element_name, int x, int y, int w, int h, int skin_state, Color col)
    {
        SkinElement el = GetElement(element_name);
        if (el == null)
            return;
        if (w == -1)
            w = el.width;
        if (h == -1)
            h = el.height;
        el.Draw(x, y, w, h, skin_state, col);
    }

    public void Draw(String element_name, int x, int y, int w, int h, int skin_state)
    {
        Draw(element_name, x, y, w, h, skin_state, Color.WHITE);
    }

    public void Draw(String element_name, int x, int y, int w, int h)
    {
        Draw(element_name, x, y, w, h, StateDefault, Color.WHITE);
    }

    public void Draw(String element_name, Vec2i c, Vec2i sz)
    {
        Draw(element_name, c.x, c.y, sz.x, sz.y, StateDefault, Color.WHITE);
    }


    public void Draw(String element_name, int x, int y)
    {
        Draw(element_name, x, y, -1, -1, StateDefault, Color.WHITE);
    }

    public void Draw(String element_name, Vec2i c)
    {
        Draw(element_name, c.x, c.y, -1, -1, StateDefault, Color.WHITE);
    }

    public Vec2i GetElementSize(String element_name)
    {
        SkinElement el = GetElement(element_name);
        if (el == null)
            return Vec2i.z;
        return new Vec2i(el.width, el.height);
    }

    // PRIVATE
    // ==============================================================================
    private SkinElement GetElement(String element_name)
    {
        return elements.get(element_name);
    }

    // CLASSES
    // ==============================================================================

    protected class SkinSprite
    {
        String _tex_name = "";
        Texture _tex = null;
        AtlasRegion _reg = null;

        protected SkinSprite(String tex)
        {
            _tex_name = tex;
            _tex = Main.getAssetManager().get(RESOURCE_DIR + _tex_name + ".png");
        }

        protected SkinSprite(AtlasRegion reg)
        {
            _reg = reg;
        }

        protected void draw(int x, int y, int w, int h, int tx, int ty, int tw, int th, Color color)
        {
            if (_tex != null)
            {
                GUIGDX.getSpriteBatch().setColor(color);
                GUIGDX.getSpriteBatch()
                        // переворачиваем по y
                        .draw(_tex, (float) x, (float) (Config.getScreenHeight() - y), (float) w, (float) (-h), tx, ty,
                              tw, th, false, true);
            }
            else
            {
                GUIGDX.getSpriteBatch().setColor(color);
                GUIGDX.getSpriteBatch()
                      .draw(_reg, (float) x, (float) (Config.getScreenHeight() - y - h), (float) w, (float) (h));

            }
        }
    }

    // элемент скина. базовый объект для взаимодействия с контролами
    // умеет выводить графику с тайлами или без с любым выравниванием
    // разбивается на кусочки SkinSubElement
    public class SkinElement
    {
        public int width;
        public int height;
        String texture_name = "";
        AtlasRegion region = null;
        SkinSprite spr = null;
        Rect offset;
        // последние размеры вывода элемнта на экран
        int CurrentSizeH;
        int CurrentSizeW;
        // все кусочки из которых состоит элемент
        List<SkinSubElement> childs = new ArrayList<SkinSubElement>();

        public SkinElement(Skin skin, String name, String tex_name, int w, int h, Rect offset)
        {
            skin.AddElement(name, this);
            this.texture_name = tex_name;
            this.width = w;
            this.height = h;
            this.offset = new Rect(offset);
            CurrentSizeH = 0;
            CurrentSizeW = 0;
        }

        public SkinElement(Skin skin, String name, AtlasRegion region, int w, int h, Rect offset)
        {
            skin.AddElement(name, this);
            this.region = region;
            this.width = w;
            this.height = h;
            this.offset = new Rect(offset);
            CurrentSizeH = 0;
            CurrentSizeW = 0;
        }

        public SkinElement(Skin skin, String name, String tex_name, int w, int h)
        {
            skin.AddElement(name, this);
            this.texture_name = tex_name;
            this.width = w;
            this.height = h;
            this.offset = new Rect(0, 0, 0, 0);
            CurrentSizeH = 0;
            CurrentSizeW = 0;
        }

        public void Draw(int x, int y, int w, int h, int skin_state, Color col)
        {
            if (spr == null)
            {
                if (!texture_name.isEmpty())
                {
                    spr = new SkinSprite(texture_name);
                }
                else
                {
                    spr = new SkinSprite(region);
                }
            }
            if (CurrentSizeH != h || CurrentSizeW != w)
                UpdateSize(w, h);
            SubDraw(x - offset.x, y - offset.y, skin_state, col);
        }

        protected void UpdateSize(int w, int h)
        {
            CurrentSizeH = h;
            CurrentSizeW = w;
            for (SkinSubElement s : childs)
            {
                s.UpdateSize(new Vec2i(width, height), new Vec2i(w + offset.w, h + offset.h));
            }
        }

        protected void SubDraw(int x, int y, int skin_state, Color col)
        {
            if (spr == null)
                return;

            for (SkinSubElement s : childs)
            {
                s.Draw(x, y, spr, skin_state, col);
            }
        }

        public void AddSubElement(SkinSubElement el)
        {
            childs.add(el);
        }
    }

    // минимальный кусочек вывода графики. выводит часть текстуры на экран с заданным выравниванием
    // тайлом или стретчем
    class SkinSubElement
    {
        // область внутри элемента для вывода
        public Rect Offset;
        // координаты последнего вывода кусочка (нужно для кеширования)
        public Rect mCoord;
        // выравнивание субэлемента (как и куда выводится кусочек)
        public byte align;
        // состояния
        public List<SkinSubElementState> states = new ArrayList<SkinSubElementState>();
        // выводим ли тайлом
        public boolean TileH;
        public boolean TileV;

        public void AddState(int state, Rect texture_rect, Color col)
        {
            states.add(new SkinSubElementState(state, texture_rect, col));
        }

        public void AddState(int state, Rect texture_rect)
        {
            states.add(new SkinSubElementState(state, texture_rect, Color.WHITE));
        }

        public SkinSubElement(SkinElement el, Rect offset, int align, boolean TileH, boolean TileV)
        {
            this.Offset = new Rect(offset);
            this.align = (byte) align;
            this.TileH = TileH;
            this.TileV = TileV;
            el.AddSubElement(this);
        }

        public SkinSubElement(SkinElement el, Rect offset, int align)
        {
            this.Offset = new Rect(offset);
            this.align = (byte) align;
            this.TileH = false;
            this.TileV = false;
            el.AddSubElement(this);
        }

        // обновить координаты на экране с учетом привязок
        public void UpdateSize(Vec2i orig, Vec2i newc)
        {
            mCoord = new Rect(Offset);

            // ===== ГОРИЗОНТАЛЬНОЕ выравнивание =====
            // проверяем должен ли вообще рисоватся элемент?
            if (mCoord.x > newc.x)
            {
                mCoord.w = 0;
            }
            else if (Align.isHStretch(align))
            {
                // растягиваем
                if (newc.x >= orig.x)
                    mCoord.w = mCoord.w + newc.x - orig.x;
                else
                {
                    if (newc.x < mCoord.x + mCoord.w)
                        mCoord.w = newc.x - mCoord.x;
                }
            }
            else if (Align.isRight(align))
            {
                // двигаем по правому краю
                if (newc.x >= orig.x)
                    mCoord.x = mCoord.x + newc.x - orig.x;
                else
                {
                    if (newc.x < mCoord.x + mCoord.w)
                        mCoord.w = newc.x - mCoord.x;
                }
            }
            else if (Align.isHCenter(align))
            {
                // выравнивание по горизонтали без растяжения
                if (newc.x >= mCoord.w)
                    mCoord.x = (newc.x - mCoord.w) / 2;
                else
                {
                    mCoord.x = 0;//(newc.x) / 2;
                    mCoord.w = newc.x;
                }
            }
            else
            {
                if (newc.x < mCoord.x + mCoord.w)
                    mCoord.w = newc.x - mCoord.x;
            }

            //------------------------------------------------------------------------------

            // ===== ВЕРТИКАЛЬНОЕ выравнивание =====
            // проверяем должен ли вообще рисоватся элемент?
            if (mCoord.y > newc.y)
            {
                mCoord.h = 0;
            }
            else if (Align.isVStretch(align))
            {
                // растягиваем
                if (newc.y >= orig.y)
                    mCoord.h = mCoord.h + newc.y - orig.y;
                else
                {
                    if (newc.y < mCoord.y + mCoord.h)
                        mCoord.h = newc.y - mCoord.y;
                }
            }
            else if (Align.isBottom(align))
            {
                // двигаем по нижнему краю
                if (newc.y >= orig.y)
                    mCoord.y = mCoord.y + newc.y - orig.y;
                else
                {
                    if (newc.y < mCoord.y + mCoord.h)
                        mCoord.h = newc.y - mCoord.y;
                }
            }
            else if (Align.isVCenter(align))
            {
                // выравнивание по вертикали без растяжения
                if (newc.y >= mCoord.h)
                    mCoord.y = (newc.y - mCoord.h) / 2;
                else
                {
                    mCoord.y = 0;//(newc.y) / 2;
                    mCoord.h = newc.y;
                }
            }
            else
            {
                if (newc.y < mCoord.y + mCoord.h)
                    mCoord.h = newc.y - mCoord.y;
            }
        }

        public void Draw(int x, int y, SkinSprite spr, int state, Color col)
        {
            if (mCoord == null) return;
            // если хотя бы один из размеров нулевой - выходим
            if ((mCoord.w == 0) || (mCoord.h == 0))
                return;

            for (SkinSubElementState s : states)
            {
                if (s.state == state)
                {
                    DrawState(s, x, y, spr, col);
                    return;
                }
            }

            for (SkinSubElementState s : states)
            {
                if (s.state == StateDefault)
                {
                    DrawState(s, x, y, spr, col);
                    return;
                }
            }

            if (state == StateDefault)
            {
                for (SkinSubElementState s : states)
                {
                    DrawState(s, x, y, spr, col);
                    return;
                }
            }
        }

        private void DrawState(SkinSubElementState state, int x, int y, SkinSprite spr, Color color)
        {
            int cx, cy, cw, ch;
            Color col;
            // итераторы координат
            cx = 0;
            cy = 0;
            // размеры тайла
            cw = state.texture_rect.w;
            ch = state.texture_rect.h;

            if (!color.equals(Color.WHITE))
                col = color;
            else
                col = state.col;

            // тайлим если нужно
            if (TileH)
            {
                while (cx < mCoord.w)
                {
                    if (cx + cw > mCoord.w)
                        cw = mCoord.w - cx;
                    if (TileV)
                    {
                        cy = 0;
                        ch = state.texture_rect.h;
                        while (cy < mCoord.h)
                        {
                            if (cy + ch > mCoord.h)
                                ch = mCoord.h - cy;
                            spr.draw(x + mCoord.x + cx, y + mCoord.y + cy, cw, ch, state.texture_rect.x,
                                     state.texture_rect.y, cw, ch, col);
                            cy += ch;
                        }
                    }
                    else
                    {
                        spr.draw(x + mCoord.x + cx, y + mCoord.y + cy, cw, mCoord.h, state.texture_rect.x,
                                 state.texture_rect.y, cw, ch, col);
                    }
                    cx += cw;
                }
            }
            else
            {
                if (TileV)
                    while (cy < mCoord.h)
                    {
                        if (cy + ch > mCoord.h)
                            ch = mCoord.h - cy;
                        spr.draw(x + mCoord.x + cx, y + mCoord.y + cy, mCoord.w, ch, state.texture_rect.x,
                                 state.texture_rect.y, cw, ch, col);
                        cy += ch;
                    }
                else
                    // если никак не тайлим - выводим текстуру стретчем
                    spr.draw(x + mCoord.x, y + mCoord.y, mCoord.w, mCoord.h, state.texture_rect.x, state.texture_rect.y,
                             state.texture_rect.w, state.texture_rect.h, col);
            }
        }
    }

    protected void AddStatesCenter(SkinElement el, int h, int w, int x, int x1, int x2, int x3, int y, int y1, int y2, int y3)
    {
        SkinSubElement sub;
        sub = new SkinSubElement(el, new Rect(0, 0, w, h), Align_Center);
        if (x != -1 && y != -1)
            sub.AddState(StateNormal, new Rect(x, y, w, h));
        if (x1 != -1 && y1 != -1)
            sub.AddState(StateHighlight, new Rect(x1, y1, w, h));
        if (x2 != -1 && y2 != -1)
            sub.AddState(StatePressed, new Rect(x2, y2, w, h));
        if (x3 != -1 && y3 != -1)
            sub.AddState(StateDisable, new Rect(x3, y3, w, h));
    }

    protected void AddStatesCenter(SkinElement el, int h, int w, int x, int y)
    {
        SkinSubElement sub;
        sub = new SkinSubElement(el, new Rect(0, 0, w, h), Align_Center);
        if (x != -1 && y != -1)
            sub.AddState(StateNormal, new Rect(x, y, w, h));
    }

    protected void AddStatesStretch(SkinElement el, int h, int w, int x, int y)
    {
        SkinSubElement sub;
        sub = new SkinSubElement(el, new Rect(0, 0, w, h), Align_Stretch);
        if (x != -1 && y != -1)
            sub.AddState(StateNormal, new Rect(x, y, w, h));
    }

    protected void AddStates(SkinElement el, int w1, int w2, int w3, int h1, int h2, int h3, int x, int x1, int x2, int x3, int y, int y1, int y2, int y3)
    {
        AddStates(el, w1, w2, w3, h1, h2, h3, 0, 0, 0, 0, x, y, x1, y1, x2, y2, x3, y3, -1, -1, -1, -1, -1, -1, -1, -1);
    }

    protected void AddStates(SkinElement el, int w1, int w2, int w3, int h1, int h2, int h3, int wi1, int wi2, int hi1, int hi2, int x, int x1, int x2, int x3, int y, int y1, int y2, int y3)
    {
        AddStates(el, w1, w2, w3, h1, h2, h3, wi1, wi2, hi1, hi2, x, y, x1, y1, x2, y2, x3, y3, -1, -1, -1, -1, -1, -1,
                  -1, -1);
    }

    protected void AddStates(SkinElement el, int w1, int w2, int w3, int h1, int h2, int h3, int w_insert1, int w_insert2, // промежутки между полосами нарезки
                             int h_insert1, int h_insert2, int x, int y,    // normal
                             int x1, int y1, // hl
                             int x2, int y2, // pressed
                             int x3, int y3, // disable
                             int x4, int y4, // check_normal
                             int x5, int y5, // check_hl
                             int x6, int y6, // check_pressel
                             int x7, int y7)
    { // check_disable
        SkinSubElement sub;
        if (h1 > 0)
        {
            if (w1 > 0)
            {
                sub = new SkinSubElement(el, new Rect(0, 0, w1, h1), Align_Left + Align_Top);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal, new Rect(x, y, w1, h1));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight, new Rect(x1, y1, w1, h1));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed, new Rect(x2, y2, w1, h1));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable, new Rect(x3, y3, w1, h1));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked, new Rect(x4, y4, w1, h1));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked, new Rect(x5, y5, w1, h1));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked, new Rect(x6, y6, w1, h1));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked, new Rect(x7, y7, w1, h1));
            }

            if (w2 > 0)
            {
                sub = new SkinSubElement(el, new Rect(w1, 0, w2, h1), Align_HStretch + Align_Top);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal, new Rect(x + w1 + w_insert1, y, w2, h1));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight, new Rect(x1 + w1 + w_insert1, y1, w2, h1));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed, new Rect(x2 + w1 + w_insert1, y2, w2, h1));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable, new Rect(x3 + w1 + w_insert1, y3, w2, h1));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked, new Rect(x4 + w1 + w_insert1, y4, w2, h1));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked, new Rect(x5 + w1 + w_insert1, y5, w2, h1));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked, new Rect(x6 + w1 + w_insert1, y6, w2, h1));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked, new Rect(x7 + w1 + w_insert1, y7, w2, h1));
            }

            if (w3 > 0)
            {
                sub = new SkinSubElement(el, new Rect(w1 + w2, 0, w3, h1), Align_Right + Align_Top);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal, new Rect(x + w1 + w2 + w_insert1 + w_insert2, y, w3, h1));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight, new Rect(x1 + w1 + w2 + w_insert1 + w_insert2, y1, w3, h1));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed, new Rect(x2 + w1 + w2 + w_insert1 + w_insert2, y2, w3, h1));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable, new Rect(x3 + w1 + w2 + w_insert1 + w_insert2, y3, w3, h1));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked, new Rect(x4 + w1 + w2 + w_insert1 + w_insert2, y4, w3, h1));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked, new Rect(x5 + w1 + w2 + w_insert1 + w_insert2, y5, w3, h1));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked, new Rect(x6 + w1 + w2 + w_insert1 + w_insert2, y6, w3, h1));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked, new Rect(x7 + w1 + w2 + w_insert1 + w_insert2, y7, w3, h1));
            }
        }
        //------------------------------------------------------------------------------------
        if (h2 > 0)
        {
            if (w1 > 0)
            {
                sub = new SkinSubElement(el, new Rect(0, h1, w1, h2), Align_Left + Align_VStretch);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal, new Rect(x, y + h1 + h_insert1, w1, h2));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight, new Rect(x1, y1 + h1 + h_insert1, w1, h2));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed, new Rect(x2, y2 + h1 + h_insert1, w1, h2));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable, new Rect(x3, y3 + h1 + h_insert1, w1, h2));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked, new Rect(x4, y4 + h1 + h_insert1, w1, h2));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked, new Rect(x5, y5 + h1 + h_insert1, w1, h2));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked, new Rect(x6, y6 + h1 + h_insert1, w1, h2));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked, new Rect(x7, y7 + h1 + h_insert1, w1, h2));
            }

            if (w2 > 0)
            {
                sub = new SkinSubElement(el, new Rect(w1, h1, w2, h2), Align_HStretch + Align_VStretch);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal, new Rect(x + w1 + w_insert1, y + h1 + h_insert1, w2, h2));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight, new Rect(x1 + w1 + w_insert1, y1 + h1 + h_insert1, w2, h2));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed, new Rect(x2 + w1 + w_insert1, y2 + h1 + h_insert1, w2, h2));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable, new Rect(x3 + w1 + w_insert1, y3 + h1 + h_insert1, w2, h2));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked, new Rect(x4 + w1 + w_insert1, y4 + h1 + h_insert1, w2, h2));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked, new Rect(x5 + w1 + w_insert1, y5 + h1 + h_insert1, w2, h2));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked, new Rect(x6 + w1 + w_insert1, y6 + h1 + h_insert1, w2, h2));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked, new Rect(x7 + w1 + w_insert1, y7 + h1 + h_insert1, w2, h2));
            }

            if (w3 > 0)
            {
                sub = new SkinSubElement(el, new Rect(w1 + w2, h1, w3, h2), Align_Right + Align_VStretch);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal,
                                 new Rect(x + w1 + w2 + w_insert1 + w_insert2, y + h1 + h_insert1, w3, h2));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight,
                                 new Rect(x1 + w1 + w2 + w_insert1 + w_insert2, y1 + h1 + h_insert1, w3, h2));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed,
                                 new Rect(x2 + w1 + w2 + w_insert1 + w_insert2, y2 + h1 + h_insert1, w3, h2));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable,
                                 new Rect(x3 + w1 + w2 + w_insert1 + w_insert2, y3 + h1 + h_insert1, w3, h2));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked,
                                 new Rect(x4 + w1 + w2 + w_insert1 + w_insert2, y4 + h1 + h_insert1, w3, h2));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked,
                                 new Rect(x5 + w1 + w2 + w_insert1 + w_insert2, y5 + h1 + h_insert1, w3, h2));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked,
                                 new Rect(x6 + w1 + w2 + w_insert1 + w_insert2, y6 + h1 + h_insert1, w3, h2));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked,
                                 new Rect(x7 + w1 + w2 + w_insert1 + w_insert2, y7 + h1 + h_insert1, w3, h2));
            }
        }
        //------------------------------------------------------------------------------------
        if (h3 > 0)
        {
            if (w1 > 0)
            {
                sub = new SkinSubElement(el, new Rect(0, h1 + h2, w1, h3), Align_Left + Align_Bottom);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal, new Rect(x, y + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight, new Rect(x1, y1 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed, new Rect(x2, y2 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable, new Rect(x3, y3 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked, new Rect(x4, y4 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked, new Rect(x5, y5 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked, new Rect(x6, y6 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked, new Rect(x7, y7 + h1 + h2 + h_insert1 + h_insert2, w1, h3));
            }
            if (w2 > 0)
            {
                sub = new SkinSubElement(el, new Rect(w1, h1 + h2, w2, h3), Align_HStretch + Align_Bottom);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal,
                                 new Rect(x + w1 + w_insert1, y + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight,
                                 new Rect(x1 + w1 + w_insert1, y1 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed,
                                 new Rect(x2 + w1 + w_insert1, y2 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable,
                                 new Rect(x3 + w1 + w_insert1, y3 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked,
                                 new Rect(x4 + w1 + w_insert1, y4 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked,
                                 new Rect(x5 + w1 + w_insert1, y5 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked,
                                 new Rect(x6 + w1 + w_insert1, y6 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked,
                                 new Rect(x7 + w1 + w_insert1, y7 + h1 + h2 + h_insert1 + h_insert2, w2, h3));
            }
            if (w3 > 0)
            {
                sub = new SkinSubElement(el, new Rect(w1 + w2, h1 + h2, w3, h3), Align_Right + Align_Bottom);
                if (x != -1 && y != -1)
                    sub.AddState(StateNormal,
                                 new Rect(x + w1 + w2 + w_insert1 + w_insert2, y + h1 + h2 + h_insert1 + h_insert2, w3,
                                          h3));
                if (x1 != -1 && y1 != -1)
                    sub.AddState(StateHighlight,
                                 new Rect(x1 + w1 + w2 + w_insert1 + w_insert2, y1 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
                if (x2 != -1 && y2 != -1)
                    sub.AddState(StatePressed,
                                 new Rect(x2 + w1 + w2 + w_insert1 + w_insert2, y2 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
                if (x3 != -1 && y3 != -1)
                    sub.AddState(StateDisable,
                                 new Rect(x3 + w1 + w2 + w_insert1 + w_insert2, y3 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
                if (x4 != -1 && y4 != -1)
                    sub.AddState(StateNormal_Checked,
                                 new Rect(x4 + w1 + w2 + w_insert1 + w_insert2, y4 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
                if (x5 != -1 && y5 != -1)
                    sub.AddState(StateHighlight_Checked,
                                 new Rect(x5 + w1 + w2 + w_insert1 + w_insert2, y5 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
                if (x6 != -1 && y6 != -1)
                    sub.AddState(StatePressed_Checked,
                                 new Rect(x6 + w1 + w2 + w_insert1 + w_insert2, y6 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
                if (x7 != -1 && y7 != -1)
                    sub.AddState(StateDisable_Checked,
                                 new Rect(x7 + w1 + w2 + w_insert1 + w_insert2, y7 + h1 + h2 + h_insert1 + h_insert2,
                                          w3, h3));
            }
        }
    }

    private class SkinSubElementState
    {
        int state;
        Rect texture_rect;
        Color col;

        public SkinSubElementState(int state, Rect texture_rect, Color col)
        {
            this.state = state;
            this.texture_rect = texture_rect;
            this.col = col;
        }
    }

    public void ParseIcons()
    {
        TextureAtlas atlas = Main.getAssetManager().get(RESOURCE_DIR + "icons.pack", TextureAtlas.class);

        Array<AtlasRegion> regions = atlas.getRegions();

        for (AtlasRegion r : regions)
        {
            SkinElement el;
            // вместо имени текстуры - имя региона, по которому и будем создавать спрайт из атласа
            el = new SkinElement(this, r.name, r, r.originalWidth, r.originalHeight, new Rect(0, 0, 0, 0));
            AddStatesCenter(el, r.packedHeight, r.packedWidth, 0, 0);
        }
    }

}
