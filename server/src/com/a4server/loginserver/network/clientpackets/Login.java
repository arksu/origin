package com.a4server.loginserver.network.clientpackets;

import com.a4server.loginserver.LoginClient;
import com.a4server.loginserver.LoginController;
import com.a4server.loginserver.LoginController.AuthLoginResult;
import com.a4server.loginserver.network.serverpackets.GameServerAuth;
import com.a4server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by arksu on 03.01.2015.
 */
public class Login extends LoginClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Login.class.getName());

	private String _login;
	private String _hash;

	@Override
	public void readImpl()
	{
		_login = readS();
		_hash = readS();
	}

	@Override
	public void run()
	{
		final LoginClient client = getClient();

		final LoginController lc = LoginController.getInstance();
		AuthLoginResult result = lc.tryAuthLogin(_login, _hash, client);
		switch (result)
		{
			case AUTH_SUCCESS:
				client.setAccount(_login);
				client.setState(LoginClient.LoginClientState.AUTHED_LOGIN);
				try
				{
					client.setSessionKey(lc.assignSessionKeyToClient(_login, client));
					client.sendPacket(new GameServerAuth(getClient()));
				}
				catch (SQLException e)
				{
					_log.error("cant assign session key to: " + client.getAccount());
				}
				client.close(null);
				break;
			case ALREADY_ON_LS:
				LoginClient oldClient;
				if ((oldClient = lc.getAuthedClient(_login)) != null)
				{
					// kick the other client
					oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
					lc.removeLoginClient(_login);
				}
				// kick also current client
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			case ALREADY_ON_GS:
				client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
				break;
			case INVALID_PASSWORD:
				client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
				break;
			case ACCOUNT_BANNED:
				client.close(LoginFailReason.REASON_PERMANENTLY_BANNED);
				break;
			case USER_NOT_FOUND:
				client.close(LoginFailReason.REASON_USER_NOT_FOUND);
				break;
			default:
				client.close(LoginFailReason.REASON_UNKNOWN_ERROR);
				break;

		}
	}
}
