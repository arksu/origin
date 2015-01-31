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

import com.a2client.Lang;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Label;
import com.a2client.gui.GUI_Window;
import com.a2client.util.Align;

public class dlg_Version extends Dialog
{
    public static dlg_Version dlg = null;

    GUI_Window wnd;
    GUI_Label status;

    static
    {
        Dialog.AddType("dlg_version", new DialogFactory()
        {
            public Dialog create()
            {
                return new dlg_Version();
            }
        });
    }

    public void DoShow()
    {
        dlg = this;

        wnd = new GUI_Window(GUI.getInstance().modal);
        wnd.SetSize(450, 200);
        wnd.Center();
        wnd.caption = Lang.getTranslate("generic", "error");
        wnd.set_close_button(false);
        wnd.resizeable = false;

        status = new GUI_Label(wnd);
        status.SetPos(20, 90);
        status.SetSize(wnd.Width() - 40, 50);
        status.align = Align.Align_HCenter + Align.Align_Top;
        // TODO : сообщение об ошибке - обновить версию клиента
        //        status.caption = Lang.getTranslate("generic", "update_version") + " 0." + Integer.toString(Resource.srv_versions
        //                .get("client"));
    }


    public void DoHide()
    {
        dlg = null;

        wnd.Unlink();
        wnd = null;
    }

    public static boolean Exist()
    {
        return dlg != null;
    }
}
