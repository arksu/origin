package com.a4server.gameserver.network;

import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.GameClient.GameClientState;
import com.a4server.gameserver.network.clientpackets.AuthGame;
import com.a4server.gameserver.network.clientpackets.CharacterSelect;
import com.a4server.gameserver.network.clientpackets.EnterWorld;
import com.a4server.gameserver.network.clientpackets.GameClientPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 03.01.2015.
 */
public class GamePacketHandler
{
    protected static final Logger _log = LoggerFactory.getLogger(GamePacketHandler.class.getName());

    static public GameClientPacket HandlePacket(byte[] buf, GameClient client)
    {
        int opcode = buf[0] & 0xff;

        GameClientPacket pkt = null;
        GameClientState state = client.getState();

        switch (state)
        {
            case CONNECTED:
                switch (opcode)
                {
                    case 0x02:
                        pkt = new AuthGame();
                        break;
                    default:
                        debugOpcode(opcode, state);
                        break;
                }
                break;
            case AUTHED:
                switch (opcode)
                {
                    case 0x04:
                        pkt = new CharacterSelect();
                        break;
//                    case 0x05:
//                        pkt = new CharacterCreate();
//                        break;
//                    case 0x06:
//                        pkt = new CharacterDelete();
//                        break;
                    default:
                        debugOpcode(opcode, state);
                        break;
                }
                break;
            case IN_GAME:
                switch (opcode)
                {
                    case 0x09:
                        pkt = new EnterWorld();
                        break;
//                    case 0x0E:
//                        pkt = new MoveBackwardToLocation();
//                        break;

                    default:
                        debugOpcode(opcode, state);
                        break;
                }
        }

        if (pkt != null)
        {
            pkt.setData(buf);
        }
        return pkt;
    }

    static private void debugOpcode(int opcode, GameClientState state)
    {
        _log.info("Unknown Opcode: " + opcode + " for state: " + state.name());
    }
}
