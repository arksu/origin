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

package com.a2client.dialogs;

import com.a2client.Main;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Label;
import com.a2client.gui.GUI_Panel;
import com.a2client.gui.GUI_Panel.RenderMode;
import com.a2client.util.Align;
import com.badlogic.gdx.graphics.Color;

public class dlg_SysMsg
{
	public static GUI_Label sysmsg_lbl;
	public static GUI_Panel sysmsg_panel;
	public static int sysmsg_timer = 0;

	public static void Update()
	{
		// если надо уменьшаем таймер
		if (sysmsg_timer > 0)
		{
			sysmsg_timer -= Main.DT;

			// если таймер истек
			if (sysmsg_timer <= 0)
			{
				// убираем контролы
				sysmsg_panel.Unlink();
				sysmsg_panel = null;
				sysmsg_lbl = null;
				sysmsg_timer = 0;
			}
		}

	}

	public static void ShowSysMessage(String msg)
	{
		if (sysmsg_lbl != null)
		{
			sysmsg_lbl.caption = msg;
		}
		else
		{
			sysmsg_panel = new GUI_Panel(GUI.getInstance().popup);
			sysmsg_panel.SetSize(500, 40);
			sysmsg_panel.CenterX();
			sysmsg_panel.SetY(100);
			sysmsg_panel.render_mode = RenderMode.rmColor;
			sysmsg_panel.bg_color = new Color(0f, 0f, 0f, 0.4f);

			sysmsg_lbl = new GUI_Label(sysmsg_panel);
			sysmsg_lbl.caption = msg;
			sysmsg_lbl.SetPos(0, 0);
			sysmsg_lbl.SetSize(sysmsg_panel.size);
			sysmsg_lbl.align = Align.Align_Center;
		}
		sysmsg_timer = 5000;
	}

}
