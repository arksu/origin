package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * объект двигается
 * Created by arksu on 11.02.15.
 */
public class ObjectMove extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ObjectMove.class.getName());

	private int _objectId;
	private int _tox;
	private int _toy;
	private int _vx;
	private int _vy;
	private int _speed;

	public ObjectMove(int objectId, int tox, int toy, int vx, int vy, int speed)
	{
		_objectId = objectId;
		_tox = tox;
		_toy = toy;
		_vx = vx;
		_vy = vy;
		_speed = speed;
	}

	@Override
	protected void write()
	{
		writeC(0x14);
		writeD(_objectId);
		writeD(_tox);
		writeD(_toy);
		writeD(_vx);
		writeD(_vy);
		writeH(_speed);
	}
}
