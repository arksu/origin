package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.event.EventChatGeneralMessage;
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
         if (client.getActiveChar() != null) {
             // смотрим в каком канале отправили сообщение
             switch (_channelId) {
                 // основной канал - общий чат вокруг объекта
                 case 0:
                     client.getActiveChar().getPos().getGrid().broadcastEvent(new EventChatGeneralMessage(
                             client.getActiveChar(),
                             _message
                     ));
                     break;
             }
         }
    }
}
