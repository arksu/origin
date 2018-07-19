package com.a4server.gameserver.network.packets.clientpackets;

import com.a4server.gameserver.network.packets.GamePktClient;
import com.a4server.util.network.BaseRecvPacket;
import com.a4server.util.network.BaseSendPacket;
import com.a4server.util.network.NetClient;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by arksu on 03.01.2015.
 */
public abstract class GameClientPacket extends BaseRecvPacket
{
	protected GamePktClient client;

	public void setClient(NetClient client)
	{
		this.client = (GamePktClient) client;
	}

	public GamePktClient getClient()
	{
		return client;
	}

	public void sendPacket(BaseSendPacket pkt)
	{
		getClient().sendPacket(pkt);
	}

	public ChannelHandlerContext getConnect()
	{
		return getClient().getChannel();
	}
}
