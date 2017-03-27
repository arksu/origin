package com.a2client.network.game.serverpackets;

import com.a2client.PlayerData;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharInfo extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x0D, CharInfo.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(CharInfo.class.getName());

	private int _objectId;
	private String _name;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_name = readS();
		_log.debug("CharInfo: " + _name + " id=" + _objectId);
	}

	@Override
	public void run()
	{
		PlayerData.getInstance().setObjectId(_objectId);
		PlayerData.getInstance().setName(_name);
	}
}
