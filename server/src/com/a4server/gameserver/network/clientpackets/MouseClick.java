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
 * Created by arksu on 08.02.15.
 */
public class MouseClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(MouseClick.class.getName());

	private static final int BUTTON_LEFT = 0;
	private static final int BUTTON_RIGHT = 1;

	private int _button;
	private int _x;
	private int _y;
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
										// todo: решить по спавну, удалить итем из таблицы вещей
										// и добавить в таблицу объектов
										if (object.getPos().trySpawn())
										{
											_log.debug("item dropped: " + item);
											player.setHand(null);
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
								if (_objectId != 0 && _objectId != player.getObjectId())
								{
									// клик по объекту. бежим к нему и делаем действие над ним
									player.setMind(new MindMoveAction(player, _objectId));
								}
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
