package com.a2client.network.game;

import com.a2client.Log;
import com.a2client.network.game.serverpackets.GameServerPacket;
import com.a2client.screens.Login;

import java.util.HashMap;
import java.util.Map;

public class GamePacketHandler
{
    static private Map<Integer, Class<? extends GameServerPacket>> _packets = new HashMap<>();

    static public GameServerPacket HandlePacket(byte[] data)
    {
        int opcode = data[0] & 0xff;

        GameServerPacket pkt = null;

        Class<? extends GameServerPacket> pktClass = _packets.get(opcode);
        if (pktClass != null)
        {
            try
            {
                pkt = pktClass.getConstructor().newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Login.Error("unknown_error");
            }
        }
        else
        {
            debugOpcode(opcode);
        }

        //Log.info("game packet opcode: " + opcode);
//        switch (opcode)
//        {
//            case 0x01:
//                pkt = new Init();
//                break;
//            case 0x03:
//                pkt = new CharacterList();
//                break;
//            case 0x07:
//                pkt = new CharacterCreateFail();
//                break;
//            case 0x08:
//                pkt = new ServerClose();
//                break;
//            case 0x0A:
//                pkt = new CharSelected();
//                break;
//            case 0x0B:
//                pkt = new MapGrid();
//                break;
//            case 0x0C:
//                pkt = new TimeUpdate();
//                break;
//            case 0x0D:
//                pkt = new CharInfo();
//                break;
//            case 0x0F:
//                pkt = new StatusUpdate();
//                break;
//            case 0x10:
//                pkt = new WorldInfo();
//                break;
//            case 0x11:
//                pkt = new ObjectAdd();
//                break;
//            case 0x12:
//                pkt = new ObjectRemove();
//                break;
//            case 0x13:
//                pkt = new PlayerAppearance();
//                break;
//            default:
//                debugOpcode(opcode);
//                break;
//        }

        // установим данные в пакет
        if (pkt != null)
        {
            pkt.setData(data);
        }
        return pkt;
    }

    static public void AddPacketType(int opcode, Class<? extends GameServerPacket> pkt)
    {
        _packets.put(opcode, pkt);
    }
    
    static public void InitPackets() {
//        Reflections reflections = new Reflections("my.project.prefix");
//
//        Set<Class<? extends Object>> allClasses =
//                reflections.getSubTypesOf(Object.class);
        
    }

    static private void debugOpcode(int opcode)
    {
        Log.info("Unknown game server packet opcode: " + opcode);
        Login.Error("packet");
    }
}
