package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.MoveObject;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.model.collision.VirtualObject;
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
    
    public abstract GameServerPacket makeMovePacket();

    /**
     * обсчитать одну итерацию движения объекта
     * @param toX куда пробуем передвинутся
     * @param toY куда пробуем передвинутся
     * @param moveType тип передвижения. идем, плывем и тд
     * @param virtualObject виртуальный объект который может дать коллизию
     * @return истина если все ок. ложь если не успешно
     */
    protected boolean Update(double toX,
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
                boolean locked = false;
                try
                {
                    // пробуем залочить грид, ждем всего 10 мс
                    locked = grid.tryLock(10);
                    // обсчитаем коллизию на это передвижение
                    CollisionResult collision = grid.checkCollision(_activeObject,
                                                                    (int) Math.round(_currentX),
                                                                    (int) Math.round(_currentY),
                                                                    (int) Math.round(toX),
                                                                    (int) Math.round(toY),
                                                                    moveType, virtualObject);
                    switch (collision.getResultType())
                    {
                        // коллизий нет
                        case COLLISION_NONE:
                            // можно ставить новую позицию объекту
                            _activeObject.getPos().setXY(toX, toY);
                            _currentX = toX;
                            _currentY = toY;
                            // разошлем всем пакет о том что объект передвинулся
                            _activeObject.getPos().getGrid().broadcastPacket(makeMovePacket());
                            return true;

                        case COLLISION_OBJECT:
                        case COLLISION_TILE:
                        case COLLISION_WORLD:
                        case COLLISION_VIRTUAL:
                            _activeObject.StopMove(collision, collision.getX(), collision.getY());
                            return false;

                        case COLLISION_FAIL:
                            _activeObject.StopMove(CollisionResult.FAIL, (int) Math.round(_currentX),
                                                   (int) Math.round(_currentY));
                            return false;

                        default:
                            // возможно какая то ошибка зарылась
                            _log.error(this.getClass().getSimpleName() + " update error, unknown collision " +
                                               collision.toString());

                            _activeObject.StopMove(CollisionResult.FAIL, (int) Math.round(_currentX),
                                                   (int) Math.round(_currentY));
                            return false;
                    }
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
                _log.warn("updateMove: GridLoadException " + e.getMessage() + " " + _activeObject
                        .toString() + "; " + toString());
                _activeObject.StopMove(CollisionResult.FAIL, (int) Math.round(_currentX),
                                       (int) Math.round(_currentY));
                return false;
            }
            catch (InterruptedException e)
            {
                _log.warn("updateMove: cant lock grid " + _activeObject.toString() + "; " + toString());
                _activeObject.StopMove(CollisionResult.FAIL, (int) Math.round(_currentX), (int) Math.round(_currentY));
                return false;
            }
        }
        else
        {
            _log.warn("updateMove: grid is null! " + _activeObject.toString() + "; " + toString());
            return false;
        }

    }
}
