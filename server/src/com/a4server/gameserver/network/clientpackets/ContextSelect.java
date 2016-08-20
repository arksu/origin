package com.a4server.gameserver.network.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 20.08.16.
 */
public class ContextSelect extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ContextSelect.class.getName());

	private String _item;

	@Override
	public void readImpl()
	{
		_item = readS();
	}

	@Override
	public void run()
	{
		_log.debug("context select: " + _item);
	}
}
