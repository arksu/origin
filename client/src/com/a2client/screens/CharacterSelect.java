package com.a2client.screens;

import com.a2client.Main;
import com.a2client.Lang;
import com.a2client.gui.GUI;
import com.a2client.gui.GUI_Button;
import com.a2client.gui.GUI_Label;
import com.a2client.gui.GUI_Window;
import com.a2client.network.game.clientpackets.CharacterDelete;
import com.a2client.network.game.serverpackets.CharacterList;
import com.a2client.util.Align;
import com.badlogic.gdx.Gdx;

public class CharacterSelect extends BaseScreen
{
    GUI_Button btnCreate, btnExit;
    GUI_Label lblStatus;
    private static String _statusText = "";

    public CharacterSelect()
    {
        GUI.reCreate();

        lblStatus = new GUI_Label(GUI.rootNormal());
        lblStatus.align = Align.Align_Center;
        lblStatus.SetSize(200, 30);
        lblStatus.SetPos(0, 30);
        lblStatus.CenterX();

        int posY = 100;
        btnCreate = new GUI_Button(GUI.rootNormal())
        {
            @Override
            public void DoClick()
            {
                CharacterCreate.Show();
            }
        };
        btnCreate.SetPos(0, posY);
        btnCreate.caption = Lang.getTranslate("Game.character.create");
        btnCreate.SetSize(150, 25);
        btnCreate.CenterX();

        posY += 70;

        for (CharacterList.CharacterData c : CharacterList._chars)
        {
            final int charId = c._char_id;
            final String cname = c._char_name;
            GUI_Button btn = new GUI_Button(GUI.rootNormal())
            {
                @Override
                public void DoClick()
                {
                    charSelected(charId);
                }
            };
            btn.caption = c._char_name;
            btn.SetPos(0, posY);
            btn.SetSize(150, 25);
            btn.CenterX();


            GUI_Button btnDelete = new GUI_Button(GUI.rootNormal())
            {
                @Override
                public void DoClick()
                {
                    showDeleteConfirm(charId, cname);
                }
            };
            btnDelete.caption = Lang.getTranslate("Game.character.delete");
            btnDelete.SetPos(btn.pos.add(160, 0));
            btnDelete.SetSize(150, 25);

            posY += 30;
        }

        btnExit = new GUI_Button(GUI.rootNormal())
        {
            @Override
            public void DoClick()
            {
                Main.ReleaseAll();
                Login.setStatus("disconnected");
            }
        };
        btnExit.caption = Lang.getTranslate("Game.cancel");
        btnExit.SetSize(100, 25);
        btnExit.SetPos(Gdx.graphics.getWidth() - 110, Gdx.graphics.getHeight() - 35);
    }

    protected void showDeleteConfirm(final int charId, final String name)
    {
        final GUI_Window wnd = new GUI_Window(GUI.rootModal());
        wnd.set_close_button(false);
        wnd.caption = Lang.getTranslate("Game.character.delete_confirm");
        wnd.SetSize(325, 200); // 25 + 25 +25 : 250 = 125 * 2
        wnd.Center();

        GUI_Button btnOk = new GUI_Button(wnd)
        {
            @Override
            public void DoClick()
            {
                charDelete(charId);
                wnd.close();
            }
        };
        btnOk.SetPos(25, 120);
        btnOk.SetSize(125, 25);
        btnOk.caption = Lang.getTranslate("Game.ok");

        GUI_Button btnCancel = new GUI_Button(wnd)
        {
            @Override
            public void DoClick()
            {
                wnd.close();
            }
        };
        btnCancel.SetPos(175, 120);
        btnCancel.SetSize(125, 25);
        btnCancel.caption = Lang.getTranslate("Game.cancel");

        GUI_Label lblText = new GUI_Label(wnd);
        lblText.caption = Lang.getTranslate("Game.character.delete_lbl")+ " " + name + "?";
        lblText.align = Align.Align_Center;
        lblText.SetSize(300, 20);
        lblText.SetPos(0, 80);
        lblText.CenterX();

    }

    static public void setStatusText(String statustext)
    {
        _statusText = statustext;
    }

    @Override
    public void onUpdate()
    {
        lblStatus.caption = _statusText;
    }

    public void charSelected(int id)
    {
        new com.a2client.network.game.clientpackets.CharacterSelect(id).Send();
    }

    protected void charDelete(int id)
    {
        new CharacterDelete(id).Send();
    }

    static public void Show()
    {
        _statusText = "";
        Main.freeScreen();
        Main.getInstance().setScreen(new CharacterSelect());
    }
}
