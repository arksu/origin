package com.a4server.gameserver.idfactory;

/**
 * Created by arksu on 03.01.2015.
 */
public class SimpleIdFactory extends IdFactory
{
    private int _lastId;

    public SimpleIdFactory()
    {
        // загрузим последний использовавшийся ид
        // todo
        _lastId = 0;

        _initialized = true;
    }

    @Override
    public int getNextId()
    {
        return 0;
    }

    @Override
    public void releaseId(int id)
    {

    }

    @Override
    public int size()
    {
        return 0;
    }
}
