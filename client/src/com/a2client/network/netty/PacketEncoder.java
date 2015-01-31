package com.a2client.network.netty;

import com.a2client.util.network.BaseSendPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<BaseSendPacket>
{
    @Override
    protected void encode(ChannelHandlerContext ctx, BaseSendPacket msg, ByteBuf out) throws Exception
    {
        msg.EncodePacket(out);
    }
}
