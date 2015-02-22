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
import java.util.*;

public class Lang
{
    private static final Logger _log = LoggerFactory.getLogger(Lang.class.getName());
    static private Set<Properties> _props = new HashSet<>();

    static public String getTranslate(String key)
    {
        String msg = "";
        for (Properties p : _props)
        {
            msg = p.getProperty(key);
            if (msg != null && !msg.isEmpty())
            {
                return msg;
            }
        }
        if (msg == null || msg.isEmpty())
        {
            msg = key;
        }
        return msg;
    }

    static public void LoadTranslate()
    {
        try
        {
            Properties p = new Properties();
            p.load(Main.class.getResourceAsStream("/translate/login.en_US.properties"));
            _props.add(p);
            p = new Properties();
            p.load(Main.class.getResourceAsStream("/translate/game.en_US.properties"));
            _props.add(p);
        }
        catch (IOException e)
        {
            _log.warn("LoadTranslate error "+e.getMessage());
            e.printStackTrace();
        }
    }
}
