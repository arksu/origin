package com.a4server.loginserver.network.serverpackets;

import com.a4server.Config;
import com.a4server.loginserver.LoginClient;
import com.a4server.loginserver.SessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by arksu on 03.01.2015.
 */
public class GameServerAuth extends LoginServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(GameServerAuth.class.getName());

	private SessionKey _key;
	private byte[] _ip;

	public GameServerAuth(LoginClient client)
	{
		_key = client.getSessionKey();
		try
		{
			_ip = InetAddress.getByName(Config.GAME_SERVER_HOST).getAddress();
		}
		catch (UnknownHostException e)
		{
			_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
			_ip = new byte[4];
			_ip[0] = 127;
			_ip[1] = 0;
			_ip[2] = 0;
			_ip[3] = 1;
		}
	}

	@Override
	protected void write()
	{
		writeC(0x04);

		writeD(_key.getId1());
		writeD(_key.getId2());

		writeC(_ip[0]);
		writeC(_ip[1]);
		writeC(_ip[2]);
		writeC(_ip[3]);
		writeD(Config.GAME_SERVER_PORT);
	}
}
