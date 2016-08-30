package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.*;
import com.a4server.gameserver.model.ai.player.MindMoveAction;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.inventory.InventoryItem;
import com.a4server.gameserver.model.position.MoveToPoint;
import com.a4server.gameserver.network.serverpackets.InventoryUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.Cursor.CursorName.Arrow;

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
			try (GameLock ignored = player.tryLock())
			{
				if (player.getCursor() == Arrow)
				{
					switch (_button)
					{
						case BUTTON_PRIMARY:
							if (_isDown)
							{
								// в руке что-то держим?
								if (player.getHand() != null)
								{
									// chpok
									if (player.dropItem(player.getHand().getItem()))
									{
										// уберем все из руки
										player.setHand(null);
									}
								}
								// кликнули в объект?
								else if (_objectId != 0 && _objectId != player.getObjectId())
								{
									moveToObject(player, _objectId);
								}
								else
								{
									_log.debug("MoveToPoint (" + _x + ", " + _y + ")");
									// для простого передвижения не требуется мозг) не надо ни о чем думать
									player.setMind(null);
									// запустим движение. создадим контроллер для этого
									player.StartMove(new MoveToPoint(_x, _y));
								}
							}
							break;

						case BUTTON_SECONDARY:
							// клик по объекту?
							GameObject object = player.isKnownObject(_objectId);
							if (object != null && !_isDown)
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

	private void moveToObject(final Player player, final int objectId)
	{
		// клик по объекту. бежим к нему и делаем действие над ним
		player.setMind(new MindMoveAction(player, _objectId, new MindMoveAction.ArrivedCallback()
		{
			@Override
			public void onArrived(CollisionResult moveResult)
			{
				GameObject object;
				switch (moveResult.getResultType())
				{
					case COLLISION_OBJECT:
						object = moveResult.getObject();
						// наша цель совпадает с тем куда пришли?
						if (object != null && object.getObjectId() == objectId && !object.isDeleteing())
						{
							_log.debug("interact with object " + object.toString());
							// надо провести взаимодействие с этим объектом
							player.beginInteract(object);
						}
						break;

					case COLLISION_NONE:
						// коллизии нет. мы знаем этот объект?
						object = player.isKnownObject(objectId);
						// если мы находимся точно в его позиции
						// этот объект вещь? т.е. вещь валяется на земле
						if (object.getPos().equals(player.getPos()) && object.getTemplate().getItem() != null)
						{
							// найдем вещь в базе
							AbstractItem item = AbstractItem.load(player, objectId);
							if (item != null)
							{
								// вещь обязательно должна быть помечена как удаленная
								if (!item.isDeleted())
								{
									throw new RuntimeException("pickup item is not deleted! " + item);
								}

								// положим вещь в инвентарь игрока
								InventoryItem putItem = player.getInventory().putItem(item);
								// сначала пометим объект в базе как удаленный, а с вещи наоборот снимем пометку
								if (putItem != null && object.markDeleted(true) && putItem.markDeleted(false))
								{
									// сохраним в базе
									putItem.store();

									// разошлем всем пакет с удалением объекта из мира
									Grid grid = player.getGrid();
									if (grid.tryLock())
									{
										try
										{
											grid.removeObject(object);
										}
										finally
										{
											grid.unlock();
										}
									}

									player.sendInteractPacket(new InventoryUpdate(player.getInventory()));
								}
							}
						}
						break;
				}

			}
		}));
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
