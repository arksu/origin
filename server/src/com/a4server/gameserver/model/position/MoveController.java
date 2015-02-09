package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.MoveObject;

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
 * Created by arksu on 09.01.2015.
 */
public abstract class MoveController
{
    protected MoveObject _activeObject;

    public void setActiveObject(MoveObject object)
    {
        _activeObject = object;
    }

    /**
     * обработать тик передвижения
     * @return движение завершено? (уперлись во чтото или прибыли в пункт назначения)
     */
    public abstract boolean updateMove();

    /**
     * находится ли объект в реально движении или стоит на месте
     * @return движется ли?
     */
    public abstract boolean isMoving();
}
