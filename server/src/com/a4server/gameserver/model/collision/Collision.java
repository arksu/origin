package com.a4server.gameserver.model.collision;

import com.a4server.Config;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Tile;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.model.objects.CollisionTemplate;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.util.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

import static com.a4server.gameserver.model.Tile.TileType.TILE_WATER_DEEP;
import static com.a4server.gameserver.model.Tile.TileType.TILE_WATER_LOW;
import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_VIRTUAL;
import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_WORLD;

/**
 * обсчет коллизий между объектами
 * Created by arksu on 07.01.2015.
 */
public class Collision
{
	private static final Logger _log = LoggerFactory.getLogger(Collision.class.getName());

	/**
	 * размер области под первичный фильтр объектов
	 */
	public static final int COLLISION_DISTANCE = 120;

	/**
	 * расстояние в единицах игровых координат между итерациями
	 */
	public static final int COLLISION_ITERATION_LENGTH = 3;

	/**
	 * сколько тайлов до конца мира будут давать коллизию
	 */
	public static final int WORLD_BUFFER_SIZE = Grid.TILE_SIZE * 5;

	public static CollisionResult checkCollision(GameObject object, int fromX, int fromY, int toX, int toY,
	                                             Move.MoveType moveType, GameObject virtual, List<Grid> grids,
	                                             int targetObjId)
	{

		// проверим координаты на выход за пределы мира
		if (checkWorldLimit(fromX, fromY) || checkWorldLimit(toX, toY))
		{
			return new CollisionResult(COLLISION_WORLD, fromX, fromY);
		}

		// первичный фильтр, создадим буфер объектов которые попадают в возможную зону коллизии
		List<GameObject> objects = new LinkedList<>();
		Rect filterRect = new Rect(COLLISION_DISTANCE).move(fromX, fromY);
		for (Grid grid : grids)
		{
			for (GameObject obj : grid.getObjects())
			{
				// условие отбора объекта
				if ((filterRect.isPointInside(obj.getPos().getX(), obj.getPos().getY())
				     // это НЕ я
				     && obj.getObjectId() != object.getObjectId()

				     && !obj.isDeleting()

				     // todo то что несем на себе не дает коллизий

				     // дают ли объекты между собой коллизию?
				     && getCollision(object, obj, false)) ||
				    // если это цель, она должна давать коллизию
				    (obj.getObjectId() == targetObjId && getCollision(object, obj, true)))
				{
					objects.add(obj);
				}
			}
		}

		// проверяем коллизию на исходных координатах
		Rect rr = object.getBoundRect().clone().move(fromX, fromY);
		Rect ro;
		Rect vrr = null;
		for (GameObject obj : objects)
		{
			ro = obj.getBoundRect().clone().move(obj.getPos().getX(), obj.getPos().getY());
			if (rr.isIntersect(ro))
			{
				return new CollisionResult(obj, fromX, fromY);
			}
		}

		// проверим тайл
		Tile tile;

		tile = checkTileCollision(object, fromX, fromY, moveType, grids);
		if (tile != null)
		{
			return new CollisionResult(tile, fromX, fromY);
		}

		if (virtual != null)
		{
			vrr = virtual.getBoundRect().clone().move(virtual.getPos().getX(), virtual.getPos().getY());
		}

		// проверим виртуальную коллизию
		if (virtual != null && getCollision(object, virtual, false))
		{
			if (rr.isIntersect(vrr))
			{
				return new CollisionResult(COLLISION_VIRTUAL, fromX, fromY);
			}
		}

		// теперь начинаем итеративно двигаться и проверяем коллизии на каждой итерации
		int cycles = 0;
		double cx = fromX;
		double cy = fromY;
		double nx, ny;
		boolean needExit = false;
		double d;
		while (true)
		{
			d = distance(cx, cy, toX, toY);
			if (d < 0.001)
			{
				return CollisionResult.NONE;
			}
			else if (d < COLLISION_ITERATION_LENGTH)
			{
				nx = toX;
				ny = toY;
				needExit = true;
			}
			else
			{
				double k = COLLISION_ITERATION_LENGTH / d;
				nx = cx + (toX - cx) * k;
				ny = cy + (toY - cy) * k;
			}
			int inx = (int) Math.round(nx);
			int iny = (int) Math.round(ny);

			// проверяем коллизию на текущих координатах
			rr = object.getBoundRect().clone().move(inx, iny);
			for (GameObject obj : objects)
			{
				ro = obj.getBoundRect().clone().move(obj.getPos().getX(), obj.getPos().getY());
				if (rr.isIntersect(ro))
				{
					return new CollisionResult(obj, (int) Math.round(cx), (int) Math.round(cy));
				}
			}

			// проверяем коллизию с тайлом на текущих координатах
			tile = checkTileCollision(object, inx, iny, moveType, grids);
			if (tile != null)
			{
				return new CollisionResult(tile, (int) Math.round(cx), (int) Math.round(cy));
			}

			// проверим виртуальную коллизию
			if (virtual != null && getCollision(object, virtual, false))
			{
				if (vrr.isIntersect(rr))
				{
					return new CollisionResult(COLLISION_VIRTUAL, (int) Math.round(cx), (int) Math.round(cy));
				}
			}

			// todo проверить клаймы

			// проверим выход за границы мира
			if (checkWorldLimit(inx, iny))
			{
				return new CollisionResult(COLLISION_WORLD, (int) Math.round(cx), (int) Math.round(cy));
			}

			if (needExit)
			{
				return CollisionResult.NONE;
			}

			cx = nx;
			cy = ny;

			cycles++;
			if (cycles > 20)
			{
				_log.warn("Failed calc collision: cycles off " + object);
				return CollisionResult.FAIL;
			}

		}
	}

	private static boolean checkWorldLimit(int x, int y)
	{
		return x < WORLD_BUFFER_SIZE ||
		       x > Grid.SUPERGRID_FULL_SIZE * Config.WORLD_SG_WIDTH - WORLD_BUFFER_SIZE ||
		       y < WORLD_BUFFER_SIZE ||
		       y > Grid.SUPERGRID_FULL_SIZE * Config.WORLD_SG_HEIGHT - WORLD_BUFFER_SIZE;
	}

	private static Tile checkTileCollision(GameObject object, int x, int y, Move.MoveType moveType, List<Grid> grids)
	{
		// по всем гридам
		for (Grid grid : grids)
		{
			// границы грида
			Rect gr = new Rect(0, 0, Grid.GRID_FULL_SIZE, Grid.GRID_FULL_SIZE);
			gr.move(grid.getCoordX(), grid.getCoordY());
			// точка внутри грида?
			if (gr.isPointInside(x, y))
			{
				// берем тайл из грида
				Tile t = grid.getTile(World.getTileIndex(x, y));
				if (getTileCollision(object, moveType, t))
				{
					return t;
				}
			}
		}
		return null;
	}

	private static double distance(double x1, double y1, double x2, double y2)
	{
		double dx = x2 - x1;
		double dy = y2 - y1;
		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * есть ли коллизия между двумя объектами?
	 * например ворота если открыты не дают коллизию никому.
	 * но если они цель - то есть мы хотим с ними чтото сделать, тогда дают
	 * @param obj1 объект который движется
	 * @param obj2 куда движемся
	 * @param isTarget obj2 - цель?
	 * @return дает или нет коллизию
	 */
	private static boolean getCollision(GameObject obj1, GameObject obj2, boolean isTarget)
	{
		// вещи не дают коллизий будучи брошенными на землю
		if (obj2.isItem() || obj1.isItem())
		{
			return false;
		}
		ObjectTemplate template = obj2.getTemplate();
		if (template != null)
		{
			CollisionTemplate collisionTemplate = template.getCollision();
			if (collisionTemplate != null)
			{
				return collisionTemplate.getCollision(obj1);
			}
		}
		return true;
	}

	/**
	 * дает ли тайл коллизию?
	 * @param object объект который движется
	 * @param moveType тип передвижения
	 * @param tile объект тайла
	 * @return истина - если дает коллизию
	 */
	private static boolean getTileCollision(GameObject object, Move.MoveType moveType, Tile tile)
	{
		switch (moveType)
		{
			case WALK:
			case SPAWN:
				return tile.getType() == TILE_WATER_DEEP;
			case SWIMMING:
				return tile.getType() != TILE_WATER_DEEP && tile.getType() != TILE_WATER_LOW;
			default:
				return true;
		}
	}

}
