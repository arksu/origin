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

package com.a2client.util;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class CursorLoader
{

    static public void SetCursor(InputStream in, int x, int y)
    {
        try
        {
            Cursor c = getCursorFromPNG(in, x, y);
            Mouse.setNativeCursor(c);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static public void SetCursor(String file_name, int x, int y)
    {
        FileInputStream in;
        try
        {
            in = new FileInputStream(file_name);
            SetCursor(in, x, y);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    static public Cursor getCursorFromPNG(InputStream in, int x, int y) throws IOException, LWJGLException
    {
        /*
        LoadableImageData imageData = null;

        imageData = new CompositeImageData();
        ((CompositeImageData) imageData).add(new PNGImageData());

        imageData.configureEdging(false);

        ByteBuffer buf = imageData.loadImage(in, true, true, null);
        for (int i = 0; i < buf.limit(); i += 4)
        {
            byte red = buf.get(i);
            byte green = buf.get(i + 1);
            byte blue = buf.get(i + 2);
            byte alpha = buf.get(i + 3);

            buf.put(i + 2, red);
            buf.put(i + 1, green);
            buf.put(i, blue);
            buf.put(i + 3, alpha);
        }

        try
        {
            int yspot = imageData.getHeight() - y - 1;
            if (yspot < 0)
            {
                yspot = 0;
            }

            return new Cursor(imageData.getTexWidth(), imageData.getTexHeight(), x, yspot, 1, buf.asIntBuffer(), null);
        }
        catch (Throwable e)
        {
            throw new LWJGLException(e);
        }
        */
        return null;
    }
}
