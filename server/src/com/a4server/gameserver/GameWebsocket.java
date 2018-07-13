package com.a4server.gameserver;

import com.a4server.loginserver.LoginClient;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ImmutableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class GameWebsocket extends WebSocketServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameWebsocket.class.getName());

	public GameWebsocket(InetSocketAddress address)
	{
		super(address);
		setReuseAddr(true);
		setTcpNoDelay(true);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		_log.debug("ws open: " + conn.getRemoteSocketAddress().toString());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		_log.debug("ws close");
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		_log.debug("ws message: " + message);
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message)
	{
		_log.debug("ws message bin: " + message);

		byte[] bytes = message.array();
		String s = null;
		try
		{
			s = new String(bytes,"UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		_log.debug("s: " + s);
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		_log.debug("ws error: " + ex.getMessage());
	}

	@Override
	public void onStart()
	{
		_log.debug("ws started");
	}
}
