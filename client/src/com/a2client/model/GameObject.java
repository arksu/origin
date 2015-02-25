package com.a2client.model;

import com.a2client.network.game.serverpackets.ObjectAdd;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
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
    private Vector2 _coord;
    private String _name;
    private String _title;
    private Mover _mover = null;
    private BoundingBox _boundingBox;

    public GameObject(ObjectAdd pkt)
    {
        _name = pkt._name;
        _title = pkt._title;
        _coord = new Vector2(pkt._x, pkt._y);
        _objectId = pkt._objectId;
        _typeId = pkt._typeId;
        _boundingBox = new BoundingBox(new Vector3(-1, 0, -1),
                                       new Vector3(+1, 1, +1));
    }

    public int getObjectId()
    {
        return _objectId;
    }

    public int getTypeId()
    {
        return _typeId;
    }

    public Vector2 getCoord()
    {
        return _coord;
    }

    public String getName()
    {
        return _name;
    }

    public BoundingBox getBoundingBox()
    {
        return _boundingBox;
    }

    public void setCoord(int x, int y)
    {
        _coord.x = x;
        _coord.y = y;
    }

    public void setCoord(Vec2i c)
    {
        _coord.x = c.x;
        _coord.y = c.y;
    }

    public void setCoord(Vector2 c)
    {
        _coord.x = c.x;
        _coord.y = c.y;
    }


    /**
     * сервер сообщает о движении объекта
     * @param cx текущие координаты
     * @param cy текущие координаты
     * @param vx вектор движения
     * @param vy вектор движения
     */
    public void Move(int cx, int cy, int vx, int vy, int speed)
    {
        if (_mover != null)
        {
            _mover.NewMove(cx, cy, vx, vy, speed);
        }
        else
        {
            _mover = new Mover(this, cx, cy, vx, vy, speed);
        }
    }

    public void StopMove()
    {
        _mover = null;
    }

    public boolean isMoving()
    {
        return _mover != null;
    }

    public void Update()
    {
        if (_mover != null)
        {
            _mover.Update();
            if (_mover._arrived)
            {
                _mover = null;
            }
        }
    }

    @Override
    public String toString()
    {
        return "(" + _name + " " + _objectId + " type=" + _typeId + ")";
    }
}
