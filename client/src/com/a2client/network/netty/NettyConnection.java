package com.a2client.network.netty;

import com.a2client.screens.Login;
import com.a2client.util.network.BaseRecvPacket;
import com.a2client.util.network.BaseSendPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketException;
import java.util.LinkedList;

public class NettyConnection
{
	private static final Logger _log = LoggerFactory.getLogger(NettyConnection.class.getName());

	public enum ConnectionType
	{
		LOGIN_SERVER,
		GAME_SERVER
	}

	private final String _host;
	private final int _port;
	private final ConnectionType _type;
	private final LinkedList<BaseRecvPacket> _packet_queue = new LinkedList<BaseRecvPacket>();
	private ChannelHandlerContext _channel;
	private boolean _wait_connect;
	private volatile int _recv_counter;
	private volatile int _send_counter;

	public NettyConnection(String host, int port, ConnectionType type)
	{
		_host = host;
		_port = port;
		_type = type;
		_channel = null;
		_wait_connect = true;
		_recv_counter = 0;
		_send_counter = 0;
		start();
	}

	public void incRecvCounter(int val)
	{
		_recv_counter += val;
	}

	public void incSendCounter(int val)
	{
		_send_counter += val;
	}

	public int getRecvCounter()
	{
		return _recv_counter;
	}

	public int getSendCounter()
	{
		return _send_counter;
	}

	protected void start()
	{
		NetWorker worker = new NetWorker();
		worker.start();
	}

	synchronized public void Close()
	{
		if (isActive() && _channel != null)
		{
			_channel.close();
			_channel = null;
		}
	}

	synchronized public void addPacket(BaseRecvPacket pkt)
	{
		_packet_queue.addLast(pkt);
	}

	synchronized private BaseRecvPacket takePacket()
	{
		if (_packet_queue.isEmpty())
		{
			return null;
		}
		else
		{
			return _packet_queue.removeFirst();
		}
	}

	public void processPackets()
	{
		while (true)
		{
			BaseRecvPacket pkt = takePacket();

			if (pkt != null)
			{
				try
				{
					pkt.readImpl();
					pkt.run();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					Login.Error("packet");
				}
			}
			else
			{
				break;
			}
		}
	}

	synchronized public boolean isActive()
	{
		return (_channel != null || _wait_connect);
	}

	public ConnectionType getType()
	{
		return _type;
	}

	synchronized public void setChannel(ChannelHandlerContext ctx)
	{
		if (ctx != null)
		{
			_wait_connect = false;
		}
		_channel = ctx;
	}

	synchronized public void sendPacket(BaseSendPacket pkt)
	{
		if (_channel != null && !_channel.isRemoved())
		{
			_channel.writeAndFlush(pkt);
		}
	}

	/**
	 * Net worker for netty connection
	 */
	private class NetWorker extends Thread
	{
		public NetWorker()
		{
			super("NetWorker");
		}

		public void run()
		{
			try
			{
				worker();
			}
			catch (InterruptedException e)
			{
				_log.error("net InterruptedException: " + e.getMessage());
				e.printStackTrace();
			}
			catch (ConnectException e)
			{
				_log.error("connection error: " + e.getMessage());
				if (e.getMessage().contains("refused"))
				{
					if (_type == ConnectionType.LOGIN_SERVER)
					{
						Login.Error("loginserver_refused");
					}
					else
					{
						Login.Error("gameserver_refused");
					}
				}
				else
				{
					Login.Error("connect_error");
				}

				synchronized (NettyConnection.this)
				{
					NettyConnection.this._wait_connect = false;
				}
			}
			catch (SocketException e)
			{
				_log.error("connection error: " + e.getMessage());
				Login.Error("refused");

				synchronized (NettyConnection.this)
				{
					NettyConnection.this._wait_connect = false;
				}
			}
			catch (Exception e)
			{
				_log.error("net error: " + e.getMessage());
				e.printStackTrace();
			}
			NettyConnection.this.setChannel(null);
		}

		void worker() throws Exception
		{
			EventLoopGroup group = new NioEventLoopGroup();
			try
			{
				Bootstrap b = new Bootstrap();
				b.group(group)
				 .channel(NioSocketChannel.class)
//				 .option(ChannelOption.TCP_NODELAY, true)
				 .handler(new ChannelInitializer<SocketChannel>()
				 {
					 @Override
					 public void initChannel(SocketChannel ch) throws Exception
					 {
						 ch.pipeline()
						   .addLast(new PacketDecoder(), new PacketEncoder(),
									new ConnectionHandler(NettyConnection.this));
					 }
				 });

				// Start the client.
				ChannelFuture f = b.connect(_host, _port).sync();

				// Wait until the connection is closed.
				f.channel().closeFuture().sync();
			}
			finally
			{
				// Shut down the event loop to terminate all threads.
				group.shutdownGracefully();
			}

		}
	}
}
