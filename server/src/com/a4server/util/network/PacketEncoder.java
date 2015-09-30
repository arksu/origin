package com.a4server.util.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by arksu on 03.01.2015.
 */
public class PacketEncoder extends MessageToByteEncoder<BaseSendPacket>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, BaseSendPacket msg, ByteBuf out) throws Exception
	{
		msg.EncodePacket(out);
	}
}