package com.a2client.network.game.clientpackets;

import com.a2client.Config;
import org.apache.log4j.Logger;

public class CharacterSelect extends GameClientPacket
{
    private static final Logger _log = Logger.getLogger(CharacterSelect.class.getName());
    private final int _char_id;

    public CharacterSelect(int char_id)
    {
        if (Config.debug)
        {
            _log.info("CharacterSelect: " + char_id);
        }
        _char_id = char_id;
    }

    @Override
    protected void write()
    {
        writeC(0x04);
        writeD(_char_id);
    }
}
