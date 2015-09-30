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

import com.a2client.gui.GUIGDX;

import java.util.ArrayList;
import java.util.List;

public class WordWrap
{
	List<String> Words = new ArrayList<String>();
	String buf, word;
	public List<String> Lines = new ArrayList<String>();
	String FontName = "default";
	int max_len = 0;

	private boolean isDelimiter(char c)
	{
		// пока только пробел - разделяет слова
		if (c == 32)
		{
			return true;
		}

		return false;
	}

	private int GetDelim(String str)
	{
		for (int i = 0; i < str.length(); i++)
		{
			if (isDelimiter(str.charAt(i)))
			{
				return i + 1;
			}
		}
		return -1;
	}

	private void AddWord(int pos)
	{
		Words.add(buf.substring(0, pos));
	}

	private void AddLine(String str)
	{
		if (str.equals(""))
		{
			return;
		}

		Lines.add(str);
	}

	private boolean isTextOut(String str)
	{
		return GUIGDX.getTextWidth(FontName, str) > max_len;
	}

	public WordWrap(String in, int max_len, String font_name)
	{
		this.FontName = font_name;
		this.max_len = max_len;
		this.buf = in;

		Run();
	}

	private void Run()
	{
		int p;
		// бьем строку на слова
		while (buf.length() > 0)
		{
			// ищем разделитель
			p = GetDelim(buf);
			if (p > 0)
			{
				AddWord(p);
				buf = buf.substring(p).trim();
			}
			else
			{
				AddWord(buf.length());
				buf = "";
			}
		}

		// формируем строки
		buf = "";
		for (p = 0; p < Words.size(); p++)
		{
			// проверяем новое слово на длину
			// если слово не влезает
			if (isTextOut(Words.get(p)))
			{
				// надо добавить буфер в новую строку
				AddLine(buf);
				// тут Buf в любом случе уже пустой
				buf = "";

				// и разделить это слово чтобы оно влезло, а остаток запихать в буфер
				word = Words.get(p);

				// проходим все слово посимвольно
				while (word.length() > 0)
				{
					// если достигли предельной длины
					if (isTextOut(buf + word.charAt(0)))
					{
						// добавляем буфер в строку
						AddLine(buf);
						// обнуляем буфер
						buf = "";
					}

					// добавляем в буфер текущий символ
					buf = buf + word.charAt(0);

					// удаляем очередной символ из слова
					word = word.substring(1);
				}
			}
			else
			{
				// проверяем строку и новое слово на длину
				if (isTextOut(buf + " " + Words.get(p)))
				{
					AddLine(buf);
					buf = Words.get(p);
				}
				else
				{
					buf = buf + Words.get(p);
				}
			}
		}

		AddLine(buf);
	}
}
