package com.a4server.gameserver.model.position;

import com.a4server.Config;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Human;
import com.a4server.gameserver.model.MovingObject;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
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
	private static final Logger _log = LoggerFactory.getLogger(MoveController.class.getName());

	/**
	 * объект который двигаем
	 */
	protected MovingObject _activeObject;

	/**
	 * текущие координаты объекта. double для сглаживания.
	 */
	protected double _currentX;
	protected double _currentY;

	/**
	 * последние координаты в которых было сохранение позиции в базу
	 */
	protected double _storedX;
	protected double _storedY;

	/**
	 * время последнего апдейта движения
	 */
	private long _lastMoveTime;

	/**
	 * расстояние до конечной точки при котором считаем что уже дошли куда надо
	 */
	protected static final double FINAL_DELTA = 0.5f;

	public MoveController()
	{
		_lastMoveTime = System.currentTimeMillis();
	}

	public void setActiveObject(MovingObject object)
	{
		_activeObject = object;
		if (object != null)
		{
			// получим текущие координаты
			_currentX = object.getPos().getX();
			_currentY = object.getPos().getY();
			_storedX = _currentX;
			_storedY = _currentY;
		}
	}

	protected abstract int getToX();

	protected abstract int getToY();

	/**
	 * как именно двигаемся. тип передвижения
	 */
	protected abstract Move.MoveType getMoveType();

	/**
	 * виртуальный объект для обсчета коллизий
	 */
	protected GameObject getVirtualObject()
	{
		return null;
	}

	/**
	 * создать пакет о том как движется объект
	 * @return пакет
	 */
	public abstract GameServerPacket makeMovePacket();

	/**
	 * внутренняя реализация движения. надо определить куда должны передвинутся за тик
	 * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
	 */
	public boolean movingImpl(double dt)
	{
		int toX = getToX();
		int toY = getToY();

		// вычислим единичный вектор
		double tdx = toX - _currentX;
		double tdy = toY - _currentY;
		double td = Math.sqrt(Math.pow(tdx, 2) + Math.pow(tdy, 2));
		// расстояние которое прошли за 1 тик. не более оставшегося до конечной точки
		double d = Math.min(dt * _activeObject.getMoveSpeed(), td);

		// помножим расстояние которое должны пройти на единичный вектор
		double tmpX = _currentX + (tdx / td) * d;
		double tmpY = _currentY + (tdy / td) * d;

		if (process(tmpX, tmpY, getMoveType(), getVirtualObject()))
		{
			td = Math.sqrt(Math.pow(_currentX - toX, 2) + Math.pow(_currentY - toY, 2));

			// предел расстояния до конечной точки на котором считаем что пришли куда надо
			boolean arrive = td <= FINAL_DELTA;
			// если уже дошли - остановим движение
			if (arrive)
			{
				onArrived(toX, toY);
			}
			return arrive;
		}
		// без проблем передвинутся не удалось. завершим движение
		return true;
	}

	protected void onArrived(int x, int y)
	{
		_activeObject.stopMove(CollisionResult.NONE);
	}

	/**
	 * возможно ли начать движение
	 * @return да или нет
	 */
	public boolean canStartMoving()
	{
		// COPYPAST! ^^^ movingImpl

		// время прошедшее с последнего апдейта. пока тупо захардкодим
		double dt = 0.1f;
		// вычислим единичный вектор
		double tdx = getToX() - _currentX;
		double tdy = getToY() - _currentY;
		double td = Math.sqrt(Math.pow(tdx, 2) + Math.pow(tdy, 2));
		// расстояние которое прошли за 1 тик. не более оставшегося до конечной точки
		double d = Math.min(dt * _activeObject.getMoveSpeed(), td);

		// помножим расстояние которое должны пройти на единичный вектор
		double tmpX = _currentX + (tdx / td) * d;
		double tmpY = _currentY + (tdy / td) * d;

		// проверим коллизию на передвижение за 1 тик
		return (checkColiision(tmpX, tmpY, getMoveType(), null, false) == CollisionResult.NONE);
	}

	/**
	 * обработать тик передвижения
	 * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
	 */
	public final boolean updateMove()
	{
		long currTime = System.currentTimeMillis();
		if (_lastMoveTime < currTime)
		{
			// узнаем сколько времени прошло между апдейтами
			boolean result = movingImpl((double) (currTime - _lastMoveTime) / 1000);
			// если я еще управляю объектом
			if (_activeObject.getMoveController() == this)
			{
				double dx = _currentX - _storedX;
				double dy = _currentY - _storedY;
				// если передвинулись достаточно далеко
				if (Math.pow(Config.UPDATE_DB_DISTANCE, 2) < (Math.pow(dx, 2) + Math.pow(dy, 2)))
				{
					// обновим состояние базе
					_activeObject.getPos().store();
					_storedX = _currentX;
					_storedY = _currentY;
				}
			}
			_lastMoveTime = currTime;
			return result;
		}
		return false;
	}

	/**
	 * обсчитать одну итерацию движения объекта
	 * @param toX куда пробуем передвинутся
	 * @param toY куда пробуем передвинутся
	 * @param moveType тип передвижения. идем, плывем и тд
	 * @param virtualObject виртуальный объект который может дать коллизию
	 * @return истина если все ок. ложь если не успешно
	 */
	protected boolean process(double toX,
	                          double toY,
	                          Move.MoveType moveType,
	                          GameObject virtualObject)
	{
		CollisionResult collision = checkColiision(toX, toY, moveType, virtualObject, true);
		switch (collision.getResultType())
		{
			// коллизий нет
			case COLLISION_NONE:
				// можно ставить новую позицию объекту
				_currentX = toX;
				_currentY = toY;

				// расскажем всем о том что мы передвинулись
				_activeObject.updateMove();

				// обновим видимые объекты
				if (_activeObject instanceof Human)
				{
					((Human) _activeObject).updateVisibleObjects(false);
				}
				return true;

			case COLLISION_FAIL:
				_activeObject.stopMove(collision);
				return false;

			default:
				_activeObject.stopMove(collision);
				return false;
		}

	}

	/**
	 * проверить коллизию при движении к указанной точке
	 * @param toX куда пытаемся передвинуть объект
	 * @param toY куда пытаемся передвинуть объект
	 * @param moveType тип движения
	 * @param virtualObject виртуальный объект если он есть
	 * @return вернет коллизию или null если была ошибка
	 */
	protected CollisionResult checkColiision(double toX,
	                                         double toY,
	                                         Move.MoveType moveType,
	                                         GameObject virtualObject,
	                                         boolean isMove)
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
					locked = grid.tryLockSafe(10);
					if (locked)
					// обсчитаем коллизию на это передвижение
					{
						return grid.checkCollision(_activeObject,
						                           (int) Math.round(_currentX),
						                           (int) Math.round(_currentY),
						                           (int) Math.round(toX),
						                           (int) Math.round(toY),
						                           moveType, virtualObject, isMove);
					}
					else
					{
						return CollisionResult.FAIL;
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
