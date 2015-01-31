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
import com.a2client.Log;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Label;
import com.a2client.gui.GUI_Progressbar;
import com.a2client.util.Align;
import com.badlogic.gdx.graphics.Color;

public class dlg_Progress extends Dialog
{
    static public dlg_Progress dlg = null;
    //	public GUI_Texture tex;
    public GUI_Progressbar progress;
    public GUI_Label txt_label;

    static
    {
        Dialog.AddType("dlg_progress", new DialogFactory()
        {
            public Dialog create()
            {
                return new dlg_Progress();
            }
        });
    }

    public void DoShow()
    {
        dlg = this;

        //		tex = new GUI_Texture(GUI.getInstance().popup);
        //		tex.SetSize(43, 66);
        //		tex.SetPos((Config.ScreenWidth-tex.Width())/2, (Config.ScreenHeight-tex.Height())/2);
        //		tex.mode = "skin_element";


        progress = new GUI_Progressbar(GUI.getInstance().popup);
        progress.SetSize(200, 30);
        progress.CenterX();
        progress.SetY(150);

        txt_label = new GUI_Label(GUI.getInstance().popup);
        txt_label.SetSize(250, 30);
        txt_label.SetY(progress.pos.y - 40);
        txt_label.CenterX();
        txt_label.align = Align.Align_Center;

    }

    public void DoHide()
    {
        dlg = null;
        //		tex.Unlink();
        //		tex = null;
        progress.Unlink();
        progress = null;
        txt_label.Unlink();
        txt_label = null;
    }

    static public void SetProgress(int val)
    {
        Log.info("set progress: " + val);
        if (val == -1)
        {
            Dialog.Hide("dlg_progress");
        }
        else
        {
            if (dlg == null)
                Dialog.Show("dlg_progress");

            //dlg.tex.skin_element = "hourglass_"+Integer.toString(val+1);
            dlg.progress.SetMin(0);
            dlg.progress.SetMax(30);
            dlg.progress.SetValue(val);
            dlg.progress.SetColor(new Color(0.5f, 0.6f, 0.1f, 1));
            dlg.txt_label.caption = Lang.getTranslate("generic", "action_progress");
        }
    }
/*
    static public void SetReuse(RawPacketOld pkt)
    {
        int time = pkt.read_int();
        int len = pkt.read_int();
        Log.info("set progress time=" + time + " len=" + len);
        if (time == 0)
        {
            Dialog.Hide("dlg_progress");
        }
        else
        {
            if (dlg == null)
                Dialog.Show("dlg_progress");

            //dlg.tex.skin_element = "hourglass_"+Integer.toString(val+1);
            dlg.progress.SetMin(0);
            dlg.progress.SetMax(len);
            dlg.progress.SetValue(time);
            dlg.progress.SetColor(new Color(12, 90, 126));
            dlg.txt_label.caption = Lang.getTranslate("generic", "action_reuse");
        }

    }
*/

    public static boolean Exist()
    {
        return dlg != null;
    }
}
