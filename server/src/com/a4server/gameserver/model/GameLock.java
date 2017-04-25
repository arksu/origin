package com.a4server.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * блокирока игрового объекта для различных действий
 * Created by arksu on 13.03.16.
 */
public class GameLock implements AutoCloseable
{
	private static final Logger _log = LoggerFactory.getLogger(GameLock.class.getName());

	private final GameObject _object;

	public GameLock(GameObject object)
	{
		_log.debug("GameLock " + this + " created");
		_object = object;
	}

	@Override
	public void close() throws RuntimeException
	{
		_log.debug("GameLock " + this.toString() + " closed");
		_object.unlock();
	}
}
