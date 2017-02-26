package com.a4server.gameserver.network;

import com.a4server.Config;
import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.network.serverpackets.Init;
import com.a4server.util.network.AsyncPacketReader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by arksu on 03.01.2015.
 */
public class GameServerHandler extends SimpleChannelInboundHandler<byte[]>
{
	private GameClient client;
	private final AsyncPacketReader<GameClient> _reader;

	private static final Logger _log = LoggerFactory.getLogger(GameServerHandler.class.getName());

	public GameServerHandler(AsyncPacketReader<GameClient> reader)
	{
		super();
		_reader = reader;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception
	{
		// добавляем данные в очередь пакетов клиента
		client.addReadPacketQueue(msg);
		// добавим клиента на обработку чтения данных
		_reader.addClientToProcess(client);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception
	{
		super.channelActive(ctx);
		if (Config.DEBUG)
		{
			InetAddress address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();
			_log.info("CONNECTED : " + address.getHostAddress());
		}
		client = new GameClient(ctx);
		// шлем клиенту пакет Init
		ctx.writeAndFlush(new Init());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		super.channelInactive(ctx);
		client.onDisconnect();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		// Close the connection when an exception is raised.
		//_log.warn("Unexpected exception from downstream. " + cause.getMessage(), cause);
		ctx.close();
	}
}

