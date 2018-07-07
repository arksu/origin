package com.a4server.gameserver;

import com.a4server.loginserver.LoginClient;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class GameWebsocket extends WebSocketServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameWebsocket.class.getName());

	public GameWebsocket(InetSocketAddress address)
	{
		super(address);
		setTcpNoDelay(true);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		_log.debug("open");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		_log.debug("close");
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		_log.debug("message");
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		_log.debug("error");
	}

	@Override
	public void onStart()
	{
		_log.debug("start");
	}
}
