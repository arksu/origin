package com.a4server.gameserver.model;

import com.a4server.gameserver.model.position.ObjectPosition;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectAdd;
import com.a4server.gameserver.network.serverpackets.ObjectRemove;
import com.a4server.util.Rect;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * игровой объект
 */
public class GameObject
{
    /**
     * ид объекта, задается лишь единожды
     */
    protected final int _objectId;

    /**
     * тип объекта
     */
    protected int _typeId;

    /**
     * позиция объекта в мире
     */
    protected ObjectPosition _pos;

    /**
     * размеры объекта
     */
    private int _width;
    private int _height;
    private Rect _boundRect;

    /**
     * имя которое отображается над объектом
     */
    protected String _name = "";

    /**
     * подпись над объектом
     */
    protected String _title = "";


    public GameObject(int objectId)
    {
        if (objectId == 0)
        {
            throw new RuntimeException("objectId can not be zero");
        }
        _objectId = objectId;
        _width = 10;
        _height = 10;
        _boundRect = new Rect(-_width / 2, -_height / 2, _width / 2, _height / 2);
    }

    public GameObject(Grid grid, ResultSet rset) throws SQLException
    {
        _objectId = rset.getInt("id");
        _typeId = rset.getInt("type");
        _pos = new ObjectPosition(rset.getInt("x"), rset.getInt("y"), grid.getLevel());
        // заполним дефолтными абстрактными данными
        _width = 10;
        _height = 10;
        _boundRect = new Rect(-_width / 2, -_height / 2, _width / 2, _height / 2);
    }

    public int getObjectId()
    {
        return _objectId;
    }

    public int getTypeId()
    {
        return _typeId;
    }

    public String getName()
    {
        return _name;
    }

    public String getTitle()
    {
        return _title;
    }

    public ObjectPosition getPos()
    {
        return _pos;
    }

    public Rect getBoundRect()
    {
        return _boundRect;
    }

    /**
     * создать пакет о добавлении меня в мир
     * @return пакет
     */
    public GameServerPacket makeAddPacket()
    {
        return new ObjectAdd(this);
    }

    /**
     * создать пакет об удалении объекта из мира
     * @return пакет
     */
    public GameServerPacket makeRemovePacket()
    {
        return new ObjectRemove(this._objectId);
    }

    @Override
    public String toString()
    {
        return "(" + getClass().getSimpleName() + ": " + (!_name
                .isEmpty() ? _name + " " : "") + "id=" + _objectId + ")";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof GameObject)
        {
            return ((GameObject) obj)._objectId == _objectId;
        }
        return super.equals(obj);
    }
}
