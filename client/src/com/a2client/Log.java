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

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Log
{

    private static final Logger _log = Logger.getLogger(Main.class);

    public static void init()
    {
        URL u = Main.class.getResource("/client-log4j.xml");
        if (u == null)
        {
            File f = new File("client-log4j.xml");
            try
            {
                u = f.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }

        }

        DOMConfigurator.configure(u);
    }

    public static void info(String msg)
    {
        _log.info(msg);
        //		org.newdawn.slick.util.Log.info(msg);
    }


    public static void debug(String msg)
    {
        if (!Config.debug)
            return;
        _log.debug(msg);
        //		org.newdawn.slick.util.Log.info(msg);
    }


    public static void error(Object msg)
    {
        _log.error(msg);
    }
}
