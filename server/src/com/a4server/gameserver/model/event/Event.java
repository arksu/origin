package com.a4server.gameserver.model.event;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;

/**
 * базовое игровое событие
 * к событию может быть прикреплен пакет. тогда он будет разослан всем клиентам которые обработали событие
 * Created by arksu on 11.02.15.
 */
public class Event
{
    public static final int MOVE = 1;
    public static final int STOP_MOVE = 2;
    public static final int CHAT_GENERAL_MESSAGE = 3;

    /**
     * объект который сгенерировал событие
     */
    protected final GameObject _object;

    /**
     * тип сообщения
     */
    protected final int _type;

    /**
     * дополнительная информация о событии
     */
    protected Object _extraInfo = null;

    /**
     * пакет прикрепленный к событию. если другой объект обработал это событие
     * то пакет будет отослан ему
     */
    protected GameServerPacket _packet = null;

    public Event(GameObject object, int type)
    {
        _object = object;
        _type = type;
    }

    public GameObject getObject()
    {
        return _object;
    }

    public int getType()
    {
        return _type;
    }

    public GameServerPacket getPacket()
    {
        return _packet;
    }

    public void setPacket(GameServerPacket pkt)
    {
        _packet = pkt;
    }

    public Object getExtraInfo()
    {
        return _extraInfo;
    }

    public void setExtraInfo(Object extraInfo)
    {
        _extraInfo = extraInfo;
    }

}
