package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.MoveObject;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.model.collision.VirtualObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * реализует передвижения объектов
 * расчитывает новую позицию. ставит ее объекту и уведомляет всех о смене позиции
 * Created by arksu on 09.01.2015.
 */
public abstract class MoveController
{
    protected static final Logger _log = LoggerFactory.getLogger(MoveController.class.getName());

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

    /**
     * обсчитать одну итерацию движения объекта
     * @param fromX откуда
     * @param fromY откуда
     * @param toX куда пробуем передвинутся
     * @param toY куда пробуем передвинутся
     * @param moveType тип передвижения. идем, плывем и тд
     * @param virtualObject виртуальный объект который может дать коллизию
     * @return истину если все ок. ложь если не успешно
     */
    protected boolean Update(double fromX,
                             double fromY,
                             double toX,
                             double toY,
                             Move.MoveType moveType,
                             VirtualObject virtualObject)
    {
        Grid grid = _activeObject.getPos().getGrid();
        if (grid != null)
        {
            // а теперь пошла самая магия!)))
            try
            {
                try
                {
                    // пробуем залочить грид, ждем всего 10 мс
                    grid.tryLock(10);
                    // обсчитаем коллизию на это передвижение
                    CollisionResult collision = grid.checkCollision(_activeObject,
                                                                    (int) Math.round(fromX),
                                                                    (int) Math.round(fromY),
                                                                    (int) Math.round(toX),
                                                                    (int) Math.round(toY),
                                                                    moveType, virtualObject);
                    switch (collision.getResultType())
                    {
                        // todo : еще коллизий при движении
                        // коллизий нет
                        case COLLISION_NONE:
                            // можно ставить новую позицию объекту
                            _activeObject.getPos().setXY(toX, toY);
                            break;
                        default:
                            // остальные варианты пока не учитываем. но тут будет очень много всего ))
                            _activeObject.StopMove();
                            return true;
                    }
                }
                finally
                {
                    // полюбому надо разблокировать грид
                    grid.unlock();
                }
            }
            catch (Grid.GridLoadException e)
            {
                _log.warn("updateMove: GridLoadException " + e.getMessage() + " " + _activeObject
                        .toString() + "; " + toString());
                _activeObject.StopMove();
                return true;
            }
            catch (InterruptedException e)
            {
                _log.warn("updateMove:  cant lock grid " + _activeObject.toString() + "; " + toString());
                _activeObject.StopMove();
                return true;
            }

            return true;
        }
        else
        {
            _log.warn("updateMove: grid is null! " + _activeObject.toString() + "; " + toString());
            return true;
        }

    }
}
