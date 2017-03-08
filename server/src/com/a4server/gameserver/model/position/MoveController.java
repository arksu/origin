package com.a4server.gameserver.model.position;

import com.a4server.Config;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Human;
import com.a4server.gameserver.model.MovingObject;
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
			_currentX = object.getPos()._x;
			_currentY = object.getPos()._y;
			_storedX = _currentX;
			_storedY = _currentY;
		}
	}

	/**
	 * находится ли объект в реально движении или стоит на месте
	 * @return движется ли?
	 */
	public abstract boolean isMoving();

	/**
	 * возможно ли начать движение
	 * @return да или нет
	 */
	public abstract boolean canStartMoving();

	/**
	 * создать пакет о том как движется объект
	 * @return пакет
	 */
	public abstract GameServerPacket makeMovePacket();

	/**
	 * внутренняя реализация движения. надо определить куда должны передвинутся за тик
	 * @return движение завершено? (истина ежели уперлись во чтото или прибыли в пункт назначения)
	 */
	public abstract boolean movingImpl(double dt);

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
					_activeObject.storeInDb();
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
				_activeObject.updateMove();

				// обновим видимые объекты
				if (_activeObject instanceof Human)
				{
					((Human) _activeObject).updateVisibleObjects(false);
				}
				return true;

			case COLLISION_FAIL:
				_activeObject.stopMove(
						collision,
						(int) Math.round(_currentX),
						(int) Math.round(_currentY));
				return false;

			default:
				_activeObject.stopMove(
						collision,
						collision.getX(),
						collision.getY());
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
					locked = grid.tryLockSafe(10);
					if (locked)
					// обсчитаем коллизию на это передвижение
					{
						return grid.checkCollision(_activeObject,
						                           (int) Math.round(_currentX),
						                           (int) Math.round(_currentY),
						                           (int) Math.round(toX),
						                           (int) Math.round(toY),
						                           moveType, virtualObject, true);
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
