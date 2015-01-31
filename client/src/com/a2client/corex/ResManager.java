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

package com.a2client.corex;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ResManager
{
    static public Map<Const.RES_TYPE, Object> Active = new EnumMap<Const.RES_TYPE, Object>(Const.RES_TYPE.class);
    static private Map<String, ResObject> items = new HashMap<String, ResObject>();

    static public ResObject Get(String name)
    {
        return items.get(name);
    }

    static public void Add(ResObject r)
    {
        items.put(r.name, r);
    }

    static public void Delete(ResObject r)
    {
        items.remove(r);
    }

}
