package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 17.02.15.
 */
public class ChatMessage extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ChatMessage.class.getName());

	private int _channelId;
	private String _message;

	public ChatMessage(int channelId, String message)
	{
		_channelId = channelId;
		_message = message;
	}

	@Override
	protected void write()
	{
		writeC(0x16);
		writeC(_channelId);
		writeS(_message);
	}
}
