package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.gamegui.GUI_ContextMenu;
import com.a2client.model.GameObject;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.network.game.clientpackets.ContextSelect;
import com.a2client.screens.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 20.08.16.
 */
public class ContextMenu extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x23, ContextMenu.class);
	}

	private int _objectId;
	private List<String> _list;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		int count = readC();
		_list = new ArrayList<>();
		while (count > 0)
		{
			_list.add(readS());
			count--;
		}
	}

	@Override
	public void run()
	{
		Game game = Game.getInstance();
		if (game != null && ObjectCache.getInstance() != null)
		{
			Matrix4 MVP = game.getCamera().combined.cpy();
			GameObject object = ObjectCache.getInstance().getObject(_objectId);
			Vector3 pos = object.getWorldCoord().cpy();
			pos.prj(MVP);

			float x = pos.x + 1;
			float y = 1 - pos.y;

			x /= 2f;
			y /= 2f;

			x *= Gdx.graphics.getWidth();
			y *= Gdx.graphics.getHeight();

			System.out.println("x=" + x + " y=" + y);

			GUI_ContextMenu popup = GUI_ContextMenu.popup(new com.a2client.gamegui.ContextMenu()
			{
				@Override
				public void OnContextClick(int idx)
				{
					new ContextSelect(_list.get(idx)).Send();
				}
			});

			for (String s : _list)
			{
				popup.addMenuItem(s);
			}
			popup.apply();
		}
	}
}
