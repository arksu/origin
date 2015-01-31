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

package com.a2client.xml;

public class XMLParam
{
    public String name;
    public String value;

    public XMLParam(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public XMLParam()
    {
        this.name = "";
        this.value = "";
    }

    @Override
    public String toString()
    {
        return "(" + name + "=" + value + ")";
    }
}
