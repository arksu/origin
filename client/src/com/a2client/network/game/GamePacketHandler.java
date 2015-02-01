package com.a2client.network.game;

import com.a2client.Log;
import com.a2client.network.game.serverpackets.*;
import com.a2client.screens.Login;

public class GamePacketHandler
{
    static public GameServerPacket HandlePacket(byte[] data)
    {
        int opcode = data[0] & 0xff;

        GameServerPacket pkt = null;
        //Log.info("game packet opcode: " + opcode);
        switch (opcode)
        {
            case 0x01:
                pkt = new Init();
                break;
            case 0x03:
                pkt = new CharacterList();
                break;
            case 0x07:
                pkt = new CharacterCreateFail();
                break;
            case 0x08:
                pkt = new ServerClose();
                break;
            case 0x0A:
                pkt = new CharSelected();
                break;
            case 0x0B:
                pkt = new MapGrid();
                break;
            case 0x0C:
                pkt = new TimeUpdate();
                break;
            case 0x0D:
                pkt = new CharInfo();
                break;
            case 0x0F:
                pkt = new StatusUpdate();
                break;
            case 0x10:
                pkt = new WorldInfo();
                break;
            case 0x11:
                pkt = new ObjectAdd();
                break;
            case 0x12:
                pkt = new ObjectRemove();
                break;
            case 0x13:
                pkt = new PlayerAppearance();
                break;
            default:
                debugOpcode(opcode);
                break;
        }

        // установим данные в пакет
        if (pkt != null)
        {
            pkt.setData(data);
        }
        return pkt;
    }

    static private void debugOpcode(int opcode)
    {
        Log.info("Unknown game packet opcode: " + opcode);
        Login.Error("packet");
    }
}
