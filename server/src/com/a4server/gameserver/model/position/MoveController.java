package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Human;
import com.a4server.gameserver.model.MoveObject;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.model.collision.VirtualObject;
import com.a4server.gameserver.model.event.AbstractObjectEvent;
import com.a4server.gameserver.model.event.EventMove;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
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

    /**
     * текущие координаты объекта. double для сглаживания.
     */
    protected double _currentX;
    protected double _currentY;

    public void setActiveObject(MoveObject object)
    {
        _activeObject = object;
        // получим текущие координаты
        _currentX = object.getPos()._x;
        _currentY = object.getPos()._y;
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
     * возможно ли начать движение
     * @return
     */
    public abstract boolean canMoving();

    /**
     * создать игровое событие о движении объекта
     * @return событие
     */
    public AbstractObjectEvent getEvent()
    {
        EventMove evt = new EventMove(_activeObject, (int) Math.round(_currentX), (int) Math.round(_currentY));
        evt.setPacket(makeMovePacket());
        return evt;
    }

    /**
     * создать пакет о том как движется объект
     * @return пакет
     */
    public abstract GameServerPacket makeMovePacket();

    /**
     * обсчитать одну итерацию движения объекта
     * @param toX куда пробуем передвинутся
     * @param toY куда пробуем передвинутся
     * @param moveType тип передвижения. идем, плывем и тд
     * @param virtualObject виртуальный объект который может дать коллизию
     * @return истина если все ок. ложь если не успешно
     */
    protected boolean Process(double toX,
                              double toY,
                              Move.MoveType moveType,
                              VirtualObject virtualObject)
    {
        CollisionResult collision = checkColiision(toX, toY, moveType, virtualObject);
        switch (collision.getResultType())
        {
            // коллизий нет
            case COLLISION_NONE:
                // можно ставить новую позицию объекту
                _activeObject.getPos().setXY(toX, toY);
                _currentX = toX;
                _currentY = toY;
                // расскажем всем о том что мы передвинулись
                _activeObject.getPos().getGrid().broadcastEvent(getEvent());

                if (_activeObject instanceof Human)
                {
                    ((Human) _activeObject).UpdateVisibleObjects(false);
                }
                return true;

            case COLLISION_OBJECT:
            case COLLISION_TILE:
            case COLLISION_WORLD:
            case COLLISION_VIRTUAL:
                _activeObject.StopMove(collision, collision.getX(), collision.getY());
                return false;

            case COLLISION_FAIL:
                _activeObject.StopMove(collision, (int) Math.round(_currentX),
                                       (int) Math.round(_currentY));
                return false;

            default:
                // возможно какая то ошибка зарылась
                _log.error(this.getClass().getSimpleName() + " process error, unknown collision " +
                                   collision.toString());

                _activeObject.StopMove(CollisionResult.FAIL, (int) Math.round(_currentX),
                                       (int) Math.round(_currentY));
                return false;
        }

    }

    protected CollisionResult checkColiision(double toX,
                                             double toY,
                                             Move.MoveType moveType,
                                             VirtualObject virtualObject)
    {
        Grid grid = _activeObject.getPos().getGrid();
        // а теперь пошла самая магия!)))
        if (grid != null)
        {
            try
            {
                boolean locked = false;
                try
                {
                    // пробуем залочить грид, ждем всего 10 мс
                    locked = grid.tryLock(10);
                    // обсчитаем коллизию на это передвижение
                    return grid.checkCollision(_activeObject,
                                               (int) Math.round(_currentX),
                                               (int) Math.round(_currentY),
                                               (int) Math.round(toX),
                                               (int) Math.round(toY),
                                               moveType, virtualObject);
                }
                finally
                {
                    // полюбому надо разблокировать грид
                    if (locked)
                    {
                        grid.unlock();
                    }
                }
            }
            catch (Grid.GridLoadException e)
            {
                _log.warn("checkColiision: GridLoadException " + e.getMessage() + " " + _activeObject
                        .toString() + "; " + toString());
                return CollisionResult.FAIL;
            }
            catch (InterruptedException e)
            {
                _log.warn("checkColiision: cant lock grid " + _activeObject.toString() + "; " + toString());
                return CollisionResult.FAIL;
            }
        }
        else
        {
            _log.warn("checkColiision: grid is null! " + _activeObject.toString() + "; " + toString());
            return CollisionResult.FAIL;
        }
    }
}
