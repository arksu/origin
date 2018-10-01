package com.origin.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class GameServer extends WebSocketServer
{
	private static final Logger _log = LoggerFactory.getLogger(GameServer.class.getName());

	/**
	 * клиенты активных игроков в игре
	 */
	private final List<GameClient> _playerClients = new LinkedList<>();

	public GameServer(InetSocketAddress address)
	{
		super(address);
		setReuseAddr(true);
		setTcpNoDelay(true);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		_log.debug("ws open: " + conn.getRemoteSocketAddress().toString());
		conn.setAttachment(new GameClient());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		_log.debug("ws close");
		Object attachment = conn.getAttachment();
		_playerClients.remove(attachment);
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		_log.debug("ws string message: " + message);
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message)
	{
		byte[] bytes = message.array();
		String s = null;
		try
		{
			s = new String(bytes, "UTF-8");

		}
		catch (UnsupportedEncodingException e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		_log.debug("ws error: " + ex.getMessage());
	}

	@Override
	public void onStart()
	{
		_log.debug("game server started at " + this.getAddress().toString());
	}
}
