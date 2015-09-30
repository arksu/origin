package com.a4server.loginserver.network.clientpackets;

import com.a4server.loginserver.LoginClient;
import com.a4server.util.network.BaseRecvPacket;
import com.a4server.util.network.BaseSendPacket;
import com.a4server.util.network.NetClient;

/**
 * Created by arksu on 03.01.2015.
 */
public abstract class LoginClientPacket extends BaseRecvPacket
{
	protected LoginClient client;

	public void setClient(NetClient client)
	{
		this.client = (LoginClient) client;
	}

	public LoginClient getClient()
	{
		return client;
	}

	public void sendPacket(BaseSendPacket pkt)
	{
		getClient().sendPacket(pkt);
	}
}
