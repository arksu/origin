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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Lang
{
    private static final Logger _log = LoggerFactory.getLogger(Lang.class.getName());
    static public List<LangItem> langs = new ArrayList<LangItem>();
    private static Lang _instance = new Lang();

    public static class LangItem
    {
        public String full_name;
        public String name;

        public LangItem(String name, String full_name)
        {
            this.full_name = full_name;
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name + ": " + full_name;
        }
    }

    static private Properties _props;

    static
    {
        langs.add(new LangItem("ru", "Russian"));
        langs.add(new LangItem("en", "English"));
        langs.add(new LangItem("ua", "Ukrainian"));
        langs.add(new LangItem("pl", "Polish"));
    }

    static public String getTranslate(String key)
    {
        String msg = _props.getProperty(key);
        if (msg == null)
        {
            msg = key;
        }
        return msg;
    }

    static public void LoadTranslate()
    {
        _props = new Properties();
        try
        {
            _props.load(_instance.getClass().getResourceAsStream("/translate/login.en_US.properties"));
        }
        catch (IOException e)
        {
            _log.warn("failed ");
        }
    }
}
