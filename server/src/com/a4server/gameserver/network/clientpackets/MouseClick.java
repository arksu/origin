package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.*;
import com.a4server.gameserver.model.ai.player.MoveActionAI;
import com.a4server.gameserver.model.ai.player.MoveVirtualAI;
import com.a4server.gameserver.model.collision.VirtualObject;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.inventory.InventoryItem;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.model.position.MoveToPoint;
import com.a4server.gameserver.model.position.ObjectPosition;
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

	/**
	 * кнопка нажата?
	 */
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
			try (GameLock ignored = player.lock())
			{
				if (player.getCursor().getName() == Arrow)
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
									moveToObjectAction(player, _objectId);
								}
								else
								{
									_log.debug("MoveToPoint (" + _x + ", " + _y + ")");
									// для простого передвижения не требуется мозг) не надо ни о чем думать
									player.setAi(null);
									// запустим движение. создадим контроллер для этого
									player.startMove(new MoveToPoint(_x, _y));
								}
							}
							break;

						case BUTTON_SECONDARY:
							// клик по объекту?
							GameObject object = player.getKnownKist().getKnownObjects().get(_objectId);
							if (object != null)
							{
								if (!_isDown)
								{
									// пкм по объекту - посмотрим что сделает объект
									_log.debug("actionClick on object: " + object);
									object.actionClick(player);
								}
							}
							else
							{
								if (_isDown)
								{
									// попали по земле
									GameObject lift = player.getLift(0);
									if (lift != null)
									{
										player.setAi(new MoveVirtualAI(player, new VirtualObject(_x, _y, lift), moveResult ->
										{
											try (GameLock ignored2 = player.lock())
											{
												if (player.getLift(0) == lift)
												{
													_log.debug("arrive virtual: " + moveResult);
													lift.getPos().setXY(_x, _y);
													lift.getPos().setHeading(player.getPos().getHeading());
													if (lift.getPos().trySpawn(1, 0))
													{
														player.removeLift(0);
													}
												}
											}
										}));
									}
								}
							}
							break;
					}
				}
				else
				{
					// есть курсор
					if (_isDown)
					{
						cursorClick(player, _x, _y, _button);
					}
				}
			}
		}
	}

	private void moveToObjectAction(final Player player, final int objectId)
	{
		// клик по объекту. бежим к нему и делаем действие над ним
		player.setAi(new MoveActionAI(player, _objectId, moveResult ->
		{
			GameObject object;
			object = moveResult.getObject();

			// этот объект вещь? т.е. вещь валяется на земле
			if (object.isItem())
			{
				Grid grid = object.getGrid();
				try (GameLock ignored = grid.lock())
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
							grid.removeObject(object);

							player.sendInteractPacket(new InventoryUpdate(player.getInventory()));
						}
					}
				}
			}
			else
			{
				_log.debug("interact with object " + object.toString());
				// надо провести взаимодействие с этим объектом
				player.beginInteract(object);
			}
		}));
	}

	private void cursorClick(Player player, int x, int y, int button)
	{
		if (button != BUTTON_PRIMARY) return;

		Cursor.CursorName cursor = player.getCursor().getName();
		_log.debug("mouse click with cursor: " + cursor);
		Grid grid;
		int n;
		Tile tile;
		switch (cursor)
		{
			case LiftUp:
				if (_objectId != 0)
				{
					player.setAi(new MoveActionAI(player, _objectId, moveResult ->
					{
						GameObject object = moveResult.getObject();
						try (GameLock ignored = player.lock(); GameLock ignored2 = object.lock())
						{
							_log.debug("LIFT UP");
							player.addLift(object, 0);
						}
					}));
				}
				player.setCursor(Arrow);
				break;

			case TileUp:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos().getLevel());
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);

				tile.setHeight(tile.getHeight() + 1);

				grid.setTile(tile, n, true);
				break;

			case TileDown:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos().getLevel());
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);

				tile.setHeight(tile.getHeight() - 1);

				grid.setTile(tile, n, true);
				break;

			case TileSand:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos().getLevel());
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);
				tile.setType(Tile.TileType.TILE_SAND);
				grid.setTile(tile, n, true);
				break;

			case TileGrass:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos().getLevel());
				n = World.getTileIndex(x, y);
				tile = grid.getTile(n);
				tile.setType(Tile.TileType.TILE_GRASS);
				grid.setTile(tile, n, true);
				break;

			case Spawn:
				grid = World.getInstance().getGridInWorldCoord(x, y, player.getPos().getLevel());
				int typeId = player.getCursor().getTypeId();
				GameObject object = ObjectsFactory.getInstance().createObject(typeId);
				object.setPos(new ObjectPosition(x, y, 0, grid.getLevel(), grid, object));
				if (object.getPos().trySpawn())
				{
					object.store();
				}
				player.setCursor(Arrow);
		}
	}
}
