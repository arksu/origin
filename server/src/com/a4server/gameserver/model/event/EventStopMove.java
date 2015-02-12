package com.a4server.gameserver.model.event;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.serverpackets.ObjectPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * событие: объект остановился
 * Created by arksu on 12.02.15.
 */
public class EventStopMove extends AbstractObjectEvent
{
    private static final Logger _log = LoggerFactory.getLogger(EventStopMove.class.getName());

    public EventStopMove(GameObject object)
    {
        super(object);
        _packet = new ObjectPos(object.getObjectId(), object.getPos()._x, object.getPos()._y);
    }
}
