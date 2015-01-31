package com.a2client.network.login.clientpackets;

import java.security.MessageDigest;

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

        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] raw = _pass.getBytes("UTF-8");
            byte[] hash = md.digest(raw);

            writeH(hash.length);
            writeB(hash);
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }
}
