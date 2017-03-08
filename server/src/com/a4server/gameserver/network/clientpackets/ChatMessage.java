package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.idfactory.IdFactory;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.model.event.GridEvent;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import com.a4server.gameserver.network.serverpackets.CreatureSay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * игрок отправляет сообщение в чат
 * Created by arksu on 17.02.15.
 */
public class ChatMessage extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ChatMessage.class.getName());

	private int _channelId;
	private String _message;

	public static final int GENERAL = 0;
	public static final int SHOUT = 0;

	@Override
	public void readImpl()
	{
		_channelId = readC();
		_message = readS();
	}

	@Override
	public void run()
	{
		Player player = client.getPlayer();
		if (player != null)
		{
			_log.debug("chat " + player + ": " + _message);
			// смотрим в каком канале отправили сообщение
			switch (_channelId)
			{
				// основной канал - общий чат вокруг объекта
				case GENERAL:
					if (_message.startsWith("/"))
					{
						if (player.getAccessLevel() >= 100)
						{
							_log.debug("console command: " + _message);
							if ("/randomgrid".equalsIgnoreCase(_message))
							{
								player.randomGrid();
							}
							else if ("/nextid".equalsIgnoreCase(_message))
							{
								IdFactory.getInstance().getNextId();
								IdFactory.getInstance().getNextId();
								IdFactory.getInstance().getNextId();
								int nextId = IdFactory.getInstance().getNextId();
								getClient().sendPacket(new CreatureSay(player.getObjectId(), "next id: " + nextId));
								_log.debug("nextid: " + nextId);
							}
							// заспавнить вещь себе в инвентарь
							else if (_message.startsWith("/createitem") || _message.startsWith("/ci"))
							{
								try
								{
									String[] v = _message.split(" ");
									int typeId = Integer.parseInt(v[1]);
									int count = Integer.parseInt(v[2]);
									ObjectTemplate template = ObjectsFactory.getInstance().getTemplate(typeId);
									if (template != null)
									{
										while (count > 0)
										{
											_log.info("сreate item: " + template.getName() + " count: " + count);
											if (!player.generateItem(typeId, 10, true))
											{
												break;
											}
											count--;
										}
									}
								}
								catch (NumberFormatException nfe)
								{
									getClient().sendPacket(new CreatureSay(player.getObjectId(), "spawn item: params error"));
								}
							}
						}
						// тут исполняем обычные команды доступные для всех
						if ("/online".equalsIgnoreCase(_message))
						{
							_log.debug("server online: " + World.getInstance().getPlayersCount());
							// онлайн сервера написать в чат игроку
							getClient().sendPacket(new CreatureSay(player.getObjectId(), "online: " + World.getInstance().getPlayersCount()));
						}
					}
					else
					{
						GridEvent gridEvent = new GridEvent(player, GridEvent.EventType.EVT_CHAT_MESSAGE, _channelId);
						gridEvent.setPacket(new CreatureSay(player.getObjectId(), _message));
						player.getPos().getGrid().broadcastEvent(gridEvent);
					}
					break;
			}
		}
	}
}
