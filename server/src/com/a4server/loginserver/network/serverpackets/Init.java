package com.a4server.loginserver.network.serverpackets;

import com.a4server.Config;

/**
 * Created by arksu on 03.01.2015.
 */
public class Init extends LoginServerPacket
{

	@Override
	protected void write()
	{
		// SInit 0x01
		writeC(0x01);

		// proto version
		writeC(Config.LOGIN_PROTO_VERSION);

		writeD(Config.SCRYPT_N);
		writeD(Config.SCRYPT_P);
		writeD(Config.SCRYPT_R);
	}
}