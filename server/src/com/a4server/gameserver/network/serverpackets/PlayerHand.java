package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Hand;

/**
 * Created by arksu on 13.09.15.
 */
public class PlayerHand extends GameServerPacket
{
	private final Hand _hand;

	public PlayerHand(Hand hand)
	{
		_hand = hand;
	}

	@Override
	protected void write()
	{
		writeC(0x1C);
		if (_hand != null)
		{
			writeD(_hand.getItem().getObjectId());
			writeD(_hand.getItem().getTemplate().getItemId());
			writeC(_hand.getOffsetX());
			writeC(_hand.getOffsetY());
		}
		else
		{
			writeD(0);
		}
	}
}
