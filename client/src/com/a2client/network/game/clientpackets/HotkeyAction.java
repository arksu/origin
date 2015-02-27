package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.02.15.
 */
public class HotkeyAction extends GameClientPacket
{
    private static final Logger _log = LoggerFactory.getLogger(HotkeyAction.class.getName());

    public static final int HOTKEY_INVENTORY = 1;
    public static final int HOTKEY_PAPERDOLL = 2;
    public static final int HOTKEY_SKILLS = 3;

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
