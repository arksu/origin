package com.a2client.network.game.serverpackets;

import com.a2client.gui.GUI_Progressbar;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.Game;

/**
 * Created by arksu on 08.04.17.
 */
public class ActionProgress extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x25, ActionProgress.class);
	}

	private int _targetObjectId;
	private int _count;
	private int _totalCount;

	@Override
	public void readImpl()
	{
		_targetObjectId = readD();
		_count = readH();
		_totalCount = readH();
	}

	@Override
	public void run()
	{
		System.out.println("action: " + _count);

		GUI_Progressbar progress = Game.getInstance().getActionProgress();
		if (_targetObjectId == 0)
		{
			progress.hide();
		}
		else
		{
			progress.show();
			progress.setMax(_totalCount);
			progress.setValue(_count);
		}
	}
}
