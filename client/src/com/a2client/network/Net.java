package com.a2client.network;

import com.a2client.network.netty.NettyConnection;

public class Net
{
    private static NettyConnection _connection;

    public static NettyConnection NewConnection(String host, int port, NettyConnection.ConnectionType type)
    {
        if (_connection != null)
        {
            _connection.Close();
        }
        _connection = new NettyConnection(host, port, type);
        return _connection;
    }

    public static NettyConnection getConnection()
    {
        return _connection;
    }

    public static void CloseConnection()
    {
        if (_connection != null)
        {
            _connection.Close();
        }
        _connection = null;
    }

    public static void ProcessPackets()
    {
        if (_connection != null)
            _connection.ProcessPackets();
    }
}
