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

import com.a2client.gui.GUI;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class Input implements InputProcessor
{
	private static boolean[] Keys = new boolean[256];
	private static boolean[] HitKeys = new boolean[256];
	public static boolean[] MouseBtns = new boolean[3];

	public static int MouseX = 0;
	public static int MouseY = 0;
	public static int MouseWheel = 0;

	public static final int MB_LEFT = 0;
	public static final int MB_RIGHT = 1;
	public static final int MB_MIDDLE = 2;
	public static final int MB_DOUBLE = 3;

	public void Update()
	{
		boolean old;
		for (int i = 0; i < 256; i++)
		{
			old = Keys[i];
			Keys[i] = Gdx.input.isKeyPressed(i);
			HitKeys[i] = (!old && Keys[i]);
		}

		for (int i = 0; i < 3; i++)
		{
			MouseBtns[i] = Gdx.input.isButtonPressed(i);
		}

		// обрабатываем введеный текст
		//        while (Keyboard.next())
		//        {
		//            GUI.getInstance()
		//               .HandleKey(Lang.GetChar(Keyboard.getEventCharacter()), Keyboard.getEventKey(), Keyboard.getEventKeyState());
		//
		//            debug_str = "char=" + Keyboard.getEventCharacter() + " key=" + Keyboard.getEventKey() + " state=" + Keyboard
		//                    .getEventKeyState();
		//        }
	}

	public static void RemoveHit(int key)
	{
		HitKeys[key] = false;
	}

	static public boolean KeyHit(int key)
	{
		return HitKeys[key];
	}

	static public boolean KeyDown(int key)
	{
		return Keys[key];
	}

	static public boolean isCtrlPressed()
	{
		return KeyDown(com.badlogic.gdx.Input.Keys.CONTROL_LEFT) || KeyDown(com.badlogic.gdx.Input.Keys.CONTROL_RIGHT);
	}

	static public boolean isShiftPressed()
	{
		return KeyDown(com.badlogic.gdx.Input.Keys.SHIFT_LEFT) || KeyDown(com.badlogic.gdx.Input.Keys.SHIFT_RIGHT);
	}

	static public boolean isAltPressed()
	{
		return KeyDown(com.badlogic.gdx.Input.Keys.ALT_LEFT) || KeyDown(com.badlogic.gdx.Input.Keys.ALT_RIGHT);
	}

	static public int GetKeyState()
	{
		return (isCtrlPressed() ? 1 : 0) + (isShiftPressed() ? 2 : 0) + (isAltPressed() ? 4 : 0);
	}

	static public boolean isWheelUpdated()
	{
		return MouseWheel != 0;
	}

	//------------------------------------------------------------------------------------------------------------------
	// GDX INPUT PROCESSOR

	@Override
	public boolean keyDown(int keycode)
	{
		GUI.getInstance().HandleKey((char) 0, keycode, true);
		return true;
	}

	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		GUI.getInstance().HandleKey(character, 0, true);
		return true;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		MouseX = screenX;
		MouseY = screenY;
		return false;
	}

	@Override
	public boolean scrolled(int amount)
	{
		MouseWheel = amount;
		return true;
	}
}
