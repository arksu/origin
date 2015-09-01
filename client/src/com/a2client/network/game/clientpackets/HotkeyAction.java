package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.02.15.
 */
// todo : delete?
public class HotkeyAction extends GameClientPacket
{
    private static final Logger _log = LoggerFactory.getLogger(HotkeyAction.class.getName());

    int _hotkey;

    public HotkeyAction(int hotkey) {
        _hotkey = hotkey;
    }

    @Override
    protected void write()
    {
        writeC(0x1A);
        writeD(_hotkey);
    }
}
