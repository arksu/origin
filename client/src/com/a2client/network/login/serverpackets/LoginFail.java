package com.a2client.network.login.serverpackets;

import com.a2client.Log;
import com.a2client.screens.Login;

public class LoginFail extends LoginServerPacket
{
	private int _reason;

	@Override
	public void readImpl()
	{
		_reason = readC();
	}

	@Override
	public void run()
	{
		Log.debug("SLoginFail");
		switch (_reason)
		{
			case 0x01:
				Login.Error("wrong_password");
				break;
			case 0x02:
				//REASON_USER_NOT_FOUND
				Login.Error("user_not_found");
				break;
			case 0x03:
				//REASON_PERMANENTLY_BANNED
				Login.Error("banned");
				break;
			case 0x04:
				//REASON_ACCOUNT_IN_USE
				Login.Error("account_in_use");
				break;
			case 0x05:
				//REASON_PURGE_TIMEOUT
				Login.Error("timeout");
				break;

			default:
				Login.Error("unknown_reason");
				Log.error("unknown reason: " + _reason + " in LoginFail");
				break;
		}
	}
}
