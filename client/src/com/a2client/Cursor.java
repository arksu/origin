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

import com.a2client.util.Utils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Cursor
{
	private static final Logger _log = LoggerFactory.getLogger(Cursor.class.getName());

	static private Cursor _instance;

	private static final String CURSORS_DIR = "assets/cursors/";

	private CursorData[] _cursors;

	static public Cursor getInstance()
	{
		if (_instance == null)
		{
			_instance = new Cursor();
		}
		return _instance;
	}

	private Cursor()
	{
		File file = Gdx.files.internal("assets/cursors/cursors.json").file();
		try
		{
			Gson gson = new Gson();
			_cursors = gson.fromJson(new FileReader(file), CursorData[].class);
		}
		catch (FileNotFoundException e)
		{
			_log.error("cursors config not found", e);
		}
	}

	public void setCursor(String name)
	{
		if (Utils.isEmpty(name)) name = "arrow";

		FileHandle file = Gdx.files.internal(CURSORS_DIR + name + ".png");
		if (!file.file().exists())
		{
			setCursor("");
			return;
		}

		CursorData fd = null;
		for (CursorData data : _cursors)
		{
			if (data.name.equalsIgnoreCase(name))
			{
				fd = data;
			}
		}

		Pixmap pm = new Pixmap(file);
		com.badlogic.gdx.graphics.Cursor cursor = Gdx.graphics.newCursor(
				pm,
				fd != null ? fd.x : 0,
				fd != null ? fd.y : 0);
		Gdx.graphics.setCursor(cursor);
		pm.dispose();
	}

	public void render()
	{

	}

	public class CursorData
	{
		@SerializedName("x")
		int x;

		@SerializedName("y")
		int y;

		@SerializedName("name")
		String name;
	}
}
