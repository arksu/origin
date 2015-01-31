package com.a2client.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class PacketDecoder extends ReplayingDecoder<PacketDecoder.DecoderState>
{
    public enum DecoderState
    {
        READ_LENGTH,
        READ_CONTENT;
    }

    private int length;

    public PacketDecoder()
    {
        // Set the initial state.
        super(DecoderState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        switch (state())
        {
            case READ_LENGTH:
                length = (in.readByte() & 0xff) + (in.readByte() & 0xff) * 256;
                checkpoint(DecoderState.READ_CONTENT);
                break;
            case READ_CONTENT:
                byte[] buf = in.readBytes(length).array();
                checkpoint(DecoderState.READ_LENGTH);
                out.add(buf);
                break;
            default:
                throw new Error("Shouldn't reach here.");
        }
    }
}
