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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class GUI_Texture extends GUI_Control
{
    private Texture _texture;
    public String skin_element = "";
    public String mode = "spr";

    public GUI_Texture(GUI_Control parent)
    {
        super(parent);
    }

    public void setTexture(Texture t)
    {
        _texture = t;
        mode = "spr";
        SetSize(t.getWidth(), t.getHeight());
    }

    public Texture getTexture()
    {
        return _texture;
    }

    public void DoRender()
    {
        if (mode.equals("spr"))
            GUIGDX.getSpriteBatch()
                  .draw(_texture, (float) abs_pos.x,
                        (float) (Gdx.graphics.getHeight() - abs_pos.y - _texture.getHeight()));
        if (mode.equals("skin_element"))
            getSkin().Draw(skin_element, abs_pos.x, abs_pos.y, size.x, size.y);
    }
}
