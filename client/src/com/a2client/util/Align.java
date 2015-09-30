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

public class Align
{

	/**
	 * ALign consts
	 */
	public static final int Align_HCenter = 0;
	public static final int Align_VCenter = 0;
	public static final int Align_Center = Align_HCenter + Align_VCenter;

	public static final int Align_Left = 2;
	public static final int Align_Right = 4;
	public static final int Align_HStretch = Align_Left + Align_Right;

	public static final int Align_Top = 8;
	public static final int Align_Bottom = 16;
	public static final int Align_VStretch = Align_Top + Align_Bottom;

	public static final int Align_Stretch = Align_HStretch + Align_VStretch;
	public static final int Align_Default = Align_Left + Align_Top;

	public static boolean isBottom(byte value)
	{
		return Align_Bottom == (value & Align_VStretch);
	}

	public static boolean isCenter(byte value)
	{
		return Align_Center == (value & Align_Stretch);
	}

	public static boolean isDefault(byte value)
	{
		return Align_Default == (value & Align_Stretch);
	}

	public static boolean isHCenter(byte value)
	{
		return Align_HCenter == (value & Align_HStretch);
	}

	public static boolean isHStretch(byte value)
	{
		return Align_HStretch == (value & Align_HStretch);
	}

	public static boolean isLeft(byte value)
	{
		return Align_Left == (value & Align_HStretch);
	}

	public static boolean isRight(byte value)
	{
		return Align_Right == (value & Align_HStretch);
	}

	public static boolean isStretch(byte value)
	{
		return Align_Stretch == (value & Align_Stretch);
	}

	public static boolean isTop(byte value)
	{
		return Align_Top == (value & Align_VStretch);
	}

	public static boolean isVCenter(byte value)
	{
		return Align_VCenter == (value & Align_VStretch);
	}

	public static boolean isVStretch(byte value)
	{
		return Align_VStretch == (value & Align_VStretch);
	}

}
