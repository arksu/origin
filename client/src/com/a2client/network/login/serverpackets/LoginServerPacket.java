package com.a2client.network.login.serverpackets;

import com.a2client.network.netty.NettyConnection;
import com.a2client.util.network.BaseRecvPacket;

public abstract class LoginServerPacket extends BaseRecvPacket
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
}
