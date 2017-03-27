package com.a2client.network.game.serverpackets;

import com.a2client.PlayerData;
import com.a2client.model.Hand;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 13.09.15.
 */
public class PlayerHand extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x1C, PlayerHand.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(PlayerHand.class.getName());

	int _objectId;
	int _w;
	int _h;
	int _offsetX;
	int _offsetY;
	String _icon;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		if (_objectId != 0)
		{
			_icon = readS();
			_w = readC();
			_h = readC();
			_offsetX = readC();
			_offsetY = readC();
		}
		_log.debug("set hand id=" + _objectId);
	}

	@Override
	public void run()
	{
		Hand hand = null;
		if (_objectId != 0)
		{
			hand = new Hand(_objectId, _w, _h, _offsetX, _offsetY, _icon);
		}
		PlayerData.getInstance().setHand(hand);
	}
}
