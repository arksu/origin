package com.a4server.gameserver.network.packets.clientpackets;

import com.a4server.Config;
import com.a4server.Database;
import com.a4server.gameserver.network.packets.GamePktClient;
import com.a4server.gameserver.network.packets.serverpackets.CharacterList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by arksu on 03.01.2015.
 */
public class AuthGame extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(AuthGame.class.getName());
	protected static final String ACCOUNT_CHECK_SESSIONKEY = "SELECT login, lastActive, lastChar FROM accounts WHERE key1 = ? AND key2 = ?";

	private int _key1, _key2;

	@Override
	public void readImpl()
	{
		_key1 = readD();
		_key2 = readD();
	}

	@Override
	public void run()
	{
		try
		{
			String login;
			try (Connection con = Database.getInstance().getConnection();
				 PreparedStatement ps = con.prepareStatement(ACCOUNT_CHECK_SESSIONKEY))
			{
				ps.setInt(1, _key1);
				ps.setInt(2, _key2);
				try (ResultSet rset = ps.executeQuery())
				{
					// нашли в базе такую сессию
					if (rset.next())
					{
						// такая сессия открыта на логин сервере. авторизация удалась
						// проверим время сессии
						long lastactive = rset.getLong("lastactive");
						login = rset.getString("login");

						// даем всего 5 секунд на коннект к гейм серверу
						long unixTime = System.currentTimeMillis() / 1000L;
						if ((unixTime - lastactive) > 5)
						{
							_log.warn("Try enter to game account [" + login + "], but session is timeout");
							// рвем коннект без объяснения причин
							getConnect().close();
						}
						else
						{
							getClient().setAccount(login);
							getClient().setLastChar(rset.getInt("lastChar"));
							getClient().setState(GamePktClient.GameClientState.AUTHED);
							if (Config.DEBUG)
							{
								_log.info("account [" + login + "] enter in game...");
							}
							// вышлем список чаров
							sendPacket(new CharacterList(getClient()));
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Failed check session key: " + _key1 + " " + _key2);
			getConnect().close();
		}
	}
}
