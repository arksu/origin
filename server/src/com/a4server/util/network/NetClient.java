package com.a4server.util.network;

/**
 * Created by arksu on 03.01.2015.
 */
public abstract class NetClient
{
    public abstract void ProcessPacket() throws InterruptedException;

    public abstract void onDisconnect();
}
