package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.network.game.GamePacketHandler;

/**
 * Created by arksu on 11.02.15.
 */
public class ObjectMove extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x14, ObjectMove.class);
	}

	private int _objectId;
	private int _tox;
	private int _toy;
	private int _vx;
	private int _vy;
	private int _speed;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_tox = readD();
		_toy = readD();
		_vx = readD();
		_vy = readD();
		_speed = readH();
	}

	@Override
	public void run()
	{
		ObjectCache.getInstance().getObject(_objectId).move(_tox, _toy, _vx, _vy, _speed);
	}
}
