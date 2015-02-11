package com.a4server.gameserver.model.event;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;

/**
 * базовое игровое событие
 * к событию может быть прикреплен пакет. тогда он будет разослан всем клиентам которые обработали событие
 * Created by arksu on 11.02.15.
 */
public class AbstractObjectEvent
{
    /**
     * объект который сгенерировал событие 
     */
    protected final GameObject _object;

    /**
     * пакет прикрепленный к событию. если другой объект обработал это событие
     * то пакет будет отослан ему
     */
    protected GameServerPacket _packet = null;

    public AbstractObjectEvent(GameObject object)
    {
        _object = object;
    }

    public GameObject getObject()
    {
        return _object;
    }

    public void setPacket(GameServerPacket pkt)
    {
        _packet = pkt;
    }

    public GameServerPacket getPacket()
    {
        return _packet;
    }
}
