package com.a4server.gameserver.network.packets.serverpackets;

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
			writeS(_hand.getItem().getTemplate().getIconName());
			writeC(_hand.getItem().getWidth());
			writeC(_hand.getItem().getHeight());
			writeC(_hand.getMx());
			writeC(_hand.getMy());
		}
		else
		{
			writeD(0);
		}
	}
}
