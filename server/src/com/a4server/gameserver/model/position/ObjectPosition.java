package com.a4server.gameserver.model.position;

import com.a4server.Config;
import com.a4server.Database;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.MovingObject;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.util.Rnd;
import com.a4server.util.Vec2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.a4server.gameserver.model.GameObject.DELETE;
import static com.a4server.gameserver.model.GameObject.UPDATE_POSITION;
import static com.a4server.gameserver.model.Player.UPDATE_CHARACTER_POS;

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
	private volatile int _x;
	private volatile int _y;

	/**
	 * уровень земли
	 */
	private volatile int _level;

	private volatile int _heading = 0;

	/**
	 * последние сохраненные данные
	 */
	private int _storedSuperGrid;

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
	public ObjectPosition(int x, int y, int heading, int level, Grid grid, GameObject activeObject)
	{
		_x = x;
		_y = y;
		_heading = heading;
		_level = level;
		_grid = grid;
		_activeObject = activeObject;
	}

	/**
	 * позиция для объекта который спавнится (игрок может и не заспавнится на свою прошлую позицию)
	 */
	public ObjectPosition(int x, int y, int heading, int level, GameObject activeObject)
	{
		_x = x;
		_y = y;
		_heading = heading;
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
		return trySpawn(5, 5);
	}

	public boolean trySpawn(int tries, int nearTries)
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
				for (int t = 0; t < tries; t++)
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
				for (int t = 0; t < nearTries; t++)
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
				throw new RuntimeException(e);
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
	 * изменить координаты в пределах !!уровня!!
	 * @param x координата
	 * @param y координата
	 */
	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
		updateGrid();
	}

	public Vec2i sub(ObjectPosition other)
	{
		return new Vec2i(_x - other._x, _y - other._y);
	}

	public void setHeading(int heading)
	{
		_heading = heading;
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public int getHeading()
	{
		return _heading;
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
//		if ((_grid != null) && (getActiveObject() != null))
//		{
//			if (value != null)
//			{
		// ставим не нулл
		// в старом гриде надо обновить состояние зон?????
//			}
//			else
//			{
		// ставим нулл
		// в старом гриде удалим игрока из всех зон??????
//			}
//		}

		// проинформируем объект что перешли в другой грид
		if (getActiveObject() instanceof MovingObject)
		{
			((MovingObject) getActiveObject()).onGridChanged();
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

	@SuppressWarnings("MethodDoesntCallSuperMethod")
	@Override
	public ObjectPosition clone()
	{
		return new ObjectPosition(_x, _y, _heading, _level, _grid, _activeObject);
	}

	public boolean equals(ObjectPosition p)
	{
		return _x == p._x && _y == p._y && _level == p._level;
	}

	@SuppressWarnings("SuspiciousNameCombination")
	public void store()
	{
		if (_activeObject.isPlayer())
		{
			try
			{
				try (Connection con = Database.getInstance().getConnection();
				     PreparedStatement ps = con.prepareStatement(UPDATE_CHARACTER_POS))
				{
					ps.setInt(1, _x);
					ps.setInt(2, _y);
					ps.setInt(3, _activeObject.getObjectId());
					ps.execute();
				}
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			// если сменился супергрид объекта - надо перенести целиком объект из одного в другой
			if (_storedSuperGrid != _grid.getSg())
			{
				// TODO проверить корректность механизма
				try
				{
					// удаляем целиком объкт из старого супергрида
					String query = DELETE;
					query = query.replaceFirst("sg_0", "sg_" + Integer.toString(_storedSuperGrid));
					try (Connection con = Database.getInstance().getConnection();
					     PreparedStatement ps = con.prepareStatement(query))
					{
						ps.setInt(1, _activeObject.getObjectId());
						ps.execute();
					}
					// сохраним, при этом он запишется уже в новый
					_activeObject.store();
					_storedSuperGrid = _grid.getSg();
				}
				catch (SQLException e)
				{
					throw new RuntimeException(e);
				}
			}
			else
			{
				String query = UPDATE_POSITION;
				query = query.replaceFirst("sg_0", "sg_" + Integer.toString(_grid.getSg()));

				// query queue
				try (Connection con = Database.getInstance().getConnection();
				     PreparedStatement statement = con.prepareStatement(query))
				{
					statement.setInt(1, _x);
					statement.setInt(2, _y);
					statement.setInt(3, _heading);
					statement.setInt(4, _grid.getId());
					statement.setInt(5, _activeObject.getObjectId());
					statement.executeUpdate();
					con.close();
				}
				catch (SQLException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		// также сохраним в базу позицию всех объектов которые несем
		for (GameObject lift : _activeObject.getLift().values())
		{
			ObjectPosition pos = lift.getPos();
			pos.setXY(_x, _y);
			pos.setHeading(_heading);
			pos.store();
		}
	}

	@Override
	public String toString()
	{
		return "(" + _x + ", " + _y + ", " + _level + ")";
	}
}
