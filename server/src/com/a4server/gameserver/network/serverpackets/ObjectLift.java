package com.a4server.gameserver.network.serverpackets;

/**
 * Created by arksu on 26.04.17.
 */
public class ObjectLift extends GameServerPacket
{
	private final int _liftedObjectId;
	private final int _parentObjectId;

	public ObjectLift(int liftedObjectId, int parentObjectId)
	{
		_liftedObjectId = liftedObjectId;
		_parentObjectId = parentObjectId;
	}

	@Override
	protected void write()
	{
		writeC(0x26);
		writeD(_liftedObjectId);
		writeD(_parentObjectId);
	}
}
