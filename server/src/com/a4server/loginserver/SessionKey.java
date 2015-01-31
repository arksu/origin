package com.a4server.loginserver;

/**
 * Created by arksu on 03.01.2015.
 */
public class SessionKey
{
    public int playOkID1;
    public int playOkID2;

    public SessionKey(int playOK1, int playOK2)
    {
        playOkID1 = playOK1;
        playOkID2 = playOK2;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof SessionKey))
        {
            return false;
        }
        final SessionKey key = (SessionKey) o;

        return ((playOkID1 == key.playOkID1) && (playOkID2 == key.playOkID2));
    }
}
