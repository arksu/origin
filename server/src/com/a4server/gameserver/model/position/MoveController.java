package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.MoveObject;

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
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

    /**
     * находится ли объект в реально движении или стоит на месте
     * @return движется ли?
     */
    public abstract boolean isMoving();

    /**
     * достигли точки назначения, больше работа контроллера не требуется. он никогда не получит updateMove
     * @return достигли?
     */
    public abstract boolean isArrived();
}
