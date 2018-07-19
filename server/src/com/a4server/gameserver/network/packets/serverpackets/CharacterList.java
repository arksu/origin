package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.Database;
import com.a4server.gameserver.network.packets.GamePktClient;
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
	private static final String CHARACTERS_SELECT = "SELECT charId, charName, accessLevel, lastAccess FROM characters WHERE del=0 AND account=?";

	private final Map<Integer, CharacterData> _chars;
	private final int _last_char;

	public class CharacterData
	{
		protected String _char_name;
		protected int _char_id;
		protected int _accessLevel;
		protected long _lastAccess;
		protected boolean _isLast;

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
			writeC(_isLast ? 1 : 0);
		}

		public int getAccessLevel()
		{
			return _accessLevel;
		}
	}

	public CharacterList(GamePktClient client)
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
					// ищем последнего чара по _lastAccess
					CharacterData last = null;
					for (CharacterData c : _chars.values())
					{
						if (last == null || c._lastAccess > last._lastAccess)
						{
							last = c;
						}
					}
					if (last != null)
					{
						last._isLast = true;
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

