package com.a4server.gameserver.network.serverpackets;

import com.a4server.Database;
import com.a4server.gameserver.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * список чаров на этапе входа в мир
 * Created by arksu on 04.01.2015.
 */
public class CharacterList extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterList.class.getName());

	// SQL Queries
	private static final String CHARACTERS_SELECT = "SELECT charId, charName, accessLevel FROM characters WHERE del=0 AND account=?";

	private final Map<Integer, CharacterData> _chars;
	private final int _last_char;

	public class CharacterData
	{
		protected String _char_name;
		protected int _char_id;
		protected int _accessLevel;

		protected CharacterData(ResultSet rset) throws SQLException
		{
			_char_name = rset.getString("charName");
			_char_id = rset.getInt("charId");
			_accessLevel = rset.getInt("accessLevel");
		}

		protected void write()
		{
			writeD(_char_id);
			writeS(_char_name);
		}

		public int getAccessLevel()
		{
			return _accessLevel;
		}
	}

	public CharacterList(GameClient client)
	{
		_last_char = client.getLastChar();
		_chars = new HashMap<>();
		try
		{
			try (Connection con = Database.getInstance().getConnection();
				 PreparedStatement ps = con.prepareStatement(CHARACTERS_SELECT))
			{
				ps.setString(1, client.getAccount());
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						CharacterData d = new CharacterData(rset);
						_chars.put(d._char_id, d);
					}
					client.setCharsInfo(_chars);
				}
			}
		}
		catch (Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	@Override
	protected void write()
	{
		// SCharacterList 0x03
		writeC(0x03);

		writeD(_last_char);
		writeH(_chars.size());
		for (CharacterData char_data : _chars.values())
		{
			char_data.write();
		}
	}
}

