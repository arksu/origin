package com.a4server.gameserver.model;

/**
 * Created by arksu on 09.01.2015.
 * объект который может передвигаться в мире
 */
public abstract class MoveObject extends GameObject
{
    MoveController _moveController = null;

    public MoveObject(int objectId)
    {
        super(objectId);
    }

    public abstract int getSpeed();
}
