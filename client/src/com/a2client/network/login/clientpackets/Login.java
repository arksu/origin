package com.a2client.network.login.clientpackets;

import com.a2client.network.login.Crypt;
import com.a2client.util.scrypt.SCryptUtil;

public class Login extends LoginClientPacket
{

    private String _login, _pass;

    public Login(String login, String pass)
    {
        _login = login;
        _pass = pass;
    }

    @Override
    protected void write()
    {
        //CLogin 0x02
        writeC(0x02);

        writeS(_login);
        writeS(SCryptUtil.scrypt(_pass, Crypt.SCRYPT_N, Crypt.SCRYPT_R, Crypt.SCRYPT_P));
    }
}
