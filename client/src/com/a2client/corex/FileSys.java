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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileSys
{

    public static MyInputStream getStream(String fname)
    {
        try
        {
            FileInputStream fin = new FileInputStream(Const.DIR_MEDIA + File.separator + fname);
            return new MyInputStream(fin);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static long getSize(String fname)
    {
        return new File(Const.DIR_MEDIA + File.separator + fname).length();
    }
}
