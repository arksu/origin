package com.a2client.network.login.clientpackets;

import com.a2client.network.login.Crypt;
import com.a2client.util.scrypt.SCryptUtil;

public class Login extends LoginClientPacket
{
	private String _login, _password;

	public Login(String login, String password)
	{
		_login = login;
		_password = password;
	}

	@Override
	protected void write()
	{
		//CLogin 0x02
		writeC(0x02);

		writeS(_login);
		writeS(Crypt.isPassowrdHash(_password) ? _password : SCryptUtil.scrypt(_password, Crypt.SCRYPT_N, Crypt.SCRYPT_R, Crypt.SCRYPT_P));
	}
}
