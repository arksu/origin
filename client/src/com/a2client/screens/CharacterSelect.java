package com.a2client.screens;

import com.a2client.Lang;
import com.a2client.Main;
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
		lblStatus.setSize(200, 30);
		lblStatus.setPos(0, 30);
		lblStatus.centerX();

		int posY = 100;
		btnCreate = new GUI_Button(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				CharacterCreate.Show();
			}
		};
		btnCreate.setPos(0, posY);
		btnCreate.caption = Lang.getTranslate("Game.character.create");
		btnCreate.setSize(150, 25);
		btnCreate.centerX();

		posY += 70;

		for (CharacterList.CharacterData c : CharacterList._chars)
		{
			final int charId = c._char_id;
			final String cname = c._char_name;
			GUI_Button btn = new GUI_Button(GUI.rootNormal())
			{
				@Override
				public void doClick()
				{
					charSelected(charId);
				}
			};
			btn.caption = c._char_name;
			btn.setPos(0, posY);
			btn.setSize(150, 25);
			btn.centerX();

			GUI_Button btnDelete = new GUI_Button(GUI.rootNormal())
			{
				@Override
				public void doClick()
				{
					showDeleteConfirm(charId, cname);
				}
			};
			btnDelete.caption = Lang.getTranslate("Game.character.delete");
			btnDelete.setPos(btn.pos.add(160, 0));
			btnDelete.setSize(150, 25);

			posY += 30;
		}

		btnExit = new GUI_Button(GUI.rootNormal())
		{
			@Override
			public void doClick()
			{
				Main.ReleaseAll();
				Login.setStatus("disconnected");
			}
		};
		btnExit.caption = Lang.getTranslate("Game.cancel");
		btnExit.setSize(100, 25);
		btnExit.setPos(Gdx.graphics.getWidth() - 110, Gdx.graphics.getHeight() - 35);
	}

	protected void showDeleteConfirm(final int charId, final String name)
	{
		final GUI_Window wnd = new GUI_Window(GUI.rootModal());
		wnd.set_close_button(false);
		wnd.caption = Lang.getTranslate("Game.character.delete_confirm");
		wnd.setSize(325, 200); // 25 + 25 +25 : 250 = 125 * 2
		wnd.center();

		GUI_Button btnOk = new GUI_Button(wnd)
		{
			@Override
			public void doClick()
			{
				charDelete(charId);
				wnd.close();
			}
		};
		btnOk.setPos(25, 120);
		btnOk.setSize(125, 25);
		btnOk.caption = Lang.getTranslate("Game.ok");

		GUI_Button btnCancel = new GUI_Button(wnd)
		{
			@Override
			public void doClick()
			{
				wnd.close();
			}
		};
		btnCancel.setPos(175, 120);
		btnCancel.setSize(125, 25);
		btnCancel.caption = Lang.getTranslate("Game.cancel");

		GUI_Label lblText = new GUI_Label(wnd);
		lblText.caption = Lang.getTranslate("Game.character.delete_lbl") + " " + name + "?";
		lblText.align = Align.Align_Center;
		lblText.setSize(300, 20);
		lblText.setPos(0, 80);
		lblText.centerX();

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
		new com.a2client.network.game.clientpackets.CharacterSelect(id).send();
	}

	protected void charDelete(int id)
	{
		new CharacterDelete(id).send();
	}

	static public void Show()
	{
		_statusText = "";
		Main.freeScreen();
		Main.getInstance().setScreen(new CharacterSelect());
	}
}
