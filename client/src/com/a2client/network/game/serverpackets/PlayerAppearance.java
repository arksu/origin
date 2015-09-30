package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;

/**
 * Created by arksu on 02.02.15.
 */
public class PlayerAppearance extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x13, PlayerAppearance.class);
	}

	int _objectId;
	int _hairStyle;
	int _hairColor;
	int _face;
	boolean _isFemale;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_isFemale = readC() == 1;
		_hairStyle = readC();
		_hairColor = readC();
		_face = readC();
	}

	@Override
	public void run()
	{

	}
}
