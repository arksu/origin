package com.a4server.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * игровая блокировка для различных действий
 * Created by arksu on 13.03.16.
 */
public class GameLock implements AutoCloseable
{
	private static final Logger _log = LoggerFactory.getLogger(GameLock.class.getName());

	private final ReentrantLock _lock;

	public GameLock(ReentrantLock lock)
	{
		_log.debug("GameLock " + this.hashCode() + " created");
		_lock = lock;
	}

	@Override
	public void close() throws RuntimeException
	{
		_log.debug("GameLock " + this.hashCode() + " closed");
		_lock.unlock();
	}
}
