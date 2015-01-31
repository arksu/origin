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
import com.a2client.network.game.clientpackets.CharacterCreate;

public class dlg_CreateCharacter extends Dialog
{
    public static dlg_CreateCharacter dlg = null;

    GUI_Button btn_ok, btn_cancel;
    GUI_Edit edit_nickname;
    GUI_Label lbl_nickname, lbl_gender, lbl_hairstyle, lbl_haircolor, lbl_skincolor;
    GUI_Panel avatar, sel_hairstyle, sel_skincolor, sel_haircolor;
    GUI_ComboBox cb_gender;
    GUI_Texture char_spr;

    private static final int ELEMENT_H = 25;
    private static final int MARGIN_H = 5;

    static
    {
        Dialog.AddType("dlg_createcharacter", new DialogFactory()
        {
            public Dialog create()
            {
                return new dlg_CreateCharacter();
            }
        });
    }

    private void OnBtnOk()
    {
        if (edit_nickname.text.isEmpty())
        {
            dlg_SysMsg.ShowSysMessage("Enter nickname!");
            return;
        }
        if (cb_gender.GetSelected() < 0)
        {
            dlg_SysMsg.ShowSysMessage("Select gender!");
            return;
        }

        new CharacterCreate(edit_nickname.text, ((cb_gender.GetSelected() == 1) ? 0 : 1), 0, 0, 0).Send();
        /*
        RawPacketOld p = new RawPacketOld(NetLogin.LOGINSERVER_CREATE_CHAR);
        p.write_string_ascii(edit_nickname.text);
        byte b = (byte)((cb_gender.GetSelected() == 1)?0:1);
        p.write_byte(b);
        LoginConnect.Send(p);
        */
    }

    private void OnBtnCancel()
    {

        Main.ReleaseAll();
    }

    @Override
    public void DoShow()
    {
        dlg = this;

        btn_ok = new GUI_Button(GUI.getInstance().normal)
        {
            @Override
            public void DoClick()
            {
                OnBtnOk();
            }
        };

        btn_cancel = new GUI_Button(GUI.getInstance().normal)
        {
            @Override
            public void DoClick()
            {
                OnBtnCancel();
            }
        };

        btn_ok.SetSize(200, ELEMENT_H);
        btn_ok.caption = Lang.getTranslate("generic", "ok");
        btn_cancel.SetSize(btn_ok.size.clone());
        btn_cancel.caption = Lang.getTranslate("generic", "cancel");

        avatar = new GUI_Panel(GUI.getInstance().normal);
        avatar.SetPos(260, 30);
        avatar.render_mode = GUI_Panel.RenderMode.rmSkin;
        avatar.skin_element = "hint";

        char_spr = new GUI_Texture(avatar);
        //        char_spr.setTexture(ResourceManager.getInstance().getTexture("char_create_back"));
        char_spr.SetPos(3, 3);

        lbl_nickname = new GUI_Label(GUI.getInstance().normal);
        lbl_nickname.caption = Lang.getTranslate("generic", "nickname");
        lbl_nickname.SetPos(30, 30);
        edit_nickname = new GUI_Edit(GUI.getInstance().normal);
        edit_nickname.SetPos(lbl_nickname.pos.add(0, ELEMENT_H + MARGIN_H));
        edit_nickname.text = "";
        edit_nickname.SetSize(200, ELEMENT_H);

        lbl_gender = new GUI_Label(GUI.getInstance().normal);
        lbl_gender.caption = Lang.getTranslate("generic", "gender");
        lbl_gender.SetPos(lbl_nickname.pos.add(0, ELEMENT_H * 3));
        cb_gender = new GUI_ComboBox(GUI.getInstance().normal);
        cb_gender.SetPos(lbl_gender.pos.add(0, ELEMENT_H + MARGIN_H));
        cb_gender.SetSize(200, ELEMENT_H);
        cb_gender.AddItem(Lang.getTranslate("generic", "male"));
        cb_gender.AddItem(Lang.getTranslate("generic", "female"));

        lbl_hairstyle = new GUI_Label(GUI.getInstance().normal);
        lbl_hairstyle.caption = Lang.getTranslate("generic", "hairstyle");
        lbl_hairstyle.SetPos(lbl_gender.pos.add(0, ELEMENT_H * 3));
        sel_hairstyle = new GUI_Panel(GUI.getInstance().normal);
        sel_hairstyle.SetSize(cb_gender.size.clone());
        sel_hairstyle.SetPos(lbl_hairstyle.pos.add(0, ELEMENT_H + MARGIN_H));
        sel_hairstyle.render_mode = GUI_Panel.RenderMode.rmSkin;
        sel_hairstyle.skin_element = "hint";
        sel_hairstyle.caption = "not available now";

        lbl_haircolor = new GUI_Label(GUI.getInstance().normal);
        lbl_haircolor.caption = Lang.getTranslate("generic", "haircolor");
        lbl_haircolor.SetPos(lbl_hairstyle.pos.add(0, ELEMENT_H * 3));
        sel_haircolor = new GUI_Panel(GUI.getInstance().normal);
        sel_haircolor.SetSize(cb_gender.size.clone());
        sel_haircolor.SetPos(lbl_haircolor.pos.add(0, ELEMENT_H + MARGIN_H));
        sel_haircolor.render_mode = GUI_Panel.RenderMode.rmSkin;
        sel_haircolor.skin_element = "hint";
        sel_haircolor.caption = "not available now";

        lbl_skincolor = new GUI_Label(GUI.getInstance().normal);
        lbl_skincolor.caption = Lang.getTranslate("generic", "skincolor");
        lbl_skincolor.SetPos(lbl_haircolor.pos.add(0, ELEMENT_H * 3));
        sel_skincolor = new GUI_Panel(GUI.getInstance().normal);
        sel_skincolor.SetSize(cb_gender.size.clone());
        sel_skincolor.SetPos(lbl_skincolor.pos.add(0, ELEMENT_H + MARGIN_H));
        sel_skincolor.render_mode = GUI_Panel.RenderMode.rmSkin;
        sel_skincolor.skin_element = "hint";
        sel_skincolor.caption = "not available now";
    }

    @Override
    public void DoResolutionChanged()
    {
        int w = Config.getScreenWidth() - avatar.pos.x - 30;
        int h = Config.getScreenHeight() - avatar.pos.y - 30;
        avatar.SetSize(w, h);
        char_spr.SetSize(avatar.size.sub(6, 6));

        btn_cancel.SetPos(30, Config.getScreenHeight() - ELEMENT_H - 30);
        btn_ok.SetPos(btn_cancel.pos.add(0, -ELEMENT_H * 2));
    }

    @Override
    public void DoHide()
    {
        dlg = null;

        avatar.Unlink();
        avatar = null;
        lbl_nickname.Unlink();
        lbl_nickname = null;
        edit_nickname.Unlink();
        edit_nickname = null;
        lbl_gender.Unlink();
        lbl_gender = null;
        cb_gender.Unlink();
        cb_gender = null;
        lbl_hairstyle.Unlink();
        lbl_hairstyle = null;
        sel_hairstyle.Unlink();
        sel_hairstyle = null;
        lbl_skincolor.Unlink();
        lbl_skincolor = null;
        sel_skincolor.Unlink();
        sel_skincolor = null;
        lbl_haircolor.Unlink();
        lbl_haircolor = null;
        sel_haircolor.Unlink();
        sel_haircolor = null;

        btn_cancel.Unlink();
        btn_cancel = null;
        btn_ok.Unlink();
        btn_ok = null;
    }

    public static boolean Exist()
    {
        return dlg != null;
    }
}
