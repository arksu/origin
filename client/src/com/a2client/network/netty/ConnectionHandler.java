package com.a2client.network.netty;

import com.a2client.Log;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.network.game.serverpackets.GameServerPacket;
import com.a2client.network.login.LoginPacketHandler;
import com.a2client.network.login.serverpackets.LoginServerPacket;
import com.a2client.util.network.BaseRecvPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ConnectionHandler extends SimpleChannelInboundHandler<byte[]>
{
    final private NettyConnection _connection;

    public ConnectionHandler(NettyConnection connection)
    {
        _connection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        _connection.setChannel(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        _connection.setChannel(null);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, byte[] bytes) throws Exception
    {
        BaseRecvPacket pkt = null;
        _connection.addRecvCounter(bytes.length);
        switch (_connection.getType())
        {
            case LOGIN_SERVER:
                pkt = LoginPacketHandler.HandlePacket(bytes);
                if (pkt != null)
                {
                    ((LoginServerPacket) pkt).setConnect(_connection);
                }
                else
                {
                    throw new RuntimeException("login packet handler: wrong pkt type " + (bytes[0] & 0xff));
                }
                break;
            case GAME_SERVER:
                pkt = GamePacketHandler.HandlePacket(bytes);
                if (pkt != null)
                {
                    ((GameServerPacket) pkt).setConnect(_connection);
                }
                else
                {
                    throw new RuntimeException("game packet handler: wrong pkt type " + (bytes[0] & 0xff));
                }
                break;
        }

        _connection.addPacket(pkt);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        Log.error("Unexpected exception from downstream.");
        _connection.setChannel(null);
        ctx.close();
    }
}
