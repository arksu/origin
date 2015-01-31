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

import java.util.ArrayList;
import java.util.List;

public class XMLParams
{
    private List<XMLParam> params = new ArrayList<XMLParam>();
    private int count;

    public String get(String name)
    {
        for (XMLParam param : params)
        {
            if (param.name.equals(name))
                return param.value;
        }
        return "";
    }

    public XMLParam getI(int idx)
    {
        return new XMLParam(params.get(idx).name, params.get(idx).value);
    }

    public int getCount()
    {
        return count;
    }

    private enum FLAG
    {
        F_BEGIN,
        F_NAME,
        F_VALUE
    }

    public XMLParams(String text)
    {
        FLAG flag = FLAG.F_BEGIN;
        int ParamIdx = -1;
        int IndexBegin = 1;
        boolean ReadValue = false;
        boolean TextFlag = false;
        int i;
        char cc;
        for (i = 0; i < text.length(); i++)
        {
            cc = text.charAt(i);
            switch (flag)
            {
                case F_BEGIN:
                    if (cc != ' ')
                    {
                        ParamIdx = params.size();
                        params.add(new XMLParam());
                        flag = FLAG.F_NAME;
                        IndexBegin = i;
                    }
                    break;

                case F_NAME:
                    if (cc == '=')
                    {
                        params.get(ParamIdx).name = XML.trim(text.substring(IndexBegin, i));
                        flag = FLAG.F_VALUE;
                        IndexBegin = i + 1;
                    }
                    break;

                case F_VALUE:
                    if (cc == '"')
                        TextFlag = !TextFlag;
                    if (cc != ' ' && !TextFlag)
                    {
                        ReadValue = true;
                    }
                    else
                    {
                        if (ReadValue)
                        {
                            params.get(ParamIdx).value = XML.trim(text.substring(IndexBegin, i));
                            flag = FLAG.F_BEGIN;
                            ReadValue = false;
                            ParamIdx = -1;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    break;
            }
        }
        if (ParamIdx != -1)
            params.get(ParamIdx).value = XML.trim(text.substring(IndexBegin, text.length()));
        count = params.size();
    }
}
