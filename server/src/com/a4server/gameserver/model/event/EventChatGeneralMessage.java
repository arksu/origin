package com.a4server.gameserver.model.event;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.serverpackets.CreatureSay;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * сообщение в чате вокруг объекта
 * Created by arksu on 17.02.15.
 */
public class EventChatGeneralMessage extends AbstractObjectEvent
{
    private static final Logger _log = LoggerFactory.getLogger(EventChatGeneralMessage.class.getName());

    private String _message;

    public EventChatGeneralMessage(GameObject object, String message)
    {
        super(object);
        _message = message;
    }

    @Override
    public GameServerPacket getPacket()
    {
        return new CreatureSay(_object.getObjectId(), _message);
    }
}
