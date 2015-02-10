package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.MoveObject;
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

        if (Update(tmpX, tmpY, Move.MoveType.MOVE_WALK, null))
        {
            
            td = Math.sqrt(Math.pow(_currentX - _toX, 2) + Math.pow(_currentY - _toY, 2));
            _log.debug("td="+Double.toString(td));

            // предел расстояния до конечной точки на котором считаем что пришли куда надо
            return td <= 0.5f;
        }
        return false;
    }

    @Override
    public boolean isMoving()
    {
        return false;
    }

}
