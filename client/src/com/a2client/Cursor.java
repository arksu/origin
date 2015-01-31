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

package com.a2client;

public class Cursor
{
    static private Cursor _instance;

    static public Cursor getInstance()
    {
        if (_instance == null)
            _instance = new Cursor();
        return _instance;
    }

    // handle network
    public void setCursor(String name)
    {
        if (name.equals(""))
            name = "arrow";
        // TODO : установить курсор
        //        ResCursor c = ResourceManager.getInstance().(name);
        //        if (c != null)
        //        {
        //            ByteArrayInputStream in = new ByteArrayInputStream(c.png_data);
        //            CursorLoader.SetCursor(in, c.offx, c.offy);
        //        }
    }

    public void render()
    {

    }
}
