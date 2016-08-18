package com.a2client.network.game.serverpackets;

import com.a2client.Config;
import com.a2client.Main;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.CharacterSelect;

import java.util.ArrayList;
import java.util.List;

public class CharacterList extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x03, CharacterList.class);
	}

	static public final List<CharacterData> _chars = new ArrayList<CharacterData>();
	static public int _last_char;

	public class CharacterData
	{
		public String _char_name;
		public int _char_id;

		protected CharacterData()
		{
			_char_id = readD();
			_char_name = readS();
		}
	}

	@Override
	public void readImpl()
	{
		_last_char = readD();
		int size = readH();

		_chars.clear();
		while (size > 0)
		{
			size--;
			_chars.add(new CharacterData());
		}
	}

	@Override
	public void run()
	{
		CharacterSelect.Show();
		if (Config.getInstance()._quickLoginMode)
		{
			if (Main.getInstance().getScreen() instanceof CharacterSelect)
			{
				((CharacterSelect) Main.getInstance().getScreen()).charSelected(_last_char);
			}
		}
		Config.getInstance()._quickLoginMode = false;
	}
}
