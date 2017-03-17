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

import com.a2client.Config;
import com.badlogic.gdx.Gdx;

import java.io.*;

public class MyInputStream extends DataInputStream
{
	/**
	 * Creates a DataInputStream that uses the specified
	 * underlying InputStream.
	 * @param in the specified input stream
	 */
	public MyInputStream(InputStream in)
	{
		super(in);
	}

	public int readWord() throws IOException
	{
		int c1 = in.read();
		int c2 = in.read();
		return c2 + (c1 << 8);
	}

	public String readAnsiString() throws IOException
	{
		int len = readWord();
		if (len == 0) return "";
		byte[] blob = new byte[len];
		in.read(blob);
		return new String(blob, "US-ASCII");
	}

	public static MyInputStream fromFile(String name)
	{
		try
		{
			File file = Gdx.files.internal(Config.MODELS_DIR + name).file();
			FileInputStream fin = new FileInputStream(file);
			return new MyInputStream(fin);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
