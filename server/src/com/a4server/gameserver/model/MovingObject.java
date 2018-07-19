package com.a4server.gameserver.model;

import com.a4server.Config;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.GridEvent;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.MoveController;
import com.a4server.gameserver.network.packets.serverpackets.ObjectPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_OBJECT;
import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_VIRTUAL;

/**
 * объект который может передвигаться в мире
 * Created by arksu on 09.01.2015.
 */
public abstract class MovingObject extends GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(MovingObject.class.getName());

	/**
	 * контроллер который управляет передвижением объекта
	 */
	protected MoveController _moveController = null;

	/**
	 * результат передвижения
	 */
	protected CollisionResult _moveResult = null;

	/**
	 * объект с которым "столкнулись" (прилинковались), может быть виртуальный или реальный
	 * если реальный, то при удалении его из known списка должны занулить и здесь.
	 * то есть это реальный объект с которым мы взаимодействуем
	 */
	protected GameObject _linkedObject = null;

	/**
	 * список гридов в которых находится объект. 9 штук.
	 */
	protected List<Grid> _grids = new ArrayList<>();

	public MovingObject(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	/**
	 * получить скорость объекта
	 * @return скорость в единицах координат в секунду
	 */
	public abstract double getMoveSpeed();

	public MoveController getMoveController()
	{
		return _moveController;
	}

	/**
	 * начать передвижение объекта
	 * @param controller контроллер движения
	 * @return результат коллизии если вдруг не началось движение можно проверить куда упираемся
	 */
	public CollisionResult startMove(MoveController controller)
	{
		controller.setActiveObject(this);
		// сначала проверим возможно ли вообще движение?
		CollisionResult startCollision = controller.checkStartCollision();
		if (startCollision == CollisionResult.NONE)
		{
			unlinkFromAll();
			// если уже стоял контроллер - возможно двигались.
			if (_moveController != null)
			{
				// обновим позицию в базе
				getPos().store();
			}
			_moveController = controller;
			_moveResult = null;
			if (_linkedObject != null)
			{
				clearLinkedObject();
			}
			// расскажем всем что мы начали движение, тут же отправится пакет клиенту
			GridEvent gridEvent = new GridEvent(
					this,
					GridEvent.EventType.EVT_START_MOVE,
					_moveController.makeMovePacket()
			);
			getPos().getGrid().broadcastEvent(gridEvent);
			GameTimeController.getInstance().addMovingObject(this);
		}
		else
		{
			if (_moveController != null)
			{
				stopMove(startCollision);
			}
			if (Config.DEBUG)
			{
				_log.debug("cant start move");
			}
		}
		return startCollision;
	}

	/**
	 * очередное обновление позиции в движении
	 */
	public void updateMove()
	{
		GridEvent gridEvent = new GridEvent(
				this,
				GridEvent.EventType.EVT_MOVE,
				_moveController.makeMovePacket()
		);
		getPos().getGrid().broadcastEvent(gridEvent);
	}

	/**
	 * прекратить движение объекта по той или иной причине
	 */
	public void stopMove(CollisionResult result)
	{
		_log.debug("stopMove: " + result.toString() + " at " + getPos());
		_moveController = null;
		_moveResult = result;
		if (_moveResult.getResultType() == COLLISION_OBJECT ||
		    _moveResult.getResultType() == COLLISION_VIRTUAL)
		{
			_linkedObject = _moveResult.getObject();
			_log.debug("set linkedObject: " + _linkedObject);
		}

		getPos().store();
		// расскажем всем что мы остановились
		GridEvent gridEvent = new GridEvent(this, GridEvent.EventType.EVT_STOP_MOVE, new ObjectPos(getObjectId(), getPos().getX(), getPos().getY()));
		getPos().getGrid().broadcastEvent(gridEvent);
	}

	/**
	 * прибыли в место назначения при передвижении
	 * вызывается из потока обработки передвижений
	 */
	protected void onArrived()
	{
		// занулим мув контроллер, чтобы корректно завершить движение
		_moveController = null;
	}

	/**
	 * @return True if the movement is finished
	 */
	public boolean updatePosition()
	{
		// получим контроллера
		MoveController controller = getMoveController();
		if (controller != null)
		{
			// обновим и узнаем закончено ли движение?
			if (controller.updateMove())
			{
				// скажем объекту что он дошел куда надо
				onArrived();
				return true;
			}
			return false;
		}
		else
		{
			// ошибка. объект в списке передвижения но контроллера у него нет.
			throw new RuntimeException("updatePosition, object havn't move controller! " + toString());
		}
	}

	public List<Grid> getGrids()
	{
		return _grids;
	}

	/**
	 * получить окружающие гриды и дождаться их загрузки
	 */
	public void loadGrids() throws Exception
	{
		_grids.clear();
		int gridX = getPos().getGridX();
		int gridY = getPos().getGridY();
		for (int i = -1; i <= 1; i++)
		{
			for (int j = -1; j <= 1; j++)
			{
				Grid grid = World.getInstance().getGrid(gridX + i, gridY + j, getPos().getLevel());
				if (grid != null)
				{
					_grids.add(grid);
				}
			}
		}

		for (Grid grid : _grids)
		{
			grid.waitLoad();
		}
	}

	/**
	 * все нужные гриды реально загружены
	 * @return загружены все?
	 */
	public boolean isGridsLoaded()
	{
		for (Grid g : _grids)
		{
			// если хоть 1 не готов
			if (!g.isLoaded())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * изменился грид в котором находимся. надо отреагировать
	 */
	public void onGridChanged()
	{
		try
		{
			// надо обновить список гридов
			ArrayList<Grid> newList = new ArrayList<>();
			int gridX = getPos().getGridX();
			int gridY = getPos().getGridY();

			for (int i = -1; i <= 1; i++)
			{
				for (int j = -1; j <= 1; j++)
				{
					Grid grid = World.getInstance().getGrid(gridX + i, gridY + j, getPos().getLevel());
					// если грид существует
					if (grid != null)
					{
						// только новые гриды в которые мы входим
						if (!_grids.contains(grid))
						{
							_grids.add(grid);
							grid.waitLoad();
							onEnterGrid(grid);
						}
						newList.add(grid);
					}
				}
			}
			// старые гриды деактивируем
			if (!newList.isEmpty())
			{
				for (Grid grid : _grids)
				{
					if (!newList.contains(grid))
					{
						onLeaveGrid(grid);
					}
				}
			}
			_grids = newList;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.error("onGridChanged failed " + this.toString());
		}
	}

	/**
	 * входим в новый для объекта грид, нужно отреагировать
	 * @param grid грид
	 */
	protected void onEnterGrid(Grid grid)
	{
	}

	/**
	 * покидаем грид
	 * @param grid грид
	 */
	protected void onLeaveGrid(Grid grid)
	{
	}

	public GameObject getLinkedObject()
	{
		return _linkedObject;
	}

	public void clearLinkedObject()
	{
		_log.debug("clearLinkedObject");
		_linkedObject = null;
	}

	@Override
	public void unlink(GameObject other)
	{
		if (other != null && other == _linkedObject)
		{
			clearLinkedObject();
		}
		super.unlink(other);
	}
}