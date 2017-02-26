package com.a4server.loginserver.network;

import com.a4server.Config;
import com.a4server.loginserver.LoginClient;
import com.a4server.loginserver.network.serverpackets.Init;
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
public class LoginServerHandler extends SimpleChannelInboundHandler<byte[]>
{
	private static final Logger _log = LoggerFactory.getLogger(LoginServerHandler.class.getName());

	private LoginClient client;

	private static AsyncPacketReader<LoginClient> _reader;

	public LoginServerHandler(AsyncPacketReader<LoginClient> reader)
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
		client = new LoginClient(ctx);
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
		_log.warn("Unexpected exception from downstream. " + cause.getMessage(), cause);
		ctx.close();
	}

}