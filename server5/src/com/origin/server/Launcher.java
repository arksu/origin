package com.origin.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Locale;

public class Launcher
{
	private static Logger _log = LoggerFactory.getLogger(Launcher.class.getName());

	public static void main(String... args)
	{
		Locale.setDefault(Locale.ROOT);

		_log.debug("starting game server...");

		try
		{
			GameServer gameServer = new GameServer(new InetSocketAddress(Config.GAME_SERVER_HOST, Config.GAME_SERVER_PORT));
			gameServer.run();
		}
		catch (Exception e)
		{
			_log.error("failed start game server", e);
			System.exit(-1);
		}
	}
}
