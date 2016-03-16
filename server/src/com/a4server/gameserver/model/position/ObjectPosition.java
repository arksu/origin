package com.a4server.gameserver.model.position;

import com.a4server.Config;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.MoveObject;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.util.Rnd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * описание позиции объекта в игровом мире
 * храним координаты. спавним привязанный объект в мир.
 * обрабатываем движение и обновляем координаты
 */
public class ObjectPosition
{
	private static final Logger _log = LoggerFactory.getLogger(ObjectPosition.class.getName());

	/**
	 * абсолютные мировые координаты
	 */
	public volatile int _x;
	public volatile int _y;
//    private volatile int _z;

	/**
	 * уровень земли
	 */
	public volatile int _level;

	/**
	 * грид в котором находимся
	 */
	private Grid _grid;

	/**
	 * объект родитель чьи координаты описываем
	 */
	private final GameObject _activeObject;

	/**
	 * мы точно знаем где находится объект (загрузка объектов грида из базы)
	 */
	public ObjectPosition(int x, int y, int level, Grid grid)
	{
		_x = x;
		_y = y;
//        _z = 0;
		_level = level;
		_grid = grid;
		_activeObject = null;
	}

	/**
	 * позиция для объекта который спавнится (игрок может и не заспавнится на свою прошлую позицию)
	 */
	public ObjectPosition(int x, int y, int level, GameObject activeObject)
	{
		_x = x;
		_y = y;
//        _z = 0;
		_level = level;
		_grid = null;
		_activeObject = activeObject;
	}

	/**
	 * позиция для объекта который спавнится (спавним в ту же позицию, под некий объект)
	 * ...игрок выкидывает вещи из инвентаря под себя
	 */
	public ObjectPosition(ObjectPosition pos, GameObject activeObject)
	{
		_x = pos.getX();
		_y = pos.getY();
//        _z = 0;
		_level = pos.getLevel();
		_grid = null;
		_activeObject = activeObject;
	}

	/**
	 * пробуем заспавнить привязанный объект в мир
	 * ищем грид по координатам
	 * просим у грида чтобы он добавил нас в себя
	 * @return истина если получилось
	 */
	public boolean trySpawn()
	{
		// получаем грид в указанной позиции
		Grid grid = World.getInstance().getGridInWorldCoord(_x, _y, _level);
		if (grid != null && _activeObject != null)
		{
			_log.debug("try spawn " + _activeObject.toString() + " at " + toString());
			try
			{
				CollisionResult result;
				// сначала пытаемся 5 раз заспавнить в указанные координаты
				for (int tries = 0; tries < 5; tries++)
				{
					result = grid.trySpawn(_activeObject);
					if (result != null && result.getResultType() == CollisionResult.CollisionType.COLLISION_NONE)
					{
						_grid = grid;
						return true;
					}
					Thread.sleep(Rnd.get(20, 120));
				}
				// ежели не получилось туда. спавним рядом
				for (int tries = 0; tries < 10; tries++)
				{
					result = grid.trySpawnNear(_activeObject, Grid.TILE_SIZE * 3, false);
					if (result != null && result.getResultType() == CollisionResult.CollisionType.COLLISION_NONE)
					{
						_grid = grid;
						return true;
					}
					_log.debug("collision: " + (result != null ? result.toString() : "null"));
					Thread.sleep(Rnd.get(20, 120));
				}
			}
			catch (Exception e)
			{
				_log.error("trySpawn error: " + e.getMessage(), e);
			}
		}
		_log.debug("spawn failed " + toString());
		return false;
	}

	public boolean trySpawnRandom()
	{
		int tries = 5;
		while (tries > 0)
		{
			setRandomPostion();
			_log.debug("try spawn random...");
			if (trySpawn())
			{
				return true;
			}
			tries--;
		}
		return false;
	}

	public GameObject getActiveObject()
	{
		return _activeObject;
	}

	/**
	 * изменить координаты в пределах уровня
	 * @param x координата
	 * @param y координата
	 */
	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
		updateGrid();
	}

	public void setXY(double x, double y)
	{
		setXY((int) Math.round(x), (int) Math.round(y));
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public int getLevel()
	{
		return _level;
	}

	/**
	 * обновить привязку к гриду если перешли в другой
	 */
	private void updateGrid()
	{
		Grid new_grid = World.getInstance().getGridInWorldCoord(_x, _y, _level);
		if (_grid != new_grid)
		{
			setGrid(new_grid);
		}

	}

	/**
	 * установить грид в котором находимся
	 * @param value грид
	 */
	protected void setGrid(Grid value)
	{
		if ((_grid != null) && (getActiveObject() != null))
		{
			if (value != null)
			{
				// ставим не нулл
				// в старом гриде надо обновить состояние зон?????
			}
			else
			{
				// ставим нулл
				// в старом гриде удалим игрока из всех зон??????
			}
		}

		// проинформируем объект что перешли в другой грид
		if (getActiveObject() instanceof MoveObject)
		{
			((MoveObject) getActiveObject()).onGridChanged();
		}
		_grid = value;
	}

	public Grid getGrid()
	{
		return _grid;
	}

	/**
	 * получить X в координатах гридов
	 * @return координата
	 */
	public int getGridX()
	{
		return _x / (Grid.GRID_FULL_SIZE);
	}

	/**
	 * получить Y в координатах гридов
	 * @return координата
	 */
	public int getGridY()
	{
		return _y / (Grid.GRID_FULL_SIZE);
	}

	/**
	 * установить случайную позицию на поверхности мира
	 */
	private void setRandomPostion()
	{
		_level = 0;
		_x = Rnd.get(Config.WORLD_SG_WIDTH * Grid.SUPERGRID_FULL_SIZE);
		_y = Rnd.get(Config.WORLD_SG_HEIGHT * Grid.SUPERGRID_FULL_SIZE);
	}

	/**
	 * получить дистанцию между позициями двух объектов
	 * @param otherPos позиция другого объекта
	 * @return дистанция в единицах координат
	 */
	public int getDistance(ObjectPosition otherPos)
	{
		if (otherPos == null)
		{
			return 10000;
		}

		if (_level != otherPos._level)
		{
			_log.warn(otherPos.toString() + " level is different");
			return 10000;
		}
		else
		{
			return (int) Math.round(Math.sqrt(Math.pow((_x - otherPos._x), 2) + Math.pow((_y - otherPos._y), 2)));
		}
	}

	@Override
	public ObjectPosition clone()
	{
		return new ObjectPosition(_x, _y, _level, _grid);
	}

	public boolean equals(ObjectPosition p)
	{
		return _x == p._x && _y == p._y && _level == p._level;
	}

	@Override
	public String toString()
	{
		return "(" + _x + ", " + _y + ", " + _level + ")";
	}
}
