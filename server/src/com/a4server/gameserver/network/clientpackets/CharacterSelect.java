package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.network.serverpackets.CharSelected;
import com.a4server.gameserver.network.serverpackets.CharacterList;
import com.a4server.gameserver.network.serverpackets.ServerClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 04.01.2015.
 */
public class CharacterSelect extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterSelect.class.getName());

	private int _charId;

	@Override
	public void readImpl()
	{
		_charId = readD();
	}

	@Override
	public void run()
	{
		final GameClient client = getClient();

		// мы должны быть уверены что можем захватить блокировку
		// но если не удалось то просто ничего не делаем, клиент повторит пакет
		if (client.getActiveCharLock().tryLock())
		{
			try
			{
				// всегда должен быть null
				// но если не так. ничего страшного клиент повторит пакет
				if (client.getActiveChar() == null)
				{
					// проверим забанен ли персонаж?
					// access level < 0
					CharacterList.CharacterData data = client.getCharsInfo().get(_charId);
					if (data.getAccessLevel() < 0)
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}

					// можно ли заходить несколькими персами с 1 ip

					// грузим перса из базы
					final Player cha = client.loadCharacter(_charId);
					// загрузить не удалось. закроем соединение
					if (cha == null)
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					_log.debug("character spawn: " + cha.toString());

					// добавим игрока в мир чтобы нельзя было повторно зайти персом
					if (World.getInstance().addPlayer(cha))
					{
						cha.setClient(client);
						// говорим клиенту показать экран загрузки мира
						client.sendPacket(new CharSelected(cha));

						client.sendPacket(cha.makeInitClientPacket());

						// пробуем заспавнить игрока
						if (!trySpawn(cha))
						{
							// если не получилось - надо выгрузить персонажа и везде удалить упоминания о нем
							// удалим игрока из игры
							if (!World.getInstance().removePlayer(_charId))
							{
								_log.warn("World does not contains loaded player for spawn");
							}
							// закроем соединение с клиентом
							client.close(ServerClose.STATIC_PACKET);
							_log.error("Fail spawn player to world (tries expired): " + cha);
							return;
						}

						// все ок. чар заспавнен в мир
						// активируем 9 гридов вокруг игрока. "вдыхаем в них жизнь"
						try
						{
							cha.loadGrids();
							cha.activateGrids();
							// лишний раз убедимся что все нужные гриды загружены
							if (!cha.isGridsLoaded())
							{
								throw new Exception("grids not loaded");
							}
						}
						catch (Exception e)
						{
							_log.error("Fail to load & wait grids", e);

							// надо корректно удалить перса из мира
							cha.deleteMe();

							return;
						}

						// последние штрихи
						cha.updateLastChar();
						client.setActiveChar(cha);
						cha.setOnlineStatus(true);


						client.setState(GameClient.GameClientState.IN_GAME);
					}
				}
			}
			finally
			{
				client.getActiveCharLock().unlock();
			}
		}
	}

	private boolean trySpawn(Player cha)
	{
		// заспавнится в указанной точке не получилось
		// пробуем в случайном месте мира
		return cha.getPos().trySpawn() || cha.getPos().trySpawnRandom();
	}
}
