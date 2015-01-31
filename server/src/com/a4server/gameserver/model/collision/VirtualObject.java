package com.a4server.gameserver.model.collision;

import com.a4server.gameserver.model.GameObject;
import com.a4server.util.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 07.01.2015.
 */
public class VirtualObject
{
    protected static final Logger _log = LoggerFactory.getLogger(VirtualObject.class.getName());

    private int _x, _y;
    private GameObject _type;

    public VirtualObject(int _x, int _y, GameObject _type)
    {
        this._x = _x;
        this._y = _y;
        this._type = _type;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    public Rect getBoundRect() {
        return _type.getBoundRect();
    }

    public GameObject getObject()
    {
        return _type;
    }
}
