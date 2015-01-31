package com.a2client.network.game.serverpackets;

import com.a2client.network.Net;
import com.a2client.network.game.clientpackets.GameClientPacket;
import com.a2client.network.netty.NettyConnection;
import com.a2client.util.network.BaseRecvPacket;

public abstract class GameServerPacket extends BaseRecvPacket
{
    private NettyConnection _connect;

    public NettyConnection getConnect()
    {
        return _connect;
    }

    public void setConnect(NettyConnection connect)
    {
        _connect = connect;
    }

    public void sendPacket(GameClientPacket packet)
    {
        Net.getConnection().sendPacket(packet);
    }
}
