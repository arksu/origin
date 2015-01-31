package com.a4server.gameserver.model;

/**
 * Created by arksu on 09.01.2015.
 */
public abstract class MoveController
{
    protected MoveObject _activeObject;

    /**
     * обработать тик передвижения
     * @return объект в который уперлись
     */
    public abstract GameObject updateMove();

}
