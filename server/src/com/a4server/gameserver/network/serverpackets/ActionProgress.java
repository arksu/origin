package com.a4server.gameserver.network.serverpackets;

/**
 * Created by arksu on 08.04.17.
 */
public class ActionProgress extends GameServerPacket
{
	public static final ActionProgress EMPTY = new ActionProgress(0, 0, 0);

	private final int _targetObjectId;
	private final int _count;
	private final int _totalCount;

	public ActionProgress(int targetObjectId, int count, int totalCount)
	{
		_targetObjectId = targetObjectId;
		_count = count;
		_totalCount = totalCount;
	}

	@Override
	protected void write()
	{
		writeC(0x25);
		writeD(_targetObjectId);
		writeH(_count);
		writeH(_totalCount);
	}
}
