package com.a4server.gameserver;

import com.a4server.Config;
import com.a4server.ThreadPoolManager;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.World;
import com.a4server.gameserver.network.GamePacketHandler;
import com.a4server.gameserver.network.serverpackets.CharacterList;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ServerClose;
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
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by arksu on 03.01.2015.
 */
public class GameClient extends NetClient
{
	private static final Logger _log = LoggerFactory.getLogger(GameClient.class.getName());

	private ChannelHandlerContext _channel;
	private final ArrayBlockingQueue<byte[]> _pktQueue;
	private final ReentrantLock _pktLock = new ReentrantLock();
	private GameClientState _state;
	/**
	 * время когда установлено соединение
	 */
	private final long _connectionStartTime;
	/**
	 * имя аккаунта с которым связан клиент
	 */
	private String _account;
	/**
	 * отсоединен от клиента. в состоянии завершения
	 */
	private boolean _isDetached;
	/**
	 * id последнего используемого чара
	 */
	private int _lastChar;
	/**
	 * инфа о чарах. список чаров. нужно чтобы потом понять какого именно выбрал клиент для входа в мир
	 */
	private Map<Integer, CharacterList.CharacterData> _charsInfo;
	/**
	 * блокировка на выбор чара при входе в игру
	 */
	private final ReentrantLock _activeCharLock = new ReentrantLock();

	/**
	 * активный персонаж которым играем
	 */
	private Player _activeChar;

	/**
	 * задача очистки всех упоминаний об игроке
	 */
	protected ScheduledFuture<?> _cleanupTask = null;

	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}

	public GameClient(ChannelHandlerContext ctx)
	{
		_channel = ctx;
		_pktQueue = new ArrayBlockingQueue<>(Config.NET_PACKET_RECV_QUEUE_SIZE);
		_state = GameClientState.CONNECTED;
		// время когда установлено соединение
		_connectionStartTime = System.currentTimeMillis();
		_isDetached = false;
		_charsInfo = null;
		_activeChar = null;

		//        if (Config.CHAR_STORE_INTERVAL > 0)
		//        {
		//            _autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, (Config.CHAR_STORE_INTERVAL * 1000L));
		//        }
		//        else
		//        {
		//            _autoSaveInDB = null;
		//        }

	}

	public Player loadCharacter(int charId)
	{
		Player character;

		// проверим есть ли такой персонаж уже в игре?
		character = World.getInstance().getPlayer(charId);
		if (character != null)
		{
			_log.error("Attempt of double login: " + character.getName() + "(" + charId + ") " + getAccount());
			// кикнем того кто в игре
			character.kick();
			return null;
		}

		character = Player.load(charId);
		if (character != null)
		{
			// установим параметры перса
			// скорость бега и тд...
		}
		else
		{
			_log.warn("failed load player character");
		}

		return character;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState state)
	{
		_state = state;
	}

	public void setLastChar(int lastChar)
	{
		_lastChar = lastChar;
	}

	public int getLastChar()
	{
		return _lastChar;
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	public void setCharsInfo(Map<Integer, CharacterList.CharacterData> charsinfo)
	{
		_charsInfo = charsinfo;
	}

	public Map<Integer, CharacterList.CharacterData> getCharsInfo()
	{
		return _charsInfo;
	}

	public Player getActiveChar()
	{
		return _activeChar;
	}

	public void setActiveChar(Player pActiveChar)
	{
		_activeChar = pActiveChar;
	}

	public void setAccount(String account)
	{
		_account = account;
	}

	public String getAccount()
	{
		return _account;
	}

	public void sendPacket(BaseSendPacket pkt)
	{
		if (_channel != null && !_channel.isRemoved() && !_isDetached && pkt != null)
		{
			_channel.writeAndFlush(pkt);
		}
	}

	public void addReadPacketQueue(byte[] msg)
	{
		if (msg != null)
		{
			_pktQueue.add(msg);
		}
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

	/**
	 * закрыть коннект отослав пакет
	 * @param pkt пакет
	 */
	public void close(GameServerPacket pkt)
	{
		_isDetached = true; // prevents more packets execution
		if (getChannel() == null)
		{
			return; // offline shop
		}

		if (pkt != null)
		{
			try
			{
				// шлем пакет, ждем когда уйдет.
				final ChannelFuture f = _channel.writeAndFlush(pkt);
				f.addListener(new ChannelFutureListener()
				{
					@Override
					public void operationComplete(ChannelFuture future)
					{
						assert f == future;
						// закроем коннект
						_channel.close();
						_channel = null;
					}
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
			_channel = null;
		}
	}

	/**
	 * Close client connection with {@link ServerClose} packet
	 */
	public void closeNow()
	{
		_isDetached = true; // больше пакеты от клиента не обрабатываем
		close(ServerClose.STATIC_PACKET);
		synchronized (this)
		{
			if (_cleanupTask == null)
			{
				_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); // instant
			}
		}
	}

	/**
	 * все очистить, разорвать все связи
	 * фактически деструктор
	 */
	protected class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// отменим автосейв, сохранять будем в ручном режиме
				//                if (_autoSaveInDB != null)
				//                {
				//                    _autoSaveInDB.cancel(true);
				//                }

				if (getActiveChar() != null) // this should only happen on connection loss
				{
					//                    if (getActiveChar().isLocked())
					//                    {
					//                        _log.log(Level.WARNING, "Player " + getActiveChar().getName() + " still performing subclass actions during disconnect.");
					//                    }
					//
					//                    // we store all data from players who are disconnected while in an event in order to restore it in the next login
					//                    if (L2Event.isParticipant(getActiveChar()))
					//                    {
					//                        L2Event.savePlayerEventStatus(getActiveChar());
					//                    }

					// prevent closing again
					getActiveChar().setClient(null);

					if (getActiveChar().isOnline())
					{
						getActiveChar().deleteMe();
					}
				}
				setActiveChar(null);
			}
			catch (Exception e1)
			{
				_log.warn("Error while cleanup client.", e1);
			}
			//            finally
			//            {
			//                LoginServerThread.getInstance().sendLogout(getAccountName());
			//            }
		}
	}

	/**
	 * обработка пакета в очереди
	 * метод synchronized для того чтобы очередь этого клиента обрабатывалась последовательно
	 * т.е. когда пакетов в очереди много, только 1 поток может их обрабатывать
	 * @throws InterruptedException
	 */
	public void ProcessPacket() throws InterruptedException
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

				if (_isDetached)
				{
					_pktQueue.clear();
					return;
				}

				// получаем экземпляр пакета
				BaseRecvPacket pkt = GamePacketHandler.HandlePacket(buf, this);
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

	@Override
	public void onDisconnect()
	{
		_isDetached = true;
		if (_activeChar != null)
		{
			_log.info(_activeChar.toString() + " disconnected");
			_activeChar.deleteMe();
			_activeChar = null;
		}
	}
}
