package com.a4server.loginserver.network;

import com.a4server.loginserver.LoginClient;
import com.a4server.loginserver.network.clientpackets.Login;
import com.a4server.loginserver.network.clientpackets.LoginClientPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 03.01.2015.
 */
public class LoginPacketHandler
{
    private static final Logger _log = LoggerFactory.getLogger(LoginPacketHandler.class.getName());

    static public LoginClientPacket HandlePacket(byte[] buf, LoginClient client)
    {
        int opcode = buf[0] & 0xff;

        LoginClientPacket pkt = null;
        LoginClient.LoginClientState state = client.getState();

        switch (state)
        {
            case CONNECTED:
                switch (opcode)
                {
                    case 0x02:
                        pkt = new Login();
                        break;

                    default:
                        debugOpcode(opcode, state);
                        break;
                }
                break;
            case AUTHED_LOGIN:
                switch (opcode)
                {

                    default:
                        debugOpcode(opcode, state);
                        break;
                }
                break;
        }

        if (pkt != null)
        {
            pkt.setData(buf);
        }
        return pkt;
    }

    static private void debugOpcode(int opcode, LoginClient.LoginClientState state)
    {
        _log.info("Unknown Opcode: " + opcode + " for state: " + state.name());
    }
}
