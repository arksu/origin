package com.a4server;

/**
 * Created by arksu on 01.01.2015.
 */
public class Server
{
    // constants for the server mode
    private static final int MODE_NONE = 0;
    public static final int MODE_GAMESERVER = 1;
    public static final int MODE_LOGINSERVER = 2;

    public static int serverMode = MODE_NONE;
}
