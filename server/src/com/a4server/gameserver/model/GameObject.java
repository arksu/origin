package com.a4server.gameserver.model;

import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectAdd;
import com.a4server.gameserver.network.serverpackets.ObjectRemove;
import com.a4server.util.Rect;

/**
 * игровой объект
 */
public abstract class GameObject
{
    /**
     * позиция объекта в мире
     */
    protected ObjectPosition _pos;

    /**
     * тип объекта
     */
    protected int _typeId;

    /**
     * размеры объекта
     */
    private int _width;
    private int _height;
    private Rect _boundRect;

    /**
     * ид объекта
     */
    protected int _objectId;

    /**
     * имя которое отображается над объектом
     */
    protected String _name = "";

    /**
     * подпись над объектом
     */
    protected String _title = "";


    public GameObject(int objectId) {
        _objectId = objectId;
        _width = 10;
        _height = 10;
        _boundRect = new Rect(-_width/2,-_height/2,_width/2,_height/2);
    }

    public int getObjectId() {
        return _objectId;
    }
    
    public int getTypeId() {
        return _typeId;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public String getTitle() {
        return _title;
    }

    public ObjectPosition getPos() {
        return _pos;
    }

    public Rect getBoundRect()
    {
        return _boundRect;
    }

    /**
     * создать пакет для отсылки другим игрокам
     * @return пакет
     */
    public GameServerPacket makeAddPacket() {
        return new ObjectAdd(this);
    }

    /**
     * создать пакет об удалении объекта из мира
     * @return пакет
     */
    public GameServerPacket makeRemovePacket() {
        return new ObjectRemove(this._objectId);
    }

}
