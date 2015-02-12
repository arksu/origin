package com.a2client.model;

import com.a2client.network.game.serverpackets.ObjectAdd;
import com.a2client.util.Vec2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * базовый игровой объект
 * Created by arksu on 06.02.15.
 */
public class GameObject
{
    protected static final Logger _log = LoggerFactory.getLogger(GameObject.class.getName());

    private int _objectId;
    private int _typeId;
    private Vec2i _coord;
    private String _name;
    private String _title;
    private Mover _mover = null;

    public GameObject(ObjectAdd pkt)
    {
        _name = pkt._name;
        _title = pkt._title;
        _coord = new Vec2i(pkt._x, pkt._y);
        _objectId = pkt._objectId;
        _typeId = pkt._typeId;
    }

    public int getObjectId()
    {
        return _objectId;
    }

    public int getTypeId()
    {
        return _typeId;
    }

    public Vec2i getCoord()
    {
        return _coord;
    }

    public void setCoord(int x, int y)
    {
        _coord.x = x;
        _coord.y = y;
    }

    /**
     * сервер сообщает о движении объекта
     * @param cx текущие координаты
     * @param cy текущие координаты
     * @param vx вектор движения
     * @param vy вектор движения
     */
    public void Move(int cx, int cy, int vx, int vy)
    {
        if (_mover != null)
        {
            _mover.UpdateMove(cx, cy, vx, vy);
        }
        else
        {
            _mover = new Mover(this, cx, cy, vx, vy);
        }
    }

    public boolean isMoving()
    {
        return _mover != null;
    }

    public void Update()
    {
        if (_mover != null) {
            _mover.Update();
        }
    }
}
