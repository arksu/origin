package com.a2client.network.game.serverpackets;

import com.a2client.Player;
import com.a2client.model.Hand;
import com.a2client.network.game.GamePacketHandler;

/**
 * Created by arksu on 13.09.15.
 */
public class PlayerHand extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x1C, PlayerHand.class);
	}

	int _objectId;
	int _offsetX;
	int _offsetY;
	int _typeId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		if (_objectId != 0)
		{
			_typeId = readD();
			_offsetX = readC();
			_offsetY = readC();
		}
	}

	@Override
	public void run()
	{
		Hand hand = null;
		if (_objectId != 0)
		{
			hand = new Hand();
		}
		Player.getInstance().setHand(hand);
	}
}
