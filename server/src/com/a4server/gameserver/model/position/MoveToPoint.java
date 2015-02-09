package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.MoveObject;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
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

    /**
     * текущие координаты объекта. double для сглаживания.
     */
    private double _currentX;
    private double _currentY;

    private Move.MoveType _moveType = Move.MoveType.MOVE_WALK;

    public MoveToPoint(int x, int y)
    {
        _toX = x;
        _toY = y;
    }

    public void setActiveObject(MoveObject object)
    {
        super.setActiveObject(object);

        // получим текущие координаты
        _currentX = object.getPos()._x;
        _currentY = object.getPos()._y;
    }

    @Override
    public boolean updateMove()
    {
        // время прошедшее с последнего апдейта. пока тупо захардкодим
        double dt = 0.1f;
        // расстояние которое прошли
        double d = dt * _activeObject.getSpeed();
        // вычислим единичный вектор
        double tdx = _toX - _currentX;
        double tdy = _toY - _currentY;
        double td = Math.sqrt(Math.pow(tdx, 2) + Math.pow(tdy, 2));

        // помножим расстояние которое должны пройти на единичный вектор
        double tmpX = _currentX + (tdx / td) * d;
        double tmpY = _currentY + (tdy / td) * d;

        Grid grid = _activeObject.getPos().getGrid();
        if (grid != null)
        {
            // а теперь пошла самая магия!)))
            try
            {
                try
                {
                    // пробуем залочить грид
                    grid.tryLock(Grid.MAX_WAIT_LOCK);
                    // обсчитаем коллизию на это передвижение
                    CollisionResult collision = grid.checkCollision(_activeObject,
                                                                    (int) Math.round(_currentX),
                                                                    (int) Math.round(_currentY),
                                                                    (int) Math.round(tmpX),
                                                                    (int) Math.round(tmpY),
                                                                    _moveType, null);
                    switch (collision.getResultType())
                    {
                        // коллизий нет
                        case COLLISION_NONE:
                            // можно ставить новую позицию объекту
                            _activeObject.getPos().setXY(tmpX, tmpY);
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
            catch (Exception e)
            {
                _log.warn("Failed to check collision " + _activeObject.toString() + "; " + toString());
                _activeObject.StopMove();
                return true;
            }

            td = Math.sqrt(Math.pow(_currentX - _toX, 2) + Math.pow(_currentY - _toY, 2));

            // предел расстояния до конечной точки на котором считаем что пришли куда надо
            return td <= 1;
        }
        else
        {
            _log.warn("updateMove: grid is null! " + _activeObject.toString() + "; " + toString());
            return true;
        }
    }

    @Override
    public boolean isMoving()
    {
        return false;
    }

}
