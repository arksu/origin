package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * движение объекта к заданной точке на карте 
 * Created by arksu on 08.02.15.
 */
public class MoveToPoint extends MoveController
{
    protected static final Logger _log = LoggerFactory.getLogger(MoveToPoint.class.getName());
    
    private int _toX;
    private int _toY;
    
    public MoveToPoint(int x, int y) {
        _toX = x;
        _toY = y;
    }

    @Override
    public GameObject updateMove()
    {
        return null;
    }
}
