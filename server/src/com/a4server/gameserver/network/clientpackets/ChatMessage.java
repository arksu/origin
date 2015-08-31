package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.network.serverpackets.CreatureSay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * игрок отправляет сообщение в чат
 * Created by arksu on 17.02.15.
 */
public class ChatMessage extends GameClientPacket
{
    private static final Logger _log = LoggerFactory.getLogger(ChatMessage.class.getName());

    private int _channelId;
    private String _message;

    @Override
    public void readImpl()
    {
        _channelId = readC();
        _message = readS();
    }

    @Override
    public void run()
    {
        if (client.getActiveChar() != null)
        {
            _log.debug("chat " + client.getActiveChar() + ": " + _message);
            // смотрим в каком канале отправили сообщение
            switch (_channelId)
            {
                // основной канал - общий чат вокруг объекта
                case 0:
                    Event event = new Event(client.getActiveChar(), Event.EventType.CHAT_GENERAL_MESSAGE, _message);
                    event.setPacket(new CreatureSay(client.getActiveChar().getObjectId(), _message));
                    client.getActiveChar().getPos().getGrid().broadcastEvent(event);
                    break;
            }
        }
    }
}
