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

public class Console
{
    private static final Logger _log = LoggerFactory.getLogger(Console.class.getName());

    static public boolean ExecuteCommand(String cmd)
    {
        _log.info("exec cmd: " + cmd);

        //        if ("bar".equals(cmd))
        //        {
        //            if (dlg_Hotbars.Exist())
        //                Dialog.Hide("dlg_hotbars");
        //            else
        //                Dialog.Show("dlg_hotbars");
        //            return true;
        //        }
        //
        if ("lang".equals(cmd))
        {
            Lang.LoadFromSite();
            return true;
        }

        return false;
    }

}
