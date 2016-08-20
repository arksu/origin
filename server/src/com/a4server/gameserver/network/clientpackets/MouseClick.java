package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.*;
import com.a4server.gameserver.model.ai.player.MindMoveAction;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.position.MoveToPoint;
import com.a4server.gameserver.model.position.ObjectPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.Cursor.CursorName.*;

/**
 * клик по карте (тайлы, объекты)
 * Created by arksu on 08.02.15.
 */
public class MouseClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(MouseClick.class.getName());

	private static final int BUTTON_PRIMARY = 0;
	private static final int BUTTON_SECONDARY = 1;

	/**
	 * какая кнопка была нажата
	 */
	private int _button;

	private boolean _isDown;

	/**
	 * куда тыкнули на картне в абсолютных мировых координатах
	 */
	private int _x;
	private int _y;

	/**
	 * ид объекта в который попали мышкой (если != 0)
	 */
	private int _objectId;

	@Override
	public void readImpl()
	{
		_button = readC();
		_x = readD();
		_y = readD();
		_objectId = readD();

		// нажатость передаем отриацательным числом
		_isDown = _button < 10;
		// восстановим кнопку
		_button = _button >= 10 ? _button - 10 : _button;
	}

	@Override
	public void run()
	{
		Player player = client.getPlayer();
		if (player != null)
		{
			if (_isDown)
			{
				try (GameLock ignored = player.tryLock())
				{
					if (player.getCursor() == Arrow)
					{
						switch (_button)
						{
							case BUTTON_PRIMARY:
								// в руке что-то держим?
								if (player.getHand() != null)
								{
									// chpok
									itemDrop(player);
								}
								// кликнули в объект?
								else if (_objectId != 0 && _objectId != player.getObjectId())
								{
									// клик по объекту. бежим к нему и делаем действие над ним
									player.setMind(new MindMoveAction(player, _objectId));
								}
								else
								{
									_log.debug("MoveToPoint (" + _x + ", " + _y + ")");
									// для простого передвижения не требуется мозг) не надо ни о чем думать
									player.setMind(null);
									// запустим движение. создадим контроллер для этого
									player.StartMove(new MoveToPoint(_x, _y));
								}
								break;

							case BUTTON_SECONDARY:
								// клик по объекту?
								GameObject object = player.isKnownObject(_objectId);
								if (object != null)
								{
									// пкм по объекту - посмотрим что сделает объект
									_log.debug("actionClick on object: " + object);
									object.actionClick(player);
								}
								break;
						}
					}
					else
					{
						cursorClick(player, _x, _y, _button);
					}
				}
				catch (Exception e)
				{
					_log.error("MouseClick error:" + e.getMessage(), e);
				}
			}
		}
	}

	private void itemDrop(Player player)
	{
		// берем вещь из руки
		AbstractItem item = player.getHand().getItem();
		// создаем новый игровой объект на основании шаблона взятой вещи
		GameObject object = new GameObject(item.getObjectId(), item.getTemplate().getObjectTemplate());
		// зададим этому объекту позицию - прямо под игроком
		object.setPos(new ObjectPosition(player.getPos(), object));

		// пытаемся заспавнить этот объект
		if (object.getPos().trySpawn())
		{
			_log.debug("item dropped: " + item);
			// сначала грохнем вещь! и только потом сохраним объект в базу
			if (item.markDeleted() && object.store())
			{
				// уберем все из руки
				player.setHand(null);
			}
			else
			{
				// но если чето сцуко пошло не так - уроним все к хуям
				throw new RuntimeException("failed update db on item drop");
			}
		}
		else
		{
			_log.debug("cant drop: " + item);
		}
	}

	private void cursorClick(Player player, int x, int y, int button)
	{
		if (button != BUTTON_PRIMARY) return;

		Cursor.CursorName cursor = player.getCursor();
		_log.debug("mouse click with cursor: " + cursor);
		Grid grid;
		int n;
		Tile tile;
		switch (cursor)
		{
			case TileUp:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos()._level);
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);

				tile.setHeight(tile.getHeight() + 1);

				grid.setTile(tile, n, true);
				break;

			case TileDown:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos()._level);
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);

				tile.setHeight(tile.getHeight() - 1);

				grid.setTile(tile, n, true);
				break;

			case TileSand:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos()._level);
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);
				tile.setType(Tile.TileType.TILE_SAND);
				grid.setTile(tile, n, true);
				break;

			case TileGrass:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos()._level);
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);
				tile.setType(Tile.TileType.TILE_GRASS);
				grid.setTile(tile, n, true);
				break;
		}
	}
}
