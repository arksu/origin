package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.ai.player.MindMoveAction;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.position.MoveToPoint;
import com.a4server.gameserver.model.position.ObjectPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * клик по карте (тайлы, объекты)
 * Created by arksu on 08.02.15.
 */
public class MouseClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(MouseClick.class.getName());

	private static final int BUTTON_LEFT = 0;
	private static final int BUTTON_RIGHT = 1;

	/**
	 * какая кнопка была нажата
	 */
	private int _button;

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
	}

	@Override
	public void run()
	{
		// нажатая кнопка < 10
		boolean isDown = _button < 10;
		_button = _button >= 10 ? _button - 10 : _button;
		Player player = client.getActiveChar();
		if (player != null)
		{
			if (isDown)
			{
				if (player.tryLock(Player.WAIT_LOCK))
				{
					try
					{
						switch (_button)
						{
							case BUTTON_LEFT:
								// в руке что-то держим?
								if (player.getHand() != null)
								{
									// chpok
									AbstractItem item = player.getHand().getItem();
									GameObject object = new GameObject(item.getObjectId(), item.getTemplate().getObjectTemplate());
									object.setPos(new ObjectPosition(player.getPos(), object));
									try
									{
										// спавним объект
										if (object.getPos().trySpawn())
										{
											_log.debug("item dropped: " + item);
											// обновляем в базе
											if (object.store() && item.markDeleted())
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
									catch (Exception e)
									{
										_log.warn("failed spawn hand item " + player.getHand().getItem());
									}
								}
								else if (_objectId != 0 && _objectId != player.getObjectId())
								{
									// клик по объекту. бежим к нему и делаем действие над ним
									player.setMind(new MindMoveAction(player, _objectId));
								}
								else
								{
									_log.debug("MoveToPoint to (" + _x + ", " + _y + ")");
									// для простого передвижения не требуется мозг) не надо ни о чем думать
									player.setMind(null);
									// запустим движение. создадим контроллер для этого
									player.StartMove(new MoveToPoint(_x, _y));
								}
								break;

							case BUTTON_RIGHT:
								// клик по объекту?
								GameObject object = player.isKnownObject(_objectId);
								if (object != null)
								{
									// пкм по объекту - посмотрим что сделает объект

								}
								break;
						}
					}
					finally
					{
						player.unlock();
					}
				}
			}
		}
	}
}
