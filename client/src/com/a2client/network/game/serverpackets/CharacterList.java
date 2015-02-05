package com.a2client.network.game.serverpackets;

import com.a2client.Config;
import com.a2client.Main;
import com.a2client.screens.CharacterSelect;

import java.util.ArrayList;
import java.util.List;

public class CharacterList extends GameServerPacket
{

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
        if (Config.quick_login_mode) {
            if (Main.getInstance().getScreen() instanceof CharacterSelect) {
                ((CharacterSelect)Main.getInstance().getScreen()).charSelected(_last_char);
            }
        }
        Config.quick_login_mode = false;
    }
}
