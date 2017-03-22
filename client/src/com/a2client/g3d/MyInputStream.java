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

package com.a2client.g3d;

import com.a2client.Config;
import com.a2client.g3d.math.Mat4f;
import com.badlogic.gdx.Gdx;

import java.io.*;

public class MyInputStream extends DataInputStream
{
	/**
	 * Creates a DataInputStream that uses the specified
	 * underlying InputStream.
	 * @param in the specified input stream
	 */
	private MyInputStream(InputStream in)
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

	public Mat4f readBlenderMat4f() throws IOException
	{
		// matrix for convert
		Mat4f ZM = new Mat4f();
		ZM.e00 = -1;
		ZM.e10 = 0;
		ZM.e20 = 0;
		ZM.e30 = 0;
		ZM.e01 = 0;
		ZM.e11 = 0;
		ZM.e21 = 1;
		ZM.e31 = 0;
		ZM.e02 = 0;
		ZM.e12 = 1;
		ZM.e22 = 0;
		ZM.e32 = 0;
		ZM.e03 = 0;
		ZM.e13 = 0;
		ZM.e23 = 0;
		ZM.e33 = 1;

		Mat4f m = new Mat4f();
		m.e00 = readFloat();
		m.e10 = readFloat();
		m.e20 = readFloat();
		m.e30 = readFloat();

		m.e01 = readFloat();
		m.e11 = readFloat();
		m.e21 = readFloat();
		m.e31 = readFloat();

		m.e02 = readFloat();
		m.e12 = readFloat();
		m.e22 = readFloat();
		m.e32 = readFloat();

		m.e03 = readFloat();
		m.e13 = readFloat();
		m.e23 = readFloat();
		m.e33 = readFloat();

		m = ZM.mul(m).mul(ZM.inverse());
//		m = m.transpose();
		return m;
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
			throw new RuntimeException("file not found: " + name);
		}
	}
}
