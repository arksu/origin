package com.a4server.gameserver.idfactory;

import com.a4server.gameserver.GlobalVariablesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * тупо инкрементим ид
 * но "кешируем", берем CAPACITY инкрементим их и запоминаем. при этом в базу говорим что последний ид уже
 * lastid + CAPACITY, т.е. в базе храним заведомо больший ид чем выдаем сейчас
 * как только выдадим все из диапазона - увеличим последний ид в базе на CAPACITY
 * таким образом обновляем базу не каждый раз при получении ид а лишь раз в CAPACITY раз
 * Created by arksu on 03.01.2015.
 */
public class SimpleIdFactory extends IdFactory
{
    private static final Logger _log = LoggerFactory.getLogger(SimpleIdFactory.class.getName());
    /**
     * сколько свободных ид брать за раз
     */
    private static final int CAPACITY = 10;

    private static final String DB_VALUE = "next_free_id";

    private int _lastId;
    private int _freeCount;

    public SimpleIdFactory()
    {
        _freeCount = 0;
        // загрузим последний использовавшийся ид
        _lastId = GlobalVariablesManager.getInstance().getVarInt(DB_VALUE);
        if (_lastId == -1)
            _lastId = 100;
        Extend();

        _initialized = true;
    }

    @Override
    synchronized public int getNextId()
    {
        _freeCount--;
        _lastId++;
        if (_freeCount <= 0)
        {
            Extend();
        }
        return _lastId;
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


    /**
     * взять следующий диапазон свободных ид и обновить запись в базе
     */
    protected void Extend()
    {
        _log.debug("take id's from " + (_lastId+1) + " to " + (_lastId + CAPACITY));
        _freeCount += CAPACITY;
        GlobalVariablesManager.getInstance().saveVarInt(DB_VALUE, _lastId + CAPACITY);
    }
}
