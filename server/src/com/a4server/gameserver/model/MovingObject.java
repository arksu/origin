package com.a4server.gameserver.model;

import com.a4server.Config;
import com.a4server.gameserver.GameTimeController;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.position.MoveController;
import com.a4server.gameserver.network.serverpackets.ObjectPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * сохранить состояние объекта в базу (позиция)
	 */
	public void storeInDb()
	{
	}

	public MoveController getMoveController()
	{
		return _moveController;
	}

	/**
	 * начать передвижение объекта
	 * @param controller контроллер движения
	 */
	public void startMove(MoveController controller)
	{
		unlinkFromAll();
		controller.setActiveObject(this);
		// сначала проверим возможно ли вообще движение?
		if (controller.canStartMoving())
		{
			// если уже стоял контроллер - возможно двигались.
			if (_moveController != null)
			{
				// сохраним состояние объекта в базу
				storeInDb();
			}
			_moveController = controller;
			_moveResult = null;
			// расскажем всем что мы начали движение, тут же отправится пакет клиенту
			getPos().getGrid().broadcastEvent(controller.getEvent());
			GameTimeController.getInstance().AddMovingObject(this);
		}
		else
		{
			if (Config.DEBUG)
			{
				_log.debug("cant start move");
			}
		}
	}

	/**
	 * прекратить движение объекта по той или иной причине
	 */
	public void stopMove(CollisionResult result, int x, int y)
	{
		_log.debug("stopMove: " + result.toString() + " at " + x + ", " + y);
		_moveController = null;
		_moveResult = result;
		getPos().setXY(x, y);
		storeInDb();
		// расскажем всем что мы остановились
		Event event = new Event(this, Event.EventType.EVT_STOP_MOVE, new ObjectPos(getObjectId(), getPos()._x, getPos()._y));
		getPos().getGrid().broadcastEvent(event);
	}

	/**
	 * прибыли в место назначения при передвижении
	 * вызывается из потока обработки передвижений
	 */
	public void onArrived()
	{
		// занулим мув контроллер, чтобы корректно завершить движение
		_moveController = null;
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
				Grid grid = World.getInstance().getGrid(gridX + i, gridY + j, getPos()._level);
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
					Grid grid = World.getInstance().getGrid(gridX + i, gridY + j, getPos()._level);
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
}