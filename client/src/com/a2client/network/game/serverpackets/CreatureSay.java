package com.a2client.network.game.serverpackets;

import com.a2client.Main;
import com.a2client.ObjectCache;
import com.a2client.model.GameObject;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 17.02.15.
 */
public class CreatureSay extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x17, CreatureSay.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(CreatureSay.class.getName());

	private int _objectId;
	private String _message;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_message = readS();
	}

	@Override
	public void run()
	{
		_log.debug("CreatureSay: " + _objectId + ": " + _message);
		GameObject obj = ObjectCache.getInstance().getObject(_objectId);
		String msg = (obj != null ? obj.getName() : "*") + ": " + _message;
		((Game) Main.getInstance().getScreen())._chatMemo.AddLine(msg);
	}
}
