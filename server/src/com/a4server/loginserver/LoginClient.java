package com.a4server.loginserver;

import com.a4server.Config;
import com.a4server.loginserver.network.LoginPacketHandler;
import com.a4server.loginserver.network.serverpackets.LoginFail;
import com.a4server.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.a4server.util.network.BaseRecvPacket;
import com.a4server.util.network.BaseSendPacket;
import com.a4server.util.network.NetClient;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by arksu on 03.01.2015.
 */
public class LoginClient extends NetClient
{
	private static final Logger _log = LoggerFactory.getLogger(LoginClient.class.getName());

	public static enum LoginClientState
	{
		/**
		 * Client has just connected .
		 */
		CONNECTED,
		/**
		 * Client has authed but doesn't has character attached to it yet.
		 */
		AUTHED_LOGIN
	}

	private ChannelHandlerContext _channel;
	private final ArrayBlockingQueue<byte[]> _pktQueue;
	private final ReentrantLock _pktLock = new ReentrantLock();

	private LoginClientState _state;
	private String _account;
	private SessionKey _sessionKey;
	private int _accessLevel;
	private int _last_char;

	private final long _connectionStartTime;

	public LoginClient(ChannelHandlerContext ctx)
	{
		_channel = ctx;
		_pktQueue = new ArrayBlockingQueue<>(Config.NET_PACKET_RECV_QUEUE_SIZE);
		_state = LoginClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
	}

	public void close(LoginFailReason reason)
	{
		if (reason != null)
		{
			try
			{
				final ChannelFuture f = _channel.writeAndFlush(new LoginFail(reason));
				f.addListener((ChannelFutureListener) future -> {
					assert f == future;
					_channel.close();
				});
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			_channel.close();
		}
	}

	public LoginClientState getState()
	{
		return _state;
	}

	public void setState(LoginClientState state)
	{
		_state = state;
	}

	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}

	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}

	public void setAccount(String account)
	{
		_account = account;
	}

	public String getAccount()
	{
		return _account;
	}

	public void setAccessLevel(int accesslevel)
	{
		_accessLevel = accesslevel;
	}

	public int getAccessLevel()
	{
		return _accessLevel;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	public ChannelHandlerContext getChannel()
	{
		return _channel;
	}

	public InetAddress getInetAddress()
	{
		return ((InetSocketAddress) getChannel().channel().remoteAddress()).getAddress();
	}

	public void addReadPacketQueue(byte[] msg)
	{
		if (msg != null)
		{
			_pktQueue.add(msg);
		}
	}

	public void onDisconnect()
	{
		if (Config.DEBUG)
		{
			_log.info("DISCONNECTED : " + toString());
		}
		LoginController.getInstance().removeLoginClient(getAccount());
		_channel = null;
	}

	public void sendPacket(BaseSendPacket pkt)
	{
		if (_channel != null && !_channel.isRemoved())
		{
			_channel.writeAndFlush(pkt);
		}
	}

	/**
	 * обработка пакета в очереди
	 * метод synchronized для того чтобы очередь этого клиента обрабатывалась последовательно
	 * т.е. когда пакетов в очереди много, только 1 поток может их обрабатывать
	 * @throws InterruptedException
	 */
	synchronized public void ProcessPacket() throws InterruptedException
	{
		if (!_pktLock.tryLock())
		{
			return;
		}

		try
		{
			while (true)
			{
				// получаем данные для пакета
				byte[] buf = _pktQueue.poll();

				if (buf == null)
				{
					return;
				}

				// получаем экземпляр пакета
				BaseRecvPacket pkt = LoginPacketHandler.HandlePacket(buf, this);

				if (pkt != null)
				{
					// подготовим
					pkt.setClient(this);
					// читаем его
					pkt.readImpl();
					// обрабатываем
					pkt.run();
				}
			}
		}
		finally
		{
			_pktLock.unlock();
		}
	}
}
