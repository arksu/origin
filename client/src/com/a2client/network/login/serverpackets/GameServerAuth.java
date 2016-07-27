package com.a2client.network.login.serverpackets;

import com.a2client.Config;
import com.a2client.Main;
import com.a2client.network.Net;
import com.a2client.network.netty.NettyConnection;
import com.a2client.screens.Login;
import com.a2client.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GameServerAuth extends LoginServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(GameServerAuth.class.getName());

	byte[] _host;

	@Override
	public void readImpl()
	{
		Login._gameserver_key1 = readD();
		Login._gameserver_key2 = readD();

		_host = readB(4);

		Login._gameserver_port = readD();
	}

	@Override
	public void run()
	{
		Login.setStatus("login_game");

		// получаем хост гейм сервера
		try
		{
			InetAddress adr = InetAddress.getByAddress(_host);
			if (!Utils.isEmpty(Config._gameServer))
			{
				Login._gameserver_host = Config._gameServer;
			}
			else
			{
				Login._gameserver_host = adr.getHostAddress();
			}

		}
		catch (UnknownHostException e)
		{
			Login._gameserver_host = "";
			_log.error("invalid gameserver hostname");
			Main.ReleaseAll();
			return;
		}

		// закроем текущее соединение
		Net.CloseConnection();

		// идем на гейм сервер
		_log.debug("connecting to gameserver: " + Login._gameserver_host + " : " + Login._gameserver_port);
		Net.NewConnection(Login._gameserver_host, Login._gameserver_port, NettyConnection.ConnectionType.GAME_SERVER);

	}
}
