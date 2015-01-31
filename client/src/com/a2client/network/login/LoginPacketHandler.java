package com.a2client.network.login;

import com.a2client.Log;
import com.a2client.network.login.serverpackets.GameServerAuth;
import com.a2client.network.login.serverpackets.Init;
import com.a2client.network.login.serverpackets.LoginFail;
import com.a2client.network.login.serverpackets.LoginServerPacket;
import com.a2client.screens.Login;

public class LoginPacketHandler
{
    static public LoginServerPacket HandlePacket(byte[] data)
    {
        int opcode = data[0] & 0xff;

        LoginServerPacket pkt = null;
        switch (opcode)
        {
            case 0x01:
                pkt = new Init();
                break;
            case 0x03:
                pkt = new LoginFail();
                break;
            case 0x04:
                pkt = new GameServerAuth();
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
        Log.info("Unknown login packet opcode: " + opcode);
        Login.Error("packet");
    }
}
