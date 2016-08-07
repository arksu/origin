package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.GameLock;
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

	private static final int BUTTON_MOVE = 0;
	private static final int BUTTON_ACTION = 1;

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
		// нажатость передаем отриацательным числом
		boolean isDown = _button < 10;
		// восстановим кнопку
		_button = _button >= 10 ? _button - 10 : _button;
		Player player = client.getActiveChar();
		if (player != null && !player.isDeleteing())
		{
			if (isDown)
			{
				try (GameLock ignored = player.tryLock())
				{
					switch (_button)
					{
						case BUTTON_MOVE:
							// в руке что-то держим?
							if (player.getHand() != null)
							{
								// chpok

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

						case BUTTON_ACTION:
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
				catch (Exception e)
				{
					_log.error("MouseClick error:" + e.getMessage(), e);
				}
			}
		}
	}
}
