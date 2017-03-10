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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

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
		return c1 + (c2 << 8);
	}

	public String readAnsiString() throws IOException
	{
		int len = readWord();
		if (len == 0) return "";
		byte[] blob = new byte[len];
		in.read(blob);
		return new String(blob, "US-ASCII");
	}
}
