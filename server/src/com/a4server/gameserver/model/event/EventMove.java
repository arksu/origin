package com.a4server.gameserver.model.event;

import com.a4server.gameserver.model.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * событие о том что объект передвинулся. находится в движении
 * Created by arksu on 11.02.15.
 */
public class EventMove extends AbstractObjectEvent
{
    private static final Logger _log = LoggerFactory.getLogger(EventMove.class.getName());

    /**
     * текущие (новые) координаты объекта
     */
    private int _cx;
    private int _cy;
    
    public EventMove(GameObject object, int cx, int cy)
    {
        super(object);
        _cx = cx;
        _cy = cy;
    }
}
