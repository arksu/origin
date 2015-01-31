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

import com.a2client.Config;
import com.a2client.Main;
import com.a2client.Lang;
import com.a2client.gui.*;
import com.a2client.gui.GUI_Panel.RenderMode;
import com.a2client.network.game.clientpackets.CharacterSelect;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class dlg_Chars extends Dialog
{
    public static dlg_Chars dlg = null;
    public List<GUI_Button> buttons = new ArrayList<GUI_Button>();
    public int last_char_id = 0;
    GUI_Window wnd;
    GUI_Button btn_create;

    static
    {
        Dialog.AddType("dlg_chars", new DialogFactory()
        {
            public Dialog create()
            {
                return new dlg_Chars();
            }
        });
    }

    // список персонажей получен
    static public void CharsRecv()
    {
        if (dlg == null)
            return;

        if (Config.quick_login_mode && dlg.last_char_id > 0)
        {
            new CharacterSelect(dlg.last_char_id).Send();
            dlg.DisableButtons();
        }
        else
        {
            for (GUI_Button b : dlg.buttons)
            {
                b.enabled = true;
            }
        }
        Config.quick_login_mode = false;
        if (dlg != null)
            dlg.wnd.Center();
    }

    static public void AddChar(final int char_id, String name)
    {
        if (dlg == null)
            return;
        GUI_Button btn = new GUI_Button(dlg.wnd)
        {
            public void DoClick()
            {
                new CharacterSelect(char_id).Send();
                dlg.DisableButtons();
            }
        };
        dlg.buttons.add(btn);
        btn.caption = name;
        btn.SetPos(100, dlg.buttons.size() * 30 + 60);
        btn.SetSize(200, 20);
        btn.CenterX();
        btn.enabled = false;

        if (char_id == dlg.last_char_id)
        {
            GUI_Panel panel = new GUI_Panel(dlg.wnd);
            panel.bg_color = Color.ORANGE;
            panel.render_mode = RenderMode.rmColor;
            panel.SetPos(25, dlg.buttons.size() * 30 + 60);
            panel.SetSize(20, 20);
        }
        dlg.wnd.SetSize(250, btn.pos.y + 40);

    }

    public void DisableButtons()
    {
        for (GUI_Button btn : buttons)
        {
            btn.enabled = false;
        }
        btn_create.enabled = false;
    }

    public void DoShow()
    {
        dlg = this;
        wnd = new GUI_Window(GUI.getInstance().normal)
        {
            protected void DoClose()
            {
                //                NetLogin.setErrorText("aborted");
                Main.ReleaseAll();
            }
        };
        wnd.SetSize(250, 100);
        wnd.caption = Lang.getTranslate("login", "select_char");
        wnd.resizeable = false;

        btn_create = new GUI_Button(wnd)
        {
            @Override
            public void DoClick()
            {
                Dialog.HideAll();
                Dialog.Show("dlg_createcharacter");
            }
        };
        btn_create.SetSize(200, 20);
        btn_create.SetPos(100, 40);
        btn_create.CenterX();
        btn_create.caption = Lang.getTranslate("login", "create_char");

    }

    public void DoHide()
    {
        for (GUI_Button btn : buttons)
        {
            btn.Unlink();
        }
        buttons.clear();
        dlg = null;
        wnd.Unlink();
        wnd = null;
    }

    public static boolean Exist()
    {
        return dlg != null;
    }
}
