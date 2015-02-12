package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * движение объекта к заданной точке на карте
 * Created by arksu on 08.02.15.
 */
public class MoveToPoint extends MoveController
{
    protected static final Logger _log = LoggerFactory.getLogger(MoveToPoint.class.getName());

    /**
     * куда движемся
     */
    private int _toX;
    private int _toY;

    /**
     * как именно двигаемся. тип передвижения
     */
    private Move.MoveType _moveType = Move.MoveType.MOVE_WALK;

    /**
     * расстояние до конечной точки при котором считаем что уже дошли куда надо
     */
    private static final double FINAL_DELTA = 0.5f;

    public MoveToPoint(int x, int y)
    {
        _toX = x;
        _toY = y;
    }

    /**
     * обработать тик движения
     * @return истина если движение завершилось. ложь если еще надо обновлять
     */
    @Override
    public boolean updateMove()
    {
        // время прошедшее с последнего апдейта. пока тупо захардкодим
        double dt = 0.1f;
        // вычислим единичный вектор
        double tdx = _toX - _currentX;
        double tdy = _toY - _currentY;
        double td = Math.sqrt(Math.pow(tdx, 2) + Math.pow(tdy, 2));
        // расстояние которое прошли за 1 тик. не более оставшегося до конечной точки
        double d = Math.min(dt * _activeObject.getSpeed(), td);

        // помножим расстояние которое должны пройти на единичный вектор
        double tmpX = _currentX + (tdx / td) * d;
        double tmpY = _currentY + (tdy / td) * d;

        if (Process(tmpX, tmpY, _moveType, null))
        {

            td = Math.sqrt(Math.pow(_currentX - _toX, 2) + Math.pow(_currentY - _toY, 2));
            _log.debug("td=" + Double.toString(td));

            // предел расстояния до конечной точки на котором считаем что пришли куда надо
            boolean arrive = td <= FINAL_DELTA;
            // если уже дошли - остановим движение
            if (arrive)
            {
                _activeObject.StopMove(CollisionResult.NONE, _toX, _toY);
            }
            return arrive;
        }
        // без проблем передвинутся не удалось. завершим движение
        return true;
    }

    @Override
    public boolean isMoving()
    {
        double td = Math.sqrt(Math.pow(_currentX - _toX, 2) + Math.pow(_currentY - _toY, 2));
        return td <= FINAL_DELTA;
    }

    @Override
    public boolean canMoving()
    {
        // COPYPAST! ^^^
        
        // время прошедшее с последнего апдейта. пока тупо захардкодим
        double dt = 0.1f;
        // вычислим единичный вектор
        double tdx = _toX - _currentX;
        double tdy = _toY - _currentY;
        double td = Math.sqrt(Math.pow(tdx, 2) + Math.pow(tdy, 2));
        // расстояние которое прошли за 1 тик. не более оставшегося до конечной точки
        double d = Math.min(dt * _activeObject.getSpeed(), td);

        // помножим расстояние которое должны пройти на единичный вектор
        double tmpX = _currentX + (tdx / td) * d;
        double tmpY = _currentY + (tdy / td) * d;

        return (checkColiision(tmpX,tmpY, _moveType, null) == CollisionResult.NONE);
    }

    /**
     * создать пакет о передвижении объекта
     * @return пакет
     */
    @Override
    public GameServerPacket makeMovePacket()
    {
        return new ObjectMove(_activeObject.getObjectId(),
                              _activeObject.getPos()._x,
                              _activeObject.getPos()._y,
                              _toX,
                              _toY
        );
    }

}
